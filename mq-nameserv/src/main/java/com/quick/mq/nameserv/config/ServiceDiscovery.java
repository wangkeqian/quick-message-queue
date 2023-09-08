package com.quick.mq.nameserv.config;

import com.quick.mq.common.config.NettyServerConfig;
import com.quick.mq.common.exchange.ConsumerNode;
import com.quick.mq.common.exchange.ServiceNode;
import com.quick.mq.common.extension.SPI;

import java.util.List;

@SPI
public interface ServiceDiscovery {
    /**
     * 发现服务
     * @return
     */
    ServiceNode findServ();

    void register();

    void removeServ();

    List<ConsumerNode> findAllConsumerByTopic(String group, String topic);
}
