package com.quick.mq.common.exchange;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * TODO 请说明此类的作用
 *
 * @author wangkq
 * @date 2023/10/10
 */
@Data
@AllArgsConstructor
public class ConsumerQueueMessage {

    //消息在commitLog的物理偏移量
    long offset;
    //消息总大小
    int size;
    //是否被消费
    int hasConsumed;
    //预留
    long def;
}
