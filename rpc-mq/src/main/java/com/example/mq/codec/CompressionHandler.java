package com.example.mq.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;
import java.util.List;
import java.util.zip.*;

public class CompressionHandler extends MessageToMessageCodec<ByteBuf, ByteBuf> {
    private static final int COMPRESSION_THRESHOLD = 512;
    private static final int COMPRESSION_LEVEL = Deflater.DEFAULT_COMPRESSION;

    @Override
    protected void encode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) {
        boolean shouldCompress = msg.readableBytes() >= COMPRESSION_THRESHOLD;
        
        try (Deflater deflater = new Deflater(COMPRESSION_LEVEL)) {
            byte[] input = new byte[msg.readableBytes()];
            msg.readBytes(input);
            
            if (shouldCompress) {
                deflater.setInput(input);
                deflater.finish();
                
                ByteBuf compressed = ctx.alloc().buffer();
                compressed.writeByte(1); // 压缩标志
                compressed.writeInt(input.length); // 原始数据长度
                
                byte[] buffer = new byte[1024];
                while (!deflater.finished()) {
                    int count = deflater.deflate(buffer);
                    compressed.writeBytes(buffer, 0, count);
                }
                out.add(compressed);
            } else {
                ByteBuf output = ctx.alloc().buffer();
                output.writeByte(0); // 未压缩标志
                output.writeBytes(input);
                out.add(output);
            }
        } catch (Exception e) {
            ctx.fireExceptionCaught(new Exception("压缩失败: " + e.getMessage(), e));
            ctx.close();
        }
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) {
        try {
            int compressFlag = msg.readByte();
            
            if (compressFlag == 1) {
                int originalSize = msg.readInt();
                
                try (Inflater inflater = new Inflater()) {
                    byte[] compressedData = new byte[msg.readableBytes()];
                    msg.readBytes(compressedData);
                    inflater.setInput(compressedData);

                    ByteBuf output = ctx.alloc().buffer(originalSize);
                    byte[] buffer = new byte[1024];
                    while (!inflater.finished()) {
                        int count = inflater.inflate(buffer);
                        output.writeBytes(buffer, 0, count);
                    }
                    out.add(output);
                }
            } else {
                ByteBuf output = ctx.alloc().buffer(msg.readableBytes());
                output.writeBytes(msg);
                out.add(output);
            }
        } catch (DataFormatException e) {
            ctx.fireExceptionCaught(new Exception("解压失败: " + e.getMessage(), e));
            ctx.close();
        } catch (Exception e) {
            ctx.fireExceptionCaught(new Exception("协议解析错误: " + e.getMessage(), e));
            ctx.close();
        }
    }
}