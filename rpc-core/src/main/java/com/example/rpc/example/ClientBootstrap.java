package com.example.rpc.example;

import com.example.rpc.core.RpcClient;
import com.example.rpc.proxy.RpcProxyFactory;

public class ClientBootstrap {
    public static void main(String[] args) throws Exception {
        RpcClient client = new RpcClient("localhost", 8080);
        client.connect();
        
        HelloService helloService = RpcProxyFactory.createProxy(HelloService.class, client);
        String result = helloService.sayHello("World");
        System.out.println("RPC调用结果: " + result);
        client.close();
    }
}