package com.mryx.matrix.project.domain;

import lombok.Data;

/**
 * @author pengcheng
 * @description 组件对象
 * @email pengcheng@missfresh.cn
 * @date 2018-12-10 16:37
 **/
@Data
public class JiraComponent {
    private String self;
    private String id;
    private String name;
    private String assigneeType;
    private String realAssigneeType;
    private String isAssigneeTypeValid;
    private String project;
    private String projectId;
}
