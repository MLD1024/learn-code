package com.example.rpc.core;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

public class RpcClient {
    private final String host;
    private final int port;
    private Channel channel;

    public RpcClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void connect() throws InterruptedException {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ch.pipeline()
                                    .addLast(new RpcEncoder(RpcRequest.class))
                                    .addLast(new RpcDecoder(RpcResponse.class))
                                    .addLast(new RpcClientHandler());
                        }
                    });

            ChannelFuture f = b.connect(host, port).sync();
            channel = f.channel();
        } finally {
            // group.shutdownGracefully().sync();
            // 添加优雅关闭等待时间
            // Thread.sleep(500);
        }
    }

    public Object send(RpcRequest request) throws Exception {
        ChannelFuture future = channel.writeAndFlush(request).sync();
        if (!future.isSuccess()) {
            throw new RuntimeException("Request send failed");
        }
        return RpcClientHandler.getResponse(request.getRequestId());
    }

    public void close() {
        if (channel != null) {
            channel.close();
        }

    }
}