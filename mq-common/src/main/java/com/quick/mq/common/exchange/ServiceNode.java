package com.quick.mq.common.exchange;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class ServiceNode {
    /**
     * 服务地址
     */
    private String host;
    /**
     * 端口
     */
    private int port;

    /**
     * 是否可用
     */
    private boolean enable;
}
