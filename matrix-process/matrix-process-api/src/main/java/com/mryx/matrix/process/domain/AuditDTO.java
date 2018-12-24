package com.mryx.matrix.process.domain;

import com.mryx.matrix.common.domain.Base;

/**
 * @author pengcheng
 * @description
 * @email pengcheng@missfresh.cn
 * @date 2018-11-02 18:59
 **/
public class AuditDTO extends Base {

    private Integer projectId;

    public Integer getProjectId() {
        return projectId;
    }

    public void setProjectId(Integer projectId) {
        this.projectId = projectId;
    }

    @Override
    public String toString() {
        return "AuditDTO [" +
                "projectId=" + projectId +
                ']';
    }
}
