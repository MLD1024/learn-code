package com.example.mq.client;

import com.example.mq.broker.BrokerController;
import com.example.mq.broker.RpcDecoder;
import com.example.mq.broker.RpcEncoder;
import com.example.mq.broker.RpcRequest;
import com.example.mq.broker.RpcResponse;
import com.example.mq.client.handler.CompressionHandler;
import com.example.mq.client.handler.ConnectionRetryHandler;
import com.example.mq.client.handler.HeartbeatHandler;
import com.example.mq.codec.MqProtocolDecoder;
import com.example.mq.client.handler.RpcClientHandler;
import com.example.mq.codec.MqProtocolEncoder;
import com.example.mq.namesrv.NameServer;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelException;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Collectors;

public class RpcClient {
    private final int maxConnections;
    private final int heartbeatInterval;
    private final Map<String, Channel> connectionPool = new ConcurrentHashMap<>();
    private final ScheduledExecutorService heartbeatExecutor = Executors.newSingleThreadScheduledExecutor();
    private NameServer nameServer;
    private BrokerController.BrokerConfig brokerConfig;
    public RpcClient(int maxConnections, int heartbeatInterval, BrokerController.BrokerConfig brokerConfig, NameServer nameServer) {
        this.maxConnections = maxConnections;
        this.heartbeatInterval = heartbeatInterval;
        this.brokerConfig = brokerConfig;
        this.nameServer = nameServer;
    }
    private Channel channel;
    public RpcClient(int maxConnections, int heartbeatInterval) {
        this.maxConnections = maxConnections;
        this.heartbeatInterval = heartbeatInterval;
    }

    private Channel getChannel(String address) {
        return connectionPool.computeIfAbsent(address, addr -> {
            String[] parts = addr.split(":");
            return createChannel(parts[0], Integer.parseInt(parts[1]));
        });
    }

    private Channel createChannel(String host, int port) {
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(new NioEventLoopGroup())
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ch.pipeline()
                                .addLast(new IdleStateHandler(0, heartbeatInterval / 1000, 0))
                                .addLast(new CompressionHandler())
                                .addLast(new MqProtocolEncoder())  // 新增MQ专用编码器
                                .addLast(new MqProtocolDecoder())  // 新增MQ专用解码器
                                .addLast(new HeartbeatHandler())
                                .addLast(new ConnectionRetryHandler(nameServer)); // 新增故障转移处理器
                    }
                });
        return bootstrap.connect(host, port).syncUninterruptibly().channel();
    }

    public void connect() throws InterruptedException {
        NioEventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ch.pipeline()
                                .addLast(new RpcEncoder(RpcRequest.class))
                                .addLast(new RpcDecoder(RpcResponse.class))
                                .addLast(new MqProtocolEncoder())
                                .addLast(new MqProtocolDecoder())
                                .addLast(new RpcClientHandler());
                        }
                    });

            try {
                String brokerIp = brokerConfig.getBrokerIp();
                int listenPort = brokerConfig.getListenPort();
                ChannelFuture future = bootstrap.connect(brokerIp, listenPort).sync();
                channel = future.channel();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("连接被中断", e);
            } catch (ChannelException e) {
                throw new RuntimeException("连接失败，请检查端口监听状态", e);
            } finally {
                if (channel == null || !channel.isActive()) {
                    group.shutdownGracefully();
                }
            }
        } finally {
            group.shutdownGracefully();
        }
    }

    public RpcResponse send(RpcRequest request) throws Exception {
        int retryCount = 0;
        List<String> brokers = nameServer.getBrokerAddresses(request.getTopic());

        while (retryCount < brokers.size()) {
            String currentBroker = brokers.get(retryCount);
            try {
                Channel currentChannel = getChannel(currentBroker);
                ChannelFuture future = currentChannel.writeAndFlush(request).sync();
                
                if (!future.isSuccess()) {
                    throw new RuntimeException("请求发送失败，Broker: " + currentBroker);
                }

                return RpcClientHandler.getResponse(request.getRequestId());
            } catch (Exception e) {
                connectionPool.remove(currentBroker);
                if (retryCount == brokers.size() - 1) {
                    brokers = nameServer.getBrokerAddresses(request.getTopic());  // 刷新Broker列表
                    if (retryCount >= brokers.size() - 1) {
                        throw new RuntimeException("所有Broker不可用: " + brokers, e);
                    }
                }
                retryCount++;
                Thread.sleep(1000);  // 重试间隔
            }
        }
        throw new RuntimeException("Failed after " + brokers.size() + " retries");
    }

    // 新增故障转移处理逻辑
    private List<String> getBackupBrokers(String topic) {
        return nameServer.getBrokerAddresses(topic)
                .stream()
                .skip(1)
                .collect(Collectors.toList());
    }
}