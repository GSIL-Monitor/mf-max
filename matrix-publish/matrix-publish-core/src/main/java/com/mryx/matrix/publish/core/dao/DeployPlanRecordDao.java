package com.mryx.matrix.publish.core.dao;

import com.mryx.matrix.publish.domain.DeployPlanRecord;

import java.util.List;

/**
 *
 * @author dinglu
 * @date 2018/10/19
 */

/*发布计划发布表*/
public interface DeployPlanRecordDao {

    DeployPlanRecord getById(Integer id);

    List<DeployPlanRecord> getByPlanId(Integer planId);

    List<DeployPlanRecord> listByCondition(DeployPlanRecord deployPlanRecord);

    List<DeployPlanRecord> listByPage(DeployPlanRecord deployPlanRecord);

    int pageTotal(DeployPlanRecord deployPlanRecord);

    int updateById(DeployPlanRecord deployPlanRecord);

    int insert(DeployPlanRecord deployPlanRecord);

    int updateStatusToSuspend(DeployPlanRecord deployPlanRecord);

    DeployPlanRecord getLastIdByPlanId(DeployPlanRecord deployPlanRecord);

    DeployPlanRecord getLastByPlanId(Integer planId);

    DeployPlanRecord getNextPlanRecord(DeployPlanRecord deployPlanRecord);

    List<DeployPlanRecord> getNextPlanRecords(DeployPlanRecord deployPlanRecord);

}
