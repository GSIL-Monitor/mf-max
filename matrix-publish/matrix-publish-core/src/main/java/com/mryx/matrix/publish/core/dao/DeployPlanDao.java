package com.mryx.matrix.publish.core.dao;

import com.mryx.matrix.publish.domain.DeployPlan;

import java.util.List;

/**
 * @author dinglu
 * @date 2018/10/19
 */

/*发布计划表*/
public interface DeployPlanDao {

    DeployPlan getById(Integer id);

    List<DeployPlan> listPublishPlanByProjectId(Integer projectId);

    List<DeployPlan> listRollbackPlanByProjectId(Integer projectId);

    List<DeployPlan> listByCondition(DeployPlan deployPlan);

    List<DeployPlan> listByPage(DeployPlan deployPlan);

    int pageTotal(DeployPlan deployPlan);

    int updateById(DeployPlan deployPlan);

    int insert(DeployPlan deployPlan);

    int updateStatusToSuspend(DeployPlan deployPlan);

}
