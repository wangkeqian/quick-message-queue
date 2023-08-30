package com.quick.mq.store;

import com.quick.mq.common.exchange.NettyMessage;
import com.quick.mq.common.store.BrokerInnerMessage;
import java.util.concurrent.locks.ReentrantLock;

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
    public void asyncPutMessage(final NettyMessage message){
        lock.lock();

        try {
            MappedFile mappedFile = mappedFileQueue.getLastMappedFile();
            if (mappedFile == null){
                mappedFileQueue.getLastMappedFile(0);
            }
            assert mappedFile != null;
            mappedFile.sendMessage(message);


        }finally {
            lock.unlock();
        }

    }

}
