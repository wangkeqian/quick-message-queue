package com.quick.mq.client;

import com.quick.mq.rpc.netty.netty.NettyClient;

/**
 * Hello world!
 *
 */
public class App
{
    public static void main( String[] args )
    {
        NettyClient client = new NettyClient();
        client.send(null);
    }
}
