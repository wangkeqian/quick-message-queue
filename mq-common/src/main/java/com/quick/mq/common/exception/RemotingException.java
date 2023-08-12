package com.quick.mq.common.exception;

public class RemotingException extends RuntimeException{
    public RemotingException() {
    }

    public RemotingException(String message) {
        super(message);
    }
}
