package com.mryx.matrix.publish.core.service;

import com.mryx.matrix.publish.domain.DeployPlan;

import java.util.List;

/**
 * 发布计划表服务
 *
 * @author dinglu
 * @date 2018/10/19
 */
public interface DeployPlanService {

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
