package com.quick.mq.common.exchange;

public class PullRequest {

    private int cqStartOffset;
    private int cqEndOffset;

    private String topic;
    private int queueId;

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public int getQueueId() {
        return queueId;
    }

    public void setQueueId(int queueId) {
        this.queueId = queueId;
    }

    public int getCqEndOffset() {
        return cqEndOffset;
    }

    public void setCqEndOffset(int cqEndOffset) {
        this.cqEndOffset = cqEndOffset;
    }

    public int getCqStartOffset() {
        return cqStartOffset;
    }

    public void setCqStartOffset(int cqStartOffset) {
        this.cqStartOffset = cqStartOffset;
    }
}
