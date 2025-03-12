package com.example.mq.test;

import com.example.mq.broker.RpcRequest;
import com.example.mq.client.RpcClient;
import com.example.mq.namesrv.NameServer;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

public class MessageFlowTest {
//
//     @Before
//     public void setup() {
//         nameServer = new NameServer(Arrays.asList("127.0.0.1:8080", "127.0.0.1:8081"));
//         client = new RpcClient(3, 5000, new BrokerConfig(), nameServer);
//     }
//
//     @Test
//     public void testNormalMessageFlow() throws Exception {
//         RpcRequest request = new RpcRequest("sendMessage", new Object[]{"testMsg"});
//         Object result = client.send(request, "producer");
//         // 验证消息接收结果
//     }
//
//     @Test
//     public void testRetryOnTimeout() {
//         // 模拟服务器响应超时
//         RpcRequest request = new RpcRequest("sendMessage", new Object[]{"timeoutMsg"});
//         // 验证重试机制
//     }
//
//     @Test
//     public void testFailover() {
//         // 模拟主节点故障
//         RpcRequest request = new RpcRequest("sendMessage", new Object[]{"failoverMsg"});
//         // 验证故障转移
//     }
}