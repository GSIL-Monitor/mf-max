package com.mryx.matrix.publish.enums;

/**
 * Agent状态
 *
 * @author supeng
 * @date 2018/09/17
 */
public enum AgentStatus {
    OFFLINE(0, "离线"),
    ONLINE(1, "在线");
    /**
     * 值
     */
    private Integer value;
    /**
     * 描述
     */
    private String command;

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    AgentStatus(Integer value, String command) {
        this.value = value;
        this.command = command;
    }
}
