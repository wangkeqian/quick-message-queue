package com.quick.mq.client.quickStart;

import com.alibaba.fastjson.JSONObject;
import com.quick.mq.common.exchange.Message;
import com.quick.mq.common.exchange.Response;
import com.quick.mq.rpc.netty.netty.NettyClient;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Producer {
    public static void main( String[] args ) {


//        for (int i = 1001; i <= 2000; i++) {
//            NettyClient client = new NettyClient();
//            JSONObject data = new JSONObject();
//            data.put("name", "消息_" + i);
//            String topic = "test_v2";
//            Message message = new Message(topic ,data);
//            Response send = client.send(message);
//            log.info("响应为 {}", send);
//        }
        for (int i = 3001; i <= 10000; i++) {
            final int t = i;
            new Thread(new Runnable() {
                @SneakyThrows
                public void run() {
                    NettyClient client = new NettyClient();
                    JSONObject data = new JSONObject();
                    data.put("name", "消息_" + t);
                    String topic = "test_v2";
                    Message message = new Message(topic ,data);
                    Response send = client.send(message);
                    log.info("响应为 {}", send);
                }
            }).start();
        }
    }
}
