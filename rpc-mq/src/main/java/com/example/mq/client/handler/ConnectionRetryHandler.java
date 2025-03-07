package com.example.mq.client.handler;

import com.example.mq.namesrv.NameServer;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleStateEvent;

public class ConnectionRetryHandler extends ChannelInboundHandlerAdapter {
    private final NameServer nameServer;
    private int retryCount = 0;
    private static final int MAX_RETRY = 3;

    public ConnectionRetryHandler(NameServer nameServer) {
        this.nameServer = nameServer;
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        if (retryCount++ < MAX_RETRY) {
            System.out.println("尝试重连第" + retryCount + "次");
            ctx.connect(nameServer.getBrokerAddresses());
        } else {
            System.out.println("超过最大重试次数，放弃连接");
            ctx.fireChannelInactive();
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
        if (evt instanceof IdleStateEvent) {
            ctx.close();
        }
    }
}