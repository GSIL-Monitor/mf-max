package com.mryx.matrix.publish.enums;

/**
 * Lb动作
 *
 * @author supeng
 * @date 2018/10/28
 */
public enum LbAction {
    /**
     * 挂流量
     */
    ON(1, "挂流量"),
    /**
     * 摘流量
     */
    OFF(0, "摘流量");
    /**
     * 编码
     */
    private int code;
    /**
     * 描述
     */
    private String description;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    LbAction(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public static LbAction getLbActionByName(String name) {
        if (name != null) {
            for (LbAction lbAction : LbAction.values()) {
                if (name.equals(lbAction.name())) {
                    return lbAction;
                }
            }
        }
        return null;
    }
}
