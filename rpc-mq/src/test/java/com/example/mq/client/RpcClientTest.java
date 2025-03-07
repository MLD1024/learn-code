package com.example.mq.client;

import com.example.mq.namesrv.NameServer;

class RpcClientTest {
    private RpcClient rpcClient;
    private NameServer nameServer;

    // @BeforeEach
    // void setUp() {
    //     nameServer = Mockito.mock(NameServer.class);
    //     when(nameServer.getAvailableBrokers()).thenReturn(Arrays.asList("127.0.0.1:8080"));
    //     rpcClient = new RpcClient(5, 3000, new BrokerConfig(), nameServer);
    // }
    //
    // @Test
    // void should_send_message_successfully() throws Exception {
    //     RpcRequest request = new RpcRequest("sendMessage", new Object[]{"test message"});
    //     assertDoesNotThrow(() -> rpcClient.send(request, "127.0.0.1:8080"));
    // }
    //
    // @Test
    // void should_throw_exception_when_timeout() {
    //     RpcRequest request = new RpcRequest("invalidMethod", new Object[0]);
    //     assertThrows(RuntimeException.class, () -> rpcClient.send(request, "127.0.0.1:8080"));
    // }
    //
    // @Test
    // void should_correctly_encode_decode_message() throws Exception {
    //     // 构造复杂消息体
    //     MessagePayload payload = new MessagePayload("test", "Hello World", System.currentTimeMillis());
    //
    //     // 测试编码解码
    //     MqProtocolEncoder encoder = new MqProtocolEncoder();
    //     byte[] encoded = encoder.encode(payload);
    //
    //     MqProtocolDecoder decoder = new MqProtocolDecoder();
    //     MessagePayload decoded = decoder.decode(encoded);
    //
    //     assertEquals(payload.getTopic(), decoded.getTopic());
    //     assertEquals(payload.getContent(), decoded.getContent());
    // }
    //
    // @Test
    // void should_retry_and_clean_pool_when_network_failure() throws Exception {
    //     // 模拟两个不可用Broker
    //     when(nameServer.getAvailableBrokers()).thenReturn(Arrays.asList("127.0.0.1:8080", "127.0.0.1:8081"));
    //
    //     RpcRequest request = new RpcRequest("sendMessage", new Object[]{"network test"});
    //
    //     // 验证最终抛出所有Broker不可用异常
    //     Exception exception = assertThrows(RuntimeException.class,
    //         () -> rpcClient.send(request, "127.0.0.1:8080"));
    //
    //     assertTrue(exception.getMessage().contains("所有Broker不可用"));
    //     // 验证连接池已清理失效连接
    //     assertEquals(0, rpcClient.getConnectionPoolSize());
    // }
    //
    // @Test
    // void should_switch_to_backup_broker_when_primary_fails() {
    //     // 准备两个Broker地址
    //     when(nameServer.getAvailableBrokers()).thenReturn(Arrays.asList("127.0.0.1:8080", "127.0.0.1:8081"));
    //
    //     // 模拟主Broker连接失败
    //     RpcRequest request = new RpcRequest("sendMessage", new Object[]{"failover test"});
    //     try {
    //         rpcClient.send(request, "127.0.0.1:8080");
    //     } catch (Exception e) {
    //         // 验证是否尝试连接备用Broker
    //         verify(nameServer, times(2)).getAvailableBrokers();
    //         assertTrue(e.getMessage().contains("127.0.0.1:8081"));
    //     }
    // }
}