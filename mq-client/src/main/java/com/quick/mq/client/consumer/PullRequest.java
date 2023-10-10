package com.quick.mq.client.consumer;

public class PullRequest {

    private long cqStartOffset;
    private long cqEndOffset;


    public long getCqEndOffset() {
        return cqEndOffset;
    }

    public void setCqEndOffset(long cqEndOffset) {
        this.cqEndOffset = cqEndOffset;
    }

    public long getCqStartOffset() {
        return cqStartOffset;
    }

    public void setCqStartOffset(long cqStartOffset) {
        this.cqStartOffset = cqStartOffset;
    }
}
