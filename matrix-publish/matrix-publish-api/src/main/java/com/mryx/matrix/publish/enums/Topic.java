package com.mryx.matrix.publish.enums;

/**
 * Mq Topic
 *
 * @author supeng
 * @date 2018/11/17
 */
public enum Topic {
    AppStart("MatrixPublishStartApp","启动应用");

    private String topic;
    private String description;

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    Topic(String topic, String description) {
        this.topic = topic;
        this.description = description;
    }
}
