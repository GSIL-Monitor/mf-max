package com.mryx.matrix.publish.domain;

import com.mryx.matrix.common.domain.Base;
import lombok.Data;

import java.util.List;

/**
 * @author pengcheng
 * @description
 * @email pengcheng@missfresh.cn
 * @date 2018-11-22 15:42
 **/
@Data
public class RollbackPlanParam extends Base {

    /**
     * 项目ID
     */
    private Integer projectId;

    /**
     * 回滚计划列表
     */
    private List<DeployPlan> rollbackList;

    @Override
    public String toString() {
        return "RollbackPlanParam{" +
                "projectId=" + projectId +
                ", rollbackList=" + rollbackList + ", " + super.toString() +
                '}';
    }
}
