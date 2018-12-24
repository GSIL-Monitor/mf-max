package com.mryx.matrix.publish.core.service.impl;

import com.mryx.matrix.publish.core.dao.DeployPlanRecordDao;
import com.mryx.matrix.publish.core.service.DeployPlanRecordService;
import com.mryx.matrix.publish.domain.DeployPlanRecord;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * 发布计划发布记录服务
 * @author dinglu
 * @date 2018/10/19
 */
@Service("deployPlanRecordService")
public class DeployPlanRecordServiceImpl implements DeployPlanRecordService{

    @Resource
    DeployPlanRecordDao deployPlanRecordDao;

    @Override
    public DeployPlanRecord getById(Integer id) {
        return deployPlanRecordDao.getById(id);
    }

    @Override
    public List<DeployPlanRecord> getByPlanId(Integer planId) {
        return deployPlanRecordDao.getByPlanId(planId);
    }

    @Override
    public List<DeployPlanRecord> listByCondition(DeployPlanRecord deployPlanRecord) {
        return deployPlanRecordDao.listByCondition(deployPlanRecord);
    }

    @Override
    public List<DeployPlanRecord> listByPage(DeployPlanRecord deployPlanRecord) {
        return deployPlanRecordDao.listByPage(deployPlanRecord);
    }

    @Override
    public int pageTotal(DeployPlanRecord deployPlanRecord) {
        return deployPlanRecordDao.pageTotal(deployPlanRecord);
    }

    @Override
    public int updateById(DeployPlanRecord deployPlanRecord) {
        return deployPlanRecordDao.updateById(deployPlanRecord);
    }

    @Override
    public int insert(DeployPlanRecord deployPlanRecord) {
        return deployPlanRecordDao.insert(deployPlanRecord);
    }

    @Override
    public int updateStatusToSuspend(DeployPlanRecord deployPlanRecord) {
        return deployPlanRecordDao.updateStatusToSuspend(deployPlanRecord);
    }

    @Override
    public DeployPlanRecord getLastIdByPlanId(DeployPlanRecord deployPlanRecord) {
        return deployPlanRecordDao.getLastIdByPlanId(deployPlanRecord);
    }

    @Override
    public DeployPlanRecord getLastByPlanId(Integer planId) {
        return deployPlanRecordDao.getLastByPlanId(planId);
    }

    @Override
    public DeployPlanRecord getNextPlanRecord(DeployPlanRecord deployPlanRecord) {
        return deployPlanRecordDao.getNextPlanRecord(deployPlanRecord);
    }

    @Override
    public List<DeployPlanRecord> getNextPlanRecords(DeployPlanRecord deployPlanRecord) {
        return deployPlanRecordDao.getNextPlanRecords(deployPlanRecord);
    }
}
