package com.quick.mq.rpc.netty;

import com.quick.mq.common.exception.RemotingException;
import com.quick.mq.common.exchange.NettyRequest;
import com.quick.mq.common.exchange.ServiceNode;
import com.quick.mq.common.extension.ExtensionLoader;
import com.quick.mq.nameserv.ServiceDiscovery;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractNetty implements RemotingRpcExchange {

    private final ServiceDiscovery discovery;

    public AbstractNetty() {
        try {
            doOpenLink();
        }catch (Exception e){
            log.error("netty 客户端链接异常");
            close();
            throw new RemotingException();
        }
        discovery = ExtensionLoader.getExtensionLoader(ServiceDiscovery.class).getExtension("zookeeper");
    }


    public final void send(NettyRequest request) {

        ServiceNode serv = discovery.findServ();

        doSend(serv ,request);
    }

    protected abstract void doSend(ServiceNode serv, NettyRequest request);

    protected abstract void doOpenLink();
}
