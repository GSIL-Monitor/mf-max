package com.mryx.matrix.publish.domain;

import com.mryx.matrix.common.domain.Base;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author pengcheng
 * @description 推荐计划参数
 * @email pengcheng@missfresh.cn
 * @date 2018-10-24 11:07
 **/
@Data
public class DeployPlanParam extends Base implements Serializable {

    private List<RecommendProjectTask> projectTask;

    private List<DeployPlan> stepList;

    @Override
    public String toString() {
        return "DeployPlanParam [" +
                "projectTask=" + projectTask +
                ", stepList=" + stepList + super.toString() +
                ']';
    }
}
