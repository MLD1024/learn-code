package com.example.mq.client;

import com.example.mq.broker.RpcRequest;
import com.example.mq.broker.RpcResponse;
import com.example.mq.config.BrokerConfig;
import com.example.mq.namesrv.NameServer;

public class MQProducer {
    private final NameServer nameServer;
    private final RpcClient rpcClient;

    public MQProducer(NameServer nameServer) {
        this.nameServer = nameServer;
        this.rpcClient = new RpcClient(1000000, 60000,nameServer); // 保持与最新构造函数参数一致
    }

    public void send(String topic, byte[] body) throws Exception {
        // 从NameServer获取Broker地址
        NameServer.BrokerData broker = nameServer.pickupBrokers(topic).get(0);
        
        // 构造RPC请求
        RpcRequest request = new RpcRequest();
        request.setRequestId(String.valueOf(System.currentTimeMillis()));
        request.setTopic(topic);
        request.setParameters(new Object[]{body});
        request.setMethodName("sendMessage");
        // 调用Broker服务
        RpcResponse response = rpcClient.send(request);
        
        if (!response.isSuccess()) {
            throw new RuntimeException("Send message failed: " + response.getErrorMessage());
        }
    }

    public static void main(String[] args) throws Exception {
        NameServer nameServer = new NameServer();
        nameServer.registerBroker("default_topic",
                BrokerConfig.brokerName, BrokerConfig.brokerIp + ":" + BrokerConfig.listenPort);
        MQProducer mqProducer = new MQProducer(nameServer);
        mqProducer.send("default_topic", "test".getBytes());

    }
}