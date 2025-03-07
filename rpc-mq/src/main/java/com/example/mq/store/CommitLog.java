package com.example.mq.store;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

public class CommitLog {
    private final ConcurrentMap<Long, MessageIndex> messageIndex = new ConcurrentHashMap<>();
    private final String storePath;
    private final FileChannel fileChannel;
    private MappedByteBuffer mappedByteBuffer;
    private final AtomicLong wrotePosition = new AtomicLong(0);

    public CommitLog(String storePath) throws IOException {
        this.storePath = storePath;
        File file = new File(storePath + File.separator + "commitlog");
        File parent = file.getParentFile();
        if (!parent.exists()) parent.mkdirs();
        
        this.fileChannel = new RandomAccessFile(file, "rw").getChannel();
        this.mappedByteBuffer = fileChannel.map(
            FileChannel.MapMode.READ_WRITE, 0, 1024 * 1024 * 1024);
    }
    
    public long appendMessage(byte[] data) {
        int dataLength = data.length;
        long currentPos = wrotePosition.getAndAdd(dataLength);
        mappedByteBuffer.position((int) currentPos);
        mappedByteBuffer.put(data);
        messageIndex.put(currentPos, new MessageIndex("commitlog", currentPos, dataLength));
        return currentPos;
    }

    public byte[] getMessage(long offset, int size) {
        MessageIndex index = messageIndex.get(offset);
        if (index == null) return new byte[0];
        
        byte[] data = new byte[index.messageSize];
        mappedByteBuffer.position((int) index.fileOffset);
        mappedByteBuffer.get(data);
        return data;
    }

    private static class MessageIndex {
        String fileName;
        long fileOffset;
        int messageSize;

        MessageIndex(String fileName, long fileOffset, int messageSize) {
            this.fileName = fileName;
            this.fileOffset = fileOffset;
            this.messageSize = messageSize;
        }
    }

    public void shutdown() {
        try {
            mappedByteBuffer.force();
            fileChannel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}