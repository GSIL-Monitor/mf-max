package com.mryx.matrix.process.web.vo;

import com.mryx.matrix.process.domain.Project;

import java.util.List;

/**
 * @author pengcheng
 * @description
 * @email pengcheng@missfresh.cn
 * @date 2018-11-02 17:58
 **/
public class AuditorList {

    private List<Auditor> superAuditors;

    private List<ProjectAuditorMappings> projects;

    public List<Auditor> getSuperAuditors() {
        return superAuditors;
    }

    public void setSuperAuditors(List<Auditor> superAuditors) {
        this.superAuditors = superAuditors;
    }

    public List<ProjectAuditorMappings> getProjects() {
        return projects;
    }

    public void setProjects(List<ProjectAuditorMappings> projects) {
        this.projects = projects;
    }

    @Override
    public String toString() {
        return "AuditorList[" +
                "superAuditors=" + superAuditors +
                ", projects=" + projects +
                ']';
    }
}
