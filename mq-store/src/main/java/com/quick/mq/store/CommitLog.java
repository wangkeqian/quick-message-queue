package com.quick.mq.store;

import com.quick.mq.common.store.BrokerInnerMessage;

public class CommitLog {


    public CommitLog() {
        //加载CommitLog文件
        this.load();
    }

    private void load() {

    }

    /**
     * 1.
     * @param message
     */
    public void asyncPutMessage(final BrokerInnerMessage message){




    }

}
