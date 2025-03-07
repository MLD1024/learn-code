package com.example.mq.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

public class MqProtocolDecoder extends ByteToMessageDecoder {
    private static final int HEADER_SIZE = 12;

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        if (in.readableBytes() < HEADER_SIZE) {
            return;
        }

        in.markReaderIndex();
        
        int magic = in.readInt();
        int version = in.readInt();
        int bodyLength = in.readInt();

        if (magic != 0x4D515050) { // MQPP
            in.resetReaderIndex();
            throw new Exception("Invalid protocol magic number");
        }

        if (in.readableBytes() < bodyLength) {
            in.resetReaderIndex();
            return;
        }

        ByteBuf body = in.readRetainedSlice(bodyLength);
        out.add(body);
    }
}