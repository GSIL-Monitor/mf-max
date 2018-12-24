package com.mryx.matrix.project.enums;

import lombok.Data;

/**
 * @author pengcheng
 * @description 自定义字段枚举类
 * @email pengcheng@missfresh.cn
 * @date 2018-11-13 16:10
 **/
public enum FieldEnum {

    /**
     * 计划分值
     */
    PlanScore("customfield_10300", "计划分值"),

    /**
     * 实际分值
     */
    RealScore("customfield_10107", "实际分值"),

    /**
     * 预计业务收益
     */
    ExpectedEarning("customfield_10305", "预计业务收益"),

    /**
     * 分值完成率
     */
    ScoreComplete("customfield_10301", "分值完成率"),

    /**
     * 分值偏差说明
     */
    ScoreDeviationDesc("customfield_10303", "分值偏差说明"),

    /**
     * 实际业务收益
     */
    RealEarning("customfield_10306", "实际业务收益"),

    /**
     * 收益完成率
     */
    EarningComplete("customfield_10302", "收益完成率"),

    /**
     * 收益偏差说明
     */
    EarningDeviationDesc("customfield_10304", "收益偏差说明"),

    /**
     * Review负责人
     */
    ReviewUser("customfield_10123", "Review负责人"),

    /**
     * 接话评审完成时间
     */
    PlanReviewCompleteDate("customfield_10112", "计划评审完成时间"),

    /**
     * 计划提测时间
     */
    PlanTestDate("customfield_10116", "计划提测时间"),

    /**
     * 计划上线时间
     */
    PlanPublishDate("customfield_10118", "计划上线时间"),

    /**
     * 计划排期时间
     */
    PlanScheduleDate("customfield_10114", "计划排期时间"),

    /**
     * 业务代表
     */
    BusinessRepresentative("customfield_10423", "业务代表"),

    /**
     * 产品经理
     */
    ProductManager("customfield_10202", "产品经理"),

    /**
     * 测试代表
     */
    TestRepresentative("customfield_10204", "测试代表"),

    /**
     * 研发代表
     */
    ResearchRepresentative("customfield_10203", "研发代表"),

    /**
     * 技术文档链接
     */
    TechDocument("customfield_10437", "技术文档链接");


    /**
     * 字段代码
     */
    private String code;

    /**
     * 字段描述
     */
    private String name;

    FieldEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }
}
