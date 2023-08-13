package com.quick.mq.common.exchange;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class DefaultNettyFuture extends CompletableFuture<Object> {

    private static final Map<String, DefaultNettyFuture> FUTURES = new ConcurrentHashMap<>();

    private DefaultNettyFuture() {
    }

    public static DefaultNettyFuture newFuture(long messageId) {
        DefaultNettyFuture future = new DefaultNettyFuture();
        FUTURES.put(String.valueOf(messageId), future);
        return future;
    }

    public static DefaultNettyFuture getFuture(String requestId) {
        return FUTURES.get(requestId);
    }

    public static void sent(long msgId, Object result) {
        DefaultNettyFuture future = FUTURES.remove(String.valueOf(msgId));
        if (Objects.nonNull(future)) {
            future.complete(result);
        } else {
            throw new IllegalStateException("Connection lost");
        }
    }

    public static void destroy() {
        FUTURES.clear();
    }
}
