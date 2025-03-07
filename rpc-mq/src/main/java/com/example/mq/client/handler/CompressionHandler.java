package com.example.mq.client.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;
import io.netty.handler.codec.compression.ZlibCodecFactory;
import io.netty.handler.codec.compression.ZlibDecoder;
import io.netty.handler.codec.compression.ZlibWrapper;

import java.util.List;

public class CompressionHandler extends MessageToMessageCodec<ByteBuf, ByteBuf> {
    @Override
    protected void encode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
        ByteBuf compressed = ZlibCodecFactory.newZlibEncoder(ctx.alloc(), ZlibWrapper.GZIP, 6).compress(msg);
        out.add(compressed);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
        ZlibDecoder zlibDecoder = ZlibCodecFactory.newZlibDecoder(ZlibWrapper.GZIP);
        zlibDecoder.
        ByteBuf decompressed = zlibDecoder.decode(ctx, msg);
        out.add(decompressed);
    }
}