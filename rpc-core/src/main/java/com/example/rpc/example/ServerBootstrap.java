package com.example.rpc.example;

import com.example.rpc.server.RpcServer;
import com.example.rpc.server.ServiceRegistry;

public class ServerBootstrap {
    public static void main(String[] args) throws Exception {
        // 注册服务实现
        ServiceRegistry.registerService(HelloService.class.getName(), new HelloServiceImpl());
        
        // 启动RPC服务器
        new RpcServer(8080).start();
    }
}