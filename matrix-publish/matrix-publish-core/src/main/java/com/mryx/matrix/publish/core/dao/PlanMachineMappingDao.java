package com.mryx.matrix.publish.core.dao;

import com.mryx.matrix.publish.domain.PlanMachineMapping;

import java.util.List;

/**
 * 发布计划机器关联表
 *
 * @author dinglu
 * @date 2018/10/19
 */

public interface PlanMachineMappingDao {

    PlanMachineMapping getById(Integer id);

    List<PlanMachineMapping> listPublishByPlanId(Integer planId);

    List<PlanMachineMapping> listRollbackByPlanId(Integer planId);

    List<PlanMachineMapping> listByCondition(PlanMachineMapping planMachineMapping);

    List<PlanMachineMapping> listByPage(PlanMachineMapping planMachineMapping);

    int pageTotal(PlanMachineMapping planMachineMapping);

    int updateById(PlanMachineMapping planMachineMapping);

    int insert(PlanMachineMapping planMachineMapping);

    int updateByIp(PlanMachineMapping planMachineMapping);

    int countUnpublishIps(PlanMachineMapping planMachineMapping);

    int updateTagAndStstus(PlanMachineMapping planMachineMapping);
}
