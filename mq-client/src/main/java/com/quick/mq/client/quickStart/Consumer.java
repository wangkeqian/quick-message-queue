package com.quick.mq.client.quickStart;


import com.quick.mq.client.consumer.DefaultConsumer;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Consumer {

    public static void main(String[] args) throws InterruptedException {
        DefaultConsumer consumer = new DefaultConsumer();

        consumer.setNameServ("101.33.248.235:2181");
        consumer.subscribe("test_v2");

        consumer.start();

    }
}
