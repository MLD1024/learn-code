package com.example.rpc.core;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RpcServerHandler extends SimpleChannelInboundHandler<RpcRequest> {
    private final Map<String, Object> serviceMap = new ConcurrentHashMap<>();

    public void registerService(String serviceName, Object serviceImpl) {
        serviceMap.put(serviceName, serviceImpl);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcRequest request) throws Exception {
        RpcResponse response = new RpcResponse();
        response.setRequestId(request.getRequestId());

        try {
            Object serviceImpl = serviceMap.get(request.getServiceName());
            Method method = serviceImpl.getClass().getMethod(
                    request.getMethodName(), 
                    request.getParameterTypes());
            Object result = method.invoke(serviceImpl, request.getParameters());
            response.setResult(result);
        } catch (Exception e) {
            response.setException(new Exception("RPC调用失败: " + e.getMessage()));
        }
        ctx.writeAndFlush(response);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}