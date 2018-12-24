package com.mryx.matrix.process.web.vo;

import java.util.List;

/**
 * @author pengcheng
 * @description
 * @email pengcheng@missfresh.cn
 * @date 2018-11-02 17:59
 **/
public class ProjectAuditorMappings {

    /**
     * 项目ID
     */
    private Integer projectId;

    /**
     * 项目与用户关系列表
     */
    private List<Auditor> auditors;

    public Integer getProjectId() {
        return projectId;
    }

    public void setProjectId(Integer projectId) {
        this.projectId = projectId;
    }

    public List<Auditor> getAuditors() {
        return auditors;
    }

    public void setAuditors(List<Auditor> auditors) {
        this.auditors = auditors;
    }

    @Override
    public String toString() {
        return "ProjectAuditorMappings{" +
                "projectId=" + projectId +
                ", auditors=" + auditors +
                '}';
    }
}
