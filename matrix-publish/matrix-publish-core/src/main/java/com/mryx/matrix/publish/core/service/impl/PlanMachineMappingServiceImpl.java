package com.mryx.matrix.publish.core.service.impl;

import com.mryx.matrix.publish.core.dao.PlanMachineMappingDao;
import com.mryx.matrix.publish.core.service.PlanMachineMappingService;
import com.mryx.matrix.publish.domain.PlanMachineMapping;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * 发布计划机器关联服务
 *
 * @author dinglu
 * @date 2018/10/19
 */

@Service("planMachineMappingService")
public class PlanMachineMappingServiceImpl implements PlanMachineMappingService {

    @Resource
    PlanMachineMappingDao planMachineMappingDao;

    @Override
    public PlanMachineMapping getById(Integer id) {
        return planMachineMappingDao.getById(id);
    }

    @Override
    public List<PlanMachineMapping> listPublishByPlanId(Integer planId) {
        return planMachineMappingDao.listPublishByPlanId(planId);
    }

    @Override
    public List<PlanMachineMapping> listRollbackByPlanId(Integer planId) {
        return planMachineMappingDao.listRollbackByPlanId(planId);
    }

    @Override
    public List<PlanMachineMapping> listByCondition(PlanMachineMapping planMachineMapping) {
        return planMachineMappingDao.listByCondition(planMachineMapping);
    }

    @Override
    public List<PlanMachineMapping> listByPage(PlanMachineMapping planMachineMapping) {
        return planMachineMappingDao.listByPage(planMachineMapping);
    }

    @Override
    public int pageTotal(PlanMachineMapping planMachineMapping) {
        return planMachineMappingDao.pageTotal(planMachineMapping);
    }

    @Override
    public int updateById(PlanMachineMapping planMachineMapping) {
        return planMachineMappingDao.updateById(planMachineMapping);
    }

    @Override
    public int insert(PlanMachineMapping planMachineMapping) {
        return planMachineMappingDao.insert(planMachineMapping);
    }

    @Override
    public int updateByIp(PlanMachineMapping planMachineMapping) {
        return planMachineMappingDao.updateByIp(planMachineMapping);
    }

    @Override
    public boolean checkPublishComplete(PlanMachineMapping planMachineMapping) {
        boolean complete = false;
        int count = planMachineMappingDao.countUnpublishIps(planMachineMapping);
        if (count <= 0) {
            complete = true;
        }
        return complete;
    }

    @Override
    public int updateTagAndStstus(PlanMachineMapping planMachineMapping) {
        return planMachineMappingDao.updateTagAndStstus(planMachineMapping);
    }
}
