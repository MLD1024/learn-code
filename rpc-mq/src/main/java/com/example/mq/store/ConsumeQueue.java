package com.example.mq.store;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.concurrent.atomic.AtomicLong;

public class ConsumeQueue {
    private static final int CQ_STORE_UNIT_SIZE = 20; // 8字节commitlog偏移量 + 4字节消息大小 + 8字节tagHash
    private final String storePath;
    private MappedByteBuffer mappedByteBuffer;
    private AtomicLong wrotePosition = new AtomicLong(0);

    public ConsumeQueue(String topic, int queueId) {
        this.storePath = "store/consumequeue/" + topic + "/" + queueId + "/";
        try {
            File file = new File(storePath + "00000000000000000000");
            File parent = file.getParentFile();
            if (!parent.exists()) {
                parent.mkdirs();
            }
            FileChannel fileChannel = new RandomAccessFile(file, "rw").getChannel();
            this.mappedByteBuffer = fileChannel.map(
                FileChannel.MapMode.READ_WRITE, 0, 1024 * 1024 * 100);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void putMessageOffset(long commitLogOffset, int size, long tagHashCode) {
        int currentPos = (int) wrotePosition.getAndAdd(CQ_STORE_UNIT_SIZE);
        mappedByteBuffer.position(currentPos);
        mappedByteBuffer.putLong(commitLogOffset);
        mappedByteBuffer.putInt(size);
        mappedByteBuffer.putLong(tagHashCode);
    }

    public long getMessageOffset(int index) {
        int pos = index * CQ_STORE_UNIT_SIZE;
        return mappedByteBuffer.getLong(pos);
    }
}