package com.example.mq.client;

import com.example.mq.broker.RpcRequest;
import com.example.mq.broker.RpcResponse;
import com.example.mq.namesrv.NameServer;

import java.util.List;

public class MQConsumer {
    private final NameServer nameServer;
    private final RpcClient rpcClient;
    private long consumeOffset = 0;

    public MQConsumer(NameServer nameServer) {
        this.nameServer = nameServer;
        this.rpcClient = new RpcClient(5, 60000); // 添加连接池参数：最大连接数5，心跳间隔60秒
    }

    public byte[] pullMessage(String topic) throws Exception {
        // 从NameServer获取Broker地址
        List<NameServer.BrokerData> availableBrokers = nameServer.getAvailableBrokers(topic);
        // 构造拉取请求
        RpcRequest request = new RpcRequest();
        request.setMethodName("pullMessage");
        request.setParameters(new Object[]{consumeOffset, 1024});

        // 调用Broker服务
        RpcResponse response = rpcClient.send(request);

        if (!response.isSuccess()) {
            throw new RuntimeException("Send message failed: " + response.getErrorMessage());
        }
        
        byte[] data = (byte[]) response.getResult();
        if (data != null) {
            consumeOffset += data.length;
        }
        return data;
    }

    public void commitOffset() {
        // TODO 实现消费位点持久化存储
    }
}