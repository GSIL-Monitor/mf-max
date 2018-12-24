package com.mryx.matrix.publish.core.service.impl;

import com.mryx.matrix.publish.core.dao.DeployPlanDao;
import com.mryx.matrix.publish.core.service.DeployPlanService;
import com.mryx.matrix.publish.domain.DeployPlan;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * 发布计划服务
 *
 * @author dinglu
 * @date 2018/10/19
 */
@Service("deployPlanService")
public class DeployPlanServiceImpl implements DeployPlanService {

    @Resource
    DeployPlanDao deployPlanDao;

    @Override
    public DeployPlan getById(Integer id) {
        return deployPlanDao.getById(id);
    }

    @Override
    public List<DeployPlan> listPublishPlanByProjectId(Integer projectId) {
        return deployPlanDao.listPublishPlanByProjectId(projectId);
    }

    @Override
    public List<DeployPlan> listRollbackPlanByProjectId(Integer projectId) {
        return deployPlanDao.listRollbackPlanByProjectId(projectId);
    }

    @Override
    public List<DeployPlan> listByCondition(DeployPlan deployPlan) {
        return deployPlanDao.listByCondition(deployPlan);
    }

    @Override
    public List<DeployPlan> listByPage(DeployPlan deployPlan) {
        return deployPlanDao.listByPage(deployPlan);
    }

    @Override
    public int pageTotal(DeployPlan deployPlan) {
        return deployPlanDao.pageTotal(deployPlan);
    }

    @Override
    public int updateById(DeployPlan deployPlan) {
        return deployPlanDao.updateById(deployPlan);
    }

    @Override
    public int insert(DeployPlan deployPlan) {
        return deployPlanDao.insert(deployPlan);
    }

    @Override
    public int updateStatusToSuspend(DeployPlan deployPlan) {
        return deployPlanDao.updateStatusToSuspend(deployPlan);
    }

}