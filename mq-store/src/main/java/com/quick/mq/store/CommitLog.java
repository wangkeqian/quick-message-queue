package com.quick.mq.store;

import com.quick.mq.common.exchange.CommitLogMessage;
import com.quick.mq.common.exchange.ConsumerQueueMessage;
import com.quick.mq.common.exchange.Message;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CommitLog {

    private ReentrantLock lock = new ReentrantLock();
    private final String storePath;
    private final MappedFileQueue mappedFileQueue;

    public CommitLog(final DefaultMessageStore defaultMessageStore) {
        this.storePath = defaultMessageStore.getMessageStoreConfig().getStorePathCommitLog();
        this.mappedFileQueue = new MappedFileQueue(this.storePath, defaultMessageStore.getMessageStoreConfig().getCommitLogSize());

    }

    /**
     * commitLog的加载委托给mappedFileQueue
     * @return
     */
    public boolean load() {

        boolean result = this.mappedFileQueue.load();

        return result;
    }

    /**
     * 1.
     * @param message
     */
    public void asyncPutMessage(final Message message){
        lock.lock();

        try {
            MappedFile mappedFile = mappedFileQueue.getLastMappedFile();
            if (mappedFile == null){
                mappedFile = mappedFileQueue.getLastMappedFile(0);
            }
            assert mappedFile != null;
            mappedFile.sendMessage(message);

        }finally {
            lock.unlock();
        }

    }

    public void recover() {
        MappedFile mappedFile = mappedFileQueue.getLastMappedFile();
        //最大消息物理偏移量
        int mappedFileOffset = 0;

        if (mappedFile != null){
            ByteBuffer buffer = mappedFile.getSliceByteBuffer();
            while (true){
                //消息长度
                int dataLength = buffer.getInt();
                if (dataLength <= 0){
                    break;
                }
                byte[] bytesBody = new byte[dataLength];
                //创建时间戳
                long timestamp = buffer.getLong();
                //消息在CommitLog的偏移量
                int offset = buffer.getInt();
                buffer.get(bytesBody, 0 , dataLength);
                mappedFileOffset += dataLength + 4 + 8 + 4;

                log.info("加载CommitLog 消息体 【{}】消息尾巴物理位置 {}" ,new String(bytesBody),mappedFileOffset);
            }
            mappedFile.setWrotePosition(mappedFileOffset);

        }

    }

    public List<CommitLogMessage> captureMessage(List<ConsumerQueueMessage> consumerQueueMessages) {

        List<CommitLogMessage> commitLogMessages = new ArrayList<>();

        for (ConsumerQueueMessage message : consumerQueueMessages) {
            MappedFile mappedFile = mappedFileQueue.getLastMappedFile();
            if (mappedFile != null){
                ByteBuffer buffer = mappedFile.getSliceByteBuffer();
                buffer.position((int) message.getOffset());
                //消息体数据长度
                int dataLength = buffer.getInt();
                //消息存储时间
                long createTime = buffer.getLong();
                //消息在CommitLog的偏移量
                int currentPos = buffer.getInt();
                byte[] bytesBody = new byte[dataLength];
                buffer.get(bytesBody, 0 , dataLength);
                CommitLogMessage commitLogMessage = new CommitLogMessage(dataLength, createTime, currentPos, bytesBody);
                commitLogMessages.add(commitLogMessage);
            }
        }
        return commitLogMessages;
    }
}
