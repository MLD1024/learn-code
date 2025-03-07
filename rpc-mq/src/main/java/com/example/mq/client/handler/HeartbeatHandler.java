package com.example.mq.client.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;

public class HeartbeatHandler extends IdleStateHandler {
    private static final int READ_IDLE_TIME = 30;

    public HeartbeatHandler() {
        super(READ_IDLE_TIME, 0, 0);
    }

    @Override
    protected void channelIdle(ChannelHandlerContext ctx, IdleStateEvent evt) {
        if (evt == IdleStateEvent.FIRST_READER_IDLE_STATE_EVENT) {
            ctx.close();
            return;
        }
        try {
            super.channelIdle(ctx, evt);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}