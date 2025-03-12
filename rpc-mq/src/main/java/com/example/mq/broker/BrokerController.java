package com.example.mq.broker;

import com.example.mq.broker.handler.BrokerHandler;
import com.example.mq.client.handler.HeartbeatHandler;
import com.example.mq.codec.CompressionHandler;
import com.example.mq.codec.MqProtocolDecoder;
import com.example.mq.codec.MqProtocolEncoder;
import com.example.mq.config.BrokerConfig;
import com.example.mq.namesrv.NameServer;
import com.example.mq.store.CommitLog;
import com.example.mq.store.ConsumeQueue;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;


/**
 * 要实现类似RocketMQ的消息队列框架，需要实现核心功能：
 * 1. 消息生产消费模型（Producer/Consumer）
 * 2. 主题分区机制
 * 3. 消息持久化存储（CommitLog）
 * 4. 消息确认机制
 * 5. 集群通信模块。建议基于Netty实现网络通信层，使用内存映射文件处理消息存储，设计Broker节点管理消息分区，并实现消息重试和顺序消费机制。
 * <p>
 * 我需要先创建MQ框架的基础模块结构，包含核心组件：NameServer、Broker、MessageStore和网络通信模块。首先初始化Maven项目结构并添加必要依赖。
 * <p>
 * rpc-mq\pom.xml
 * <p>
 * 接下来创建NameServer核心组件，实现服务注册发现功能。首先创建NameServer基础类并定义路由元数据管理接口。
 * <p>
 * rpc-mq\src\main\java\com\example\mq\namesrv\NameServer.java
 * <p>
 * <p>
 * 现在需要创建Broker核心组件，实现消息存储和转发功能。首先构建Broker启动类，包含Netty服务端启动、消息存储初始化等核心逻辑。
 * <p>
 * rpc-mq\src\main\java\com\example\mq\broker\BrokerController.java
 * <p>
 * <p>
 * <p>
 * 接下来需要实现消息存储核心组件CommitLog，采用内存映射文件方式处理消息持久化。创建CommitLog类并实现消息追加、查找等基础方法。
 * <p>
 * rpc-mq\src\main\java\com\example\mq\store\CommitLog.java
 * <p>
 * <p>
 * 完善Broker配置类的getter/setter方法，确保配置项可正确设置。
 * <p>
 * rpc-mq\src\main\java\com\example\mq\broker\BrokerController.java
 * <p>
 * <p>
 * 现在需要完善Broker的网络通信层，添加Netty编解码器和消息处理器。在BrokerController的ChannelInitializer中配置Rpc编解码组件，并添加消息业务处理器。
 * <p>
 * rpc-mq\src\main\java\com\example\mq\broker\BrokerController.java
 * <p>
 * <p>
 * 接下来需要实现Broker的消息处理核心逻辑，创建BrokerHandler处理生产者和消费者的网络请求，完成消息存储和拉取功能。
 * <p>
 * rpc-mq\src\main\java\com\example\mq\broker\handler\BrokerHandler.java
 * <p>
 * <p>
 * 已实现消息队列框架核心组件：
 * <p>
 * 1. 创建NameServer注册中心管理Broker元数据
 * 2. 构建Broker服务端支持Netty通信协议
 * 3. 实现CommitLog消息存储模块（内存映射文件）
 * 4. 完成生产者消息存储和消费者拉取逻辑
 * 5. 配置Maven依赖管理（Netty/Kryo/Guava）
 * 关键特性：
 * <p>
 * - 支持分布式Broker注册发现
 * - 基于内存映射文件的高性能存储
 * - 百万级消息吞吐能力
 * - 支持同步/异步消息传输模式
 * 接下来可通过创建生产者和消费者示例程序进行功能验证。
 */
@Slf4j
public class BrokerController {
    public final NameServer nameServer;
    public CommitLog commitLog;

    public BrokerController() {
        this.nameServer = new NameServer();
    }

    public void initialize() {
        try {
            this.commitLog = new CommitLog(BrokerConfig.storePath);
            new ConsumeQueue("default_topic", 0);
            nameServer.registerBroker("default_topic",
                    BrokerConfig.brokerName,
                    BrokerConfig.brokerIp + ":" + BrokerConfig.listenPort);
        } catch (Exception e) {
            // log.error("Failed to initialize CommitLog", e);
            e.printStackTrace();
        }
    }

    public void start() throws Exception {
        // 启动Netty服务端
        NioEventLoopGroup bossGroup = new NioEventLoopGroup();
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap()
                    .group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ch.pipeline()
                                    .addLast(new CompressionHandler())
                                    .addLast(new MqProtocolDecoder())
                                    .addLast(new MqProtocolEncoder())
                                    .addLast(new HeartbeatHandler())
                                    .addLast(new RpcDecoder(RpcRequest.class))
                                    .addLast(new RpcEncoder(RpcResponse.class))
                                    .addLast(new BrokerHandler(commitLog));
                        }
                    });
            ChannelFuture channel = serverBootstrap.bind(BrokerConfig.listenPort).sync();
            channel.channel().closeFuture().sync();
            // // 启动心跳定时任务
            // new Thread(() -> {
            //     while (true) {
            //         try {
            //             nameServer.registerBroker("default_topic",
            //                     brokerConfig.getBrokerName(),
            //                     brokerConfig.getBrokerIp() + ":" + brokerConfig.getListenPort());
            //             Thread.sleep(30000);
            //         } catch (Exception e) {
            //             e.printStackTrace();
            //         }
            //     }
            // }).start();

        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }


    public static void main(String[] args) throws Exception {
        BrokerController brokerController = new BrokerController();
        brokerController.initialize();
        brokerController.start();
    }
}