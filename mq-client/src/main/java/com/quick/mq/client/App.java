package com.quick.mq.client;

import com.alibaba.fastjson.JSONObject;
import com.quick.mq.common.exchange.NettyMessage;
import com.quick.mq.common.exchange.Response;
import com.quick.mq.rpc.netty.netty.NettyClient;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class App
{
    public static void main( String[] args ) throws ExecutionException, InterruptedException, TimeoutException {

        for (int i = 0; i < 200; i++) {
            new Thread(new Runnable() {
                @SneakyThrows
                public void run() {
                    NettyClient client = new NettyClient();
                    JSONObject data = new JSONObject();
                    data.put("name", "nihao");
                    NettyMessage message = new NettyMessage(data);
                    Response send = client.send(message);
                    log.info("响应为 {}", send);
                }
            }).start();
        }
    }
}
