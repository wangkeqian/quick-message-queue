package com.quick.mq.common.exchange;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PullMessageRequest {

    private String topic;
    private int queueId;

}
