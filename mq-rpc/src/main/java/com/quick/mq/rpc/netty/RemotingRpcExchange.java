package com.quick.mq.rpc.netty;

import com.quick.mq.common.exchange.Message;
import com.quick.mq.common.exchange.Response;

public interface RemotingRpcExchange {
    Response send(Message request) throws Exception;

    void close();
}
