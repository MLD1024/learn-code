package com.example.mq.codec;

import com.example.mq.broker.RpcRequest;
import com.example.mq.broker.RpcResponse;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;

public class MqProtocolEncoder extends MessageToByteEncoder<Object> {
    private static final Logger logger = LoggerFactory.getLogger(MqProtocolEncoder.class);

    @Override
    protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) {
        // 添加消息类型标识
        int messageType = 0;
        if (msg instanceof RpcRequest) {
            messageType = 1;
        } else if (msg instanceof RpcResponse) {
            messageType = 2;
        }
        
        byte[] payload = msg.toString().getBytes(StandardCharsets.UTF_8);
        
        // 协议格式：类型(4字节) + 长度(4字节) + 内容
        out.writeInt(messageType);
        out.writeInt(payload.length);
        out.writeBytes(payload);
        
        logger.info("编码消息完成 type={} length={}", messageType, payload.length);
    }
}