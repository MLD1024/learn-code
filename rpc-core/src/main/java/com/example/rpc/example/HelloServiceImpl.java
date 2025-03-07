package com.example.rpc.example;

public class HelloServiceImpl implements HelloService {
    @Override
    public String sayHello(String name) {
        return "Hello " + name + "!";
    }

    // public java.lang.String sayHello(java.lang.String arg) {
    //     com.example.rpc.core.RpcRequest request = new com.example.rpc.core.RpcRequest();
    //     request.setRequestId("7ad6fea8-275f-49f5-86e4-a70dfcab14c4");
    //     request.setServiceName("com.example.rpc.example.HelloService");
    //     request.setMethodName("sayHello");
    //     request.setParameterTypes(new java.lang.Class[]{java.lang.String.class});
    //     request.setParameters(new Object[]{"World"});
    //     // return (java.lang.String) client.send(request);
    // }


    // public java.lang.String sayHello(java.lang.String arg) {
    //     com.example.rpc.core.RpcRequest request = new com.example.rpc.core.RpcRequest();
    //     request.setRequestId("f3956973-e34c-4ae3-9f50-e19f41d98f42");
    //     request.setServiceName("com.example.rpc.example.HelloService");
    //     request.setMethodName("sayHello");
    //     request.setParameterTypes(new java.lang.Class[]{java.lang.String.class});
    //     request.setParameters(new Object[]{hello});
    //     return (java.lang.String) client.send(request);
    // }
    public java.lang.String sayHello(java.lang.String arg0, com.example.rpc.core.RpcClient client) throws Exception {
        com.example.rpc.core.RpcRequest request = new com.example.rpc.core.RpcRequest();
        request.setRequestId("6ca10281-c3cd-41f2b-b419-49caa426bb35");
        request.setServiceName("com.example.rpc.example.HelloService");
        request.setMethodName("sayHello");
        request.setParameterTypes(new java.lang.Class[]{java.lang.String.class});
        request.setParameters(new Object[]{arg0});
        return (java.lang.String) client.send(request);
    }
}