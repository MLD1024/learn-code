package com.example.mq.client;

import com.example.mq.broker.RpcRequest;
import com.example.mq.broker.RpcResponse;
import com.example.mq.namesrv.NameServer;

public class MQProducer {
    private final NameServer nameServer;
    private final RpcClient rpcClient;

    public MQProducer(NameServer nameServer) {
        this.nameServer = nameServer;
        this.rpcClient = new RpcClient(1000000, 60000); // 保持与最新构造函数参数一致
    }

    public void send(String topic, byte[] body) throws Exception {
        // 从NameServer获取Broker地址
        NameServer.BrokerData broker = nameServer.pickupBrokers(topic).get(0);
        
        // 构造RPC请求
        RpcRequest request = new RpcRequest();
        request.setMethodName("sendMessage");
        request.setParameters(new Object[]{body});
        
        // 调用Broker服务
        RpcResponse response = rpcClient.send(request);
        
        if (!response.isSuccess()) {
            throw new RuntimeException("Send message failed: " + response.getErrorMessage());
        }
    }
}