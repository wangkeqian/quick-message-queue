package com.quick.mq.rpc.netty;

import com.quick.mq.common.exchange.NettyRequest;
import com.quick.mq.common.exchange.Request;

public interface RemotingRpcExchange {
    void send(NettyRequest request) throws Exception;

    void close();
}
