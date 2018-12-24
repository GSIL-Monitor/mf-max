package com.mryx.matrix.publish.domain;

import com.mryx.matrix.common.domain.Base;

import java.io.Serializable;
import java.util.List;

/**
 *
 * @author dinglu
 * @date 2018/9/30
 */
public class RollBackParam extends Base implements Serializable {

    private static final long serialVersionUID = 8321488463081733693L;
    private List<ProjectTask> projectTaskList;

    public List<ProjectTask> getProjectTaskList() {
        return projectTaskList;
    }

    public void setProjectTaskList(List<ProjectTask> projectTaskList) {
        this.projectTaskList = projectTaskList;
    }

    @Override
    public String toString() {
        return "RollBackParam{" +
                "projectTaskList=" + projectTaskList +
                '}';
    }
}
