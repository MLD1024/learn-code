package com.example.mq.broker.handler;

import com.example.mq.broker.RpcRequest;
import com.example.mq.broker.RpcResponse;
import com.example.mq.store.CommitLog;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class BrokerHandler extends SimpleChannelInboundHandler<RpcRequest> {
    private final CommitLog commitLog;

    public BrokerHandler(CommitLog commitLog) {
        this.commitLog = commitLog;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcRequest request) {
        // 处理生产者发送消息
        if (request.getMethodName().equals("sendMessage")) {
            byte[] messageBody = (byte[]) request.getParameters()[0];
            long offset = commitLog.appendMessage(messageBody);
            
            RpcResponse response = new RpcResponse();
            response.setRequestId(request.getRequestId());
            response.setResult(offset);
            ctx.writeAndFlush(response);
        }
        // 处理消费者拉取消息
        else if (request.getMethodName().equals("pullMessage")) {
            long offset = (long) request.getParameters()[0];
            int size = (int) request.getParameters()[1];
            byte[] data = commitLog.getMessage(offset, size);
            
            RpcResponse response = new RpcResponse();
            response.setRequestId(request.getRequestId());
            response.setResult(data);
            ctx.writeAndFlush(response);
        }
    }
}