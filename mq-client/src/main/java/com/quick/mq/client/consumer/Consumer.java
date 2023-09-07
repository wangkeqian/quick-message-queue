package com.quick.mq.client.consumer;

public interface Consumer {
    void start() throws InterruptedException;


    void shutdown();
}
