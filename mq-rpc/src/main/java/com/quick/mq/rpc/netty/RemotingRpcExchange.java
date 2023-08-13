package com.quick.mq.rpc.netty;

import com.quick.mq.common.exchange.NettyMessage;
import com.quick.mq.common.exchange.Response;

public interface RemotingRpcExchange {
    Response send(NettyMessage request) throws Exception;

    void close();
}
