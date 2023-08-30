package com.quick.mq.nameserv.config;

import com.quick.mq.common.config.NettyServerConfig;
import com.quick.mq.common.exchange.ServiceNode;
import com.quick.mq.common.extension.SPI;

@SPI
public interface ServiceDiscovery {
    /**
     * 发现服务
     * @return
     */
    ServiceNode findServ();

    void register();

    void removeServ();
}
