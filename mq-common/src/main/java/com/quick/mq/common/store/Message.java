package com.quick.mq.common.store;

import cn.hutool.core.util.ObjectUtil;
import com.quick.mq.common.message.MessageConst;

import java.util.HashMap;
import java.util.Map;

public abstract class Message {


    long msgId;
    String topic;
    byte[] body;
    private final Map<String, String> properties = new HashMap<>();
    long createTimestamp;
    int flag = 0;
    String productClientId;

    /**
     * 获取延迟队列时间（毫秒）
     */
    public long getDelayTimestamp(){
        String level = properties.get(MessageConst.PROPERTY_DELAY_TIME_LEVEL);
        if (ObjectUtil.isNotEmpty(level)){
            return Long.parseLong(level);
        }
        return 0;
    }
}
