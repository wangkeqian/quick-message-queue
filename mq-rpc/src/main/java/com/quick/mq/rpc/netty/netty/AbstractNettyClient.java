package com.quick.mq.rpc.netty.netty;

import com.quick.mq.common.exception.RemotingException;
import com.quick.mq.common.exchange.NettyMessage;
import com.quick.mq.common.exchange.Response;
import com.quick.mq.common.exchange.ServiceNode;
import com.quick.mq.common.extension.ExtensionLoader;
import com.quick.mq.nameserv.ServiceDiscovery;
import com.quick.mq.rpc.netty.RemotingRpcExchange;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractNettyClient implements RemotingRpcExchange {

    private final ServiceDiscovery discovery;

    public AbstractNettyClient() {
        try {
            openConnect();
        }catch (Exception e){
            log.error("netty 客户端链接异常");
            close();
            throw new RemotingException();
        }
        discovery = ExtensionLoader.getExtensionLoader(ServiceDiscovery.class).getExtension("zookeeper");
    }


    public final Response send(NettyMessage message) throws InterruptedException, ExecutionException, TimeoutException {
        ServiceNode serv = discovery.findServ();
        return doSend(serv, message);
    }

    protected abstract Response doSend(ServiceNode serv, NettyMessage request)
        throws InterruptedException, ExecutionException, TimeoutException;

    protected abstract void openConnect();
}
