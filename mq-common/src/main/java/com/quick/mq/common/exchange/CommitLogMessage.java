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
public class CommitLogMessage {
    //消息体数据长度
    int dataLength;
    //消息存储时间
    long createTime;
    //消息在CommitLog的偏移量
    int currentPos;
    byte[] bytesBody;
}
