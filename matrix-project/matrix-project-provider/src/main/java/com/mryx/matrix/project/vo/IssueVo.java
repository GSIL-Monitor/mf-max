package com.mryx.matrix.project.vo;

import com.mryx.matrix.project.domain.Issue;
import lombok.Data;

/**
 * @author pengcheng
 * @description
 * @email pengcheng@missfresh.cn
 * @date 2018-12-13 12:10
 **/
@Data
public class IssueVo extends Issue {

    /**
     * 检索条件
     */
    private String query;

    /**
     * matrix 任务类型
     */
    private String projectType;
}
