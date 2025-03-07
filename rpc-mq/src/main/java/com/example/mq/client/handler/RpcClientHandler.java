package com.example.mq.client.handler;

import com.example.mq.broker.RpcResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.concurrent.ConcurrentHashMap;

public class RpcClientHandler extends SimpleChannelInboundHandler<RpcResponse> {
    public static final ConcurrentHashMap<String, RpcResponse> responseMap = new ConcurrentHashMap<>();

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcResponse response) {
        responseMap.put(response.getRequestId(), response);
    }

    public static RpcResponse getResponse(String requestId) {
        while (true) {
            RpcResponse response = responseMap.get(requestId);
            if (response != null) {
                return responseMap.remove(requestId);
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}