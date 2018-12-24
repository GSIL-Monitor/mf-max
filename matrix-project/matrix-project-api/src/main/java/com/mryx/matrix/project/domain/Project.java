package com.mryx.matrix.project.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.mryx.matrix.common.domain.Page;
import lombok.Data;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

/**
 * 项目
 *
 * @author supeng
 * @date 2018/11/11
 */
@Data
public class Project extends Page {

    /**
     * 主键ID
     */
    private Integer id;

    /**
     * Jira项目ID
     */
    private String jiraProjectId;

    /**
     * 项目关键字
     */
    private String key;

    /**
     * 项目名称
     */
    private String name;

    /**
     * 项目描述
     */
    private String description;

    /**
     * 项目负责人
     */
    private String lead;

    /**
     * 问题类型,多种类型用逗号分隔
     */
    private String assigneeType;

    /**
     * 创建用户
     */
    private String createUser;

    /**
     * 修改用户
     */
    private String modifyUser;

    /**
     * 创建时间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date gmtCreated;

    /**
     * 修改时间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Timestamp gmtModified;

    /**
     * 项目中包含的需求列表
     */
    private List<Issue> issueList;
}
