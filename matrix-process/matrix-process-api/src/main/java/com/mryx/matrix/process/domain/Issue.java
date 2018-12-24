package com.mryx.matrix.process.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * @author pengcheng
 * @description
 * @email pengcheng@missfresh.cn
 * @date 2018-12-12 18:05
 **/
@Data
public class Issue {

    /**
     * 需求主键ID
     */
    private Integer id;

    /**
     * Jira需求ID
     */
    private String jiraIssueId;

    /**
     * Jira关键字
     */
    private String key;

    /**
     * 关联项目ID
     */
    private Integer projectId;

    /**
     * Jira项目ID
     */
    private String jiraProjectId;

    /**
     * 创建用户
     */
    private String createUser;

    /**
     * 修改用户
     */
    private String modifyUser;

    /**
     * 概要
     */
    private String summary;

    /**
     * 类型
     */
    private String issueType;

    /**
     * 描述
     */
    private String description;

    /**
     * 优先级
     */
    private Integer priority;

    /**
     * 经办人
     */
    private String assignee;

    /**
     * 报告人
     */
    private String reporter;

    /**
     * 状态
     */
    private String issueStatus;

    /**
     * 创建时间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date gmtCreated;

    /**
     * 修改时间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date gmtModified;

    /**
     * 删除标识，1:正常|0:删除
     */
    private Integer delFlag;

    /**
     * 计划分值
     */
    private Float planScore;

    /**
     * 预计业务收益率
     */
    private Float expectedEarning;

    /**
     * 分值完成率
     */
    private Float scoreComplete;

    /**
     * 分值偏差说明
     */
    private String scoreDeviationDesc;

    /**
     * 实际业务收益
     */
    private Float realEarning;

    /**
     * 收益完成率
     */
    private Float earningComplete;

    /**
     * 收益偏差说明
     */
    private String earningDeviationDesc;
}
