package com.quick.mq.store;

import com.quick.mq.common.store.BrokerInnerMessage;

public class CommitLog {

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
    public void asyncPutMessage(final BrokerInnerMessage message){




    }

}
