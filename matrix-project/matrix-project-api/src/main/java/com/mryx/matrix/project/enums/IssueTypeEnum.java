package com.mryx.matrix.project.enums;

/**
 * @author pc
 */

public enum IssueTypeEnum {

    REQUIREMENT("10100", "需求", ""),

    STORY("10001", "故事", "创建的 JIRA 软件-请勿编辑或删除。问题类型的用户故事。"),

    BUG("10004", "BUG", "测试过程，维护过程发现影响系统运行的问题。");

    private String code;

    private String name;

    private String desc;

    IssueTypeEnum(String code, String name, String desc) {
        this.code = code;
        this.name = name;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getDesc() {
        return desc;
    }}
