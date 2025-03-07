package com.example.rpc.core;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RpcClientHandler extends SimpleChannelInboundHandler<RpcResponse> {
    private static final Map<String, Object> responseMap = new ConcurrentHashMap<>();

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcResponse response) {
        responseMap.put(response.getRequestId(), response.getResult());
    }

    public static Object getResponse(String requestId) {
        while (true) {
            Object result = responseMap.get(requestId);
            if (result != null) {
                responseMap.remove(requestId);
                return result;
            }
        }
    }
}