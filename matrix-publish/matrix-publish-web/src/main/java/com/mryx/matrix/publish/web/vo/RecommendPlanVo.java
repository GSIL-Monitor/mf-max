package com.mryx.matrix.publish.web.vo;

import com.mryx.matrix.publish.domain.DeployPlan;

import java.io.Serializable;
import java.util.List;

/**
 * @author pengcheng
 * @description 推荐计划VO
 * @email pengcheng@missfresh.cn
 * @date 2018-10-24 16:25
 **/
public class RecommendPlanVo implements Serializable {

    private List<DeployPlanVo> stepList;

    public List<DeployPlanVo> getStepList() {
        return stepList;
    }

    public void setStepList(List<DeployPlanVo> stepList) {
        this.stepList = stepList;
    }
}
