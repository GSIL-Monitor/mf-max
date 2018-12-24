package com.mryx.matrix.publish.web.controller;

import com.alibaba.fastjson.JSON;
import com.mryx.matrix.common.domain.ResultVo;
import com.mryx.matrix.publish.core.service.DeployPlanService;
import com.mryx.matrix.publish.core.service.PlanMachineMappingService;
import com.mryx.matrix.publish.core.service.ProjectTaskService;
import com.mryx.matrix.publish.domain.*;
import com.mryx.matrix.publish.enums.PublishStatusEnum;
import com.mryx.matrix.publish.web.vo.DeployPlanVo;
import com.mryx.matrix.publish.web.vo.PlanMachineMappingVo;
import com.mryx.matrix.publish.web.vo.RecommendPlanVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author pengcheng
 * @description
 * @email pengcheng@missfresh.cn
 * @date 2018-11-21 11:38
 **/
@RestController
@RequestMapping("/api/publish/rollbackPlan")
public class RollbackPlanController {

    private static final Logger logger = LoggerFactory.getLogger(RollbackPlanController.class);

    /**
     * 特殊分隔符
     */
    private char separator = 0x7f;

    @Resource
    DeployPlanService deployPlanService;

    @Resource
    PlanMachineMappingService planMachineMappingService;

    @Resource
    ProjectTaskService projectTaskService;

    @PostMapping("/getByProjectId")
    @ResponseBody
    public ResultVo getByProjectId(@RequestBody DeployPlan rollback) {
        try {
            logger.info("get rollback plan by project id , param = {}", JSON.toJSONString(rollback));
            Integer projectId = rollback.getProjectId();
            if (projectId == null || projectId <= 0) {
                logger.info("get rollback plan by project id fail , 参数错误 , param = {}", JSON.toJSONString(rollback));
                return ResultVo.Builder.FAIL().initErrCodeAndMsg("2000", "参数错误: projectId is required");
            }
            List<DeployPlan> deployPlanList = deployPlanService.listRollbackPlanByProjectId(projectId);
            if (deployPlanList == null || deployPlanList.size() <= 0) {
                logger.info("get rollback plan by project id is no data , param = {}", rollback.toString());
                return ResultVo.Builder.SUCC().initSuccData(new ArrayList<>());
            }
            List<DeployPlanVo> deployPlanVoList = deployPlanList.stream().map(deployPlan -> {
                return convert2Vo(deployPlan, Integer.valueOf(1));
            }).collect(Collectors.toList());

            deployPlanVoList.sort(new Comparator<DeployPlanVo>() {
                @Override
                public int compare(DeployPlanVo o1, DeployPlanVo o2) {
                    if (o1 == null && o2 == null) {
                        return 0;
                    }

                    if (o1 != null && o2 == null) {
                        return -1;
                    }

                    if (o1 == null && o2 != null) {
                        return 1;
                    }
                    return o1.getSequenece() - o2.getSequenece();
                }
            });

            return ResultVo.Builder.SUCC().initSuccData(deployPlanVoList);
        } catch (Exception e) {
            logger.error("get deployPlan by projectId exception , Exception = {}", e);
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("4000", "系统异常");
        }
    }

    /**
     * @param deployPlans
     * @return
     */
    @PostMapping("/save")
    @ResponseBody
    public ResultVo save(@RequestBody DeployPlanParam deployPlans) {

        try {
            List<DeployPlan> newDeployPlanList = deployPlans.getStepList();
            if (CollectionUtils.isEmpty(newDeployPlanList)) {
                logger.info("save deploy rollback fail , 参数错误 ， param = {}", deployPlans.toString());
                return ResultVo.Builder.FAIL().initErrCodeAndMsg("2000", "参数错误");
            }

            Integer projectId = newDeployPlanList.get(0).getProjectId();
            if (projectId == null || projectId <= 0) {
                logger.info("save deploy rollback fail , 参数错误 ， projectId = {}", projectId);
                return ResultVo.Builder.FAIL().initErrCodeAndMsg("2000", "参数错误");
            }

            // 根据项目ID获取上次所有回滚计划
            DeployPlan last = new DeployPlan();
            last.setProjectId(projectId);
            last.setDelFlag(1);
            last.setRollbackFlag(1);
            List<DeployPlan> oldRollbackPlanList = deployPlanService.listByCondition(last);

            Map<Integer, DeployPlan> oldDeployPlanMap = new HashMap<Integer, DeployPlan>();
            if (!CollectionUtils.isEmpty(oldRollbackPlanList)) {
                oldRollbackPlanList.stream().forEach(deployPlan -> {
                    Integer id = deployPlan.getId();
                    oldDeployPlanMap.put(id, deployPlan);
                });
            }

            // 待新增回滚计划
            List<DeployPlan> insertDeployPlanList = new ArrayList<>();
            // 遍历前端最新接收的回滚计划列表
            for (int index = 0; index < newDeployPlanList.size(); index++) {
                DeployPlan newRollback = newDeployPlanList.get(index);
                Integer rollbackId = newRollback.getId();
                List<PlanMachineMapping> planMachineMappingList = newRollback.getPlanMachineMappings();
                StringBuilder builder = new StringBuilder();
                if (!CollectionUtils.isEmpty(planMachineMappingList)) {
                    planMachineMappingList.stream().forEach(planMachineMapping -> {
                        if (builder.length() > 0) {
                            builder.append(",");
                        }
                        builder.append(planMachineMapping.getServiceIps());
                    });
                }
                String newIps = builder.toString();
                newRollback.setServiceIps(newIps);
                newRollback.setSequenece(index + 1);

                DeployPlan oldRollback = oldDeployPlanMap.get(rollbackId);
                if (oldRollback == null) {
                    insertDeployPlanList.add(newRollback);
                    continue;
                }

                // 如果发布计划被禁用：只更新更新旧计划顺序，其他属性不变
                // 由于禁用的接口调用时，实时修改数据，所以这里新旧发布状态认为一致
                // 且禁用的发布计划不允许修改
                boolean b_Disable = Integer.valueOf(0).equals(newRollback.getDisable());

                Integer projectTaskId = oldRollback.getProjectTaskId();
                PublishStatusEnum statusEnum = getPlanStatus(planMachineMappingList);
                if (b_Disable || PublishStatusEnum.RELEASE_SUCCESS.equals(statusEnum)) {
                    String oldIps = oldRollback.getServiceIps();
                    if (!oldIps.equals(newIps)) {
                        // 如果IP列表发生变化，需要更新该计划中的机器列表
                        PlanMachineMapping condition = new PlanMachineMapping();
                        condition.setDelFlag(1);
                        condition.setPlanId(rollbackId);
                        List<PlanMachineMapping> oldPlanMachineMappingList = planMachineMappingService.listByCondition(condition);
                        if (!CollectionUtils.isEmpty(oldPlanMachineMappingList)) {
                            oldPlanMachineMappingList.stream().forEach(planMachineMapping -> {
                                planMachineMapping.setDelFlag(0);
                                planMachineMappingService.updateById(planMachineMapping);
                            });
                        }

                        int successCount = 0;
                        int failureCount = 0;
                        int waitCount = 0;

                        for (PlanMachineMapping planMachineMapping : planMachineMappingList) {
                            planMachineMapping.setProjectTaskId(projectTaskId);
                            planMachineMapping.setProjectId(projectId);
                            planMachineMapping.setId(null);
                            planMachineMapping.setDelFlag(1);
                            planMachineMapping.setPlanId(rollbackId);
                            planMachineMapping.setRollbackFlag(1);

                            Integer deployStatus = planMachineMapping.getDeployStatus();
                            if (PublishStatusEnum.RELEASE_SUCCESS.getCode().equals(deployStatus)) {
                                successCount++;
                            } else if (PublishStatusEnum.RELEASE_FAIL.getCode().equals(deployStatus)) {
                                failureCount++;
                            } else if (PublishStatusEnum.RELEASE_WAIT.getCode().equals(deployStatus)) {
                                waitCount++;
                            }
                            planMachineMappingService.insert(planMachineMapping);
                        }

                        int machineCount = planMachineMappingList.size();
                        if (successCount == machineCount) {
                            oldRollback.setDeployStatus(PublishStatusEnum.RELEASE_SUCCESS.getCode());
                        } else if (failureCount > 0) {
                            oldRollback.setDeployStatus(PublishStatusEnum.RELEASE_FAIL.getCode());
                        } else if (successCount > 0) {
                            oldRollback.setDeployStatus(PublishStatusEnum.RELEASE_PART_SUCCESS.getCode());
                        } else if (waitCount > 0) {
                            oldRollback.setDeployStatus(PublishStatusEnum.RELEASE_WAIT.getCode());
                        } else {
                            oldRollback.setDeployStatus(PublishStatusEnum.INIT.getCode());
                        }
                        oldRollback.setMachineCount(machineCount);
                        oldRollback.setServiceIps(newIps);
                    }
                    oldRollback.setSequenece(index + 1);
                    oldRollback.setWaitTime(newRollback.getWaitTime());
                    deployPlanService.updateById(oldRollback);
                    oldDeployPlanMap.remove(rollbackId);
                    continue;
                }
                insertDeployPlanList.add(newRollback);
            }

            ///
            // 在上一个循环方法中，已经把禁用的和满足更新条件的（应用已经发布成功且BTag未发生变化，且IP列表未发生变化）进行了更新
            // 剩余的留在oldDeployPlanMap中的历史发布计划，需要被删除
            //
            if (!CollectionUtils.isEmpty(oldDeployPlanMap)) {
                oldDeployPlanMap.values().stream().forEach(deployPlan -> {
                    deployPlan.setDelFlag(0);
                    deployPlanService.updateById(deployPlan);
                    PlanMachineMapping condition = new PlanMachineMapping();
                    condition.setDelFlag(1);
                    condition.setPlanId(deployPlan.getId());
                    List<PlanMachineMapping> planMachineMappingList = planMachineMappingService.listByCondition(condition);
                    if (!CollectionUtils.isEmpty(planMachineMappingList)) {
                        planMachineMappingList.stream().forEach(planMachineMapping -> {
                            planMachineMapping.setDelFlag(0);
                            planMachineMappingService.updateById(planMachineMapping);
                        });
                    }
                });
            }

            ///
            // 将insertDeployPlanList列表中的发布计划保存到数据库，并且需要同步添加机器映射列表
            if (!CollectionUtils.isEmpty(insertDeployPlanList)) {
                insertDeployPlanList.stream().forEach(deployPlan -> {
                    deployPlan.setId(null);
                    List<PlanMachineMapping> planMachineMappingList = deployPlan.getPlanMachineMappings();
                    deployPlan.setMachineCount(planMachineMappingList.size());
                    deployPlanService.insert(deployPlan);

                    int successCount = 0;
                    int failureCount = 0;
                    int waitCount = 0;
                    for (PlanMachineMapping planMachineMapping : planMachineMappingList) {
                        planMachineMapping.setPlanId(deployPlan.getId());
                        planMachineMapping.setProjectId(projectId);
                        planMachineMapping.setProjectTaskId(deployPlan.getProjectTaskId());
                        planMachineMapping.setId(null);
                        Integer deployStatus = planMachineMapping.getDeployStatus();
                        if (PublishStatusEnum.RELEASE_SUCCESS.getCode().equals(deployStatus)) {
                            successCount++;
                        } else if (PublishStatusEnum.RELEASE_FAIL.getCode().equals(deployStatus)) {
                            failureCount++;
                        } else if (PublishStatusEnum.RELEASE_WAIT.getCode().equals(deployStatus)) {
                            waitCount++;
                        }
                        planMachineMappingService.insert(planMachineMapping);
                    }

                    int machineCount = planMachineMappingList.size();
                    if (successCount == machineCount) {
                        deployPlan.setDeployStatus(PublishStatusEnum.RELEASE_SUCCESS.getCode());
                    } else if (failureCount > 0) {
                        deployPlan.setDeployStatus(PublishStatusEnum.RELEASE_FAIL.getCode());
                    } else if (successCount > 0) {
                        deployPlan.setDeployStatus(PublishStatusEnum.RELEASE_PART_SUCCESS.getCode());
                    } else if (waitCount > 0) {
                        deployPlan.setDeployStatus(PublishStatusEnum.RELEASE_WAIT.getCode());
                    } else {
                        deployPlan.setDeployStatus(PublishStatusEnum.INIT.getCode());
                    }
                    deployPlanService.updateById(deployPlan);
                });
            }

            ResultVo resultVo = this.getByProjectId(last);
            logger.info("save deploy return , {}", JSON.toJSONString(resultVo));
            return resultVo;
        } catch (Exception e) {
            logger.error("save plan rollback exception , Exception = {}", e);
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("4000", "系统异常");
        }
    }

    /**
     * 根据发布服务器状态获取当前批次状态
     *
     * @param planMachineMappingList
     * @return
     */
    private PublishStatusEnum getPlanStatus(List<PlanMachineMapping> planMachineMappingList) {
        int successCount = 0;
        int failureCount = 0;
        int waitCount = 0;
        int initCount = 0;
        for (PlanMachineMapping planMachineMapping : planMachineMappingList) {
            Integer deployStatus = planMachineMapping.getDeployStatus();
            if (PublishStatusEnum.RELEASE_SUCCESS.getCode().equals(deployStatus)) {
                successCount++;
            } else if (PublishStatusEnum.RELEASE_FAIL.getCode().equals(deployStatus)) {
                failureCount++;
                break;
            } else if (PublishStatusEnum.RELEASE_WAIT.getCode().equals(deployStatus)) {
                waitCount++;
            } else if (PublishStatusEnum.INIT.getCode().equals(deployStatus)) {
                initCount++;
            }
        }

        int machineCount = planMachineMappingList.size();
        if (successCount == machineCount) {
            return PublishStatusEnum.RELEASE_SUCCESS;
        } else if (failureCount > 0) {
            return PublishStatusEnum.RELEASE_FAIL;
        } else if (successCount > 0) {
            return PublishStatusEnum.RELEASE_PART_SUCCESS;
        } else if (waitCount > 0) {
            return PublishStatusEnum.RELEASE_WAIT;
        } else {
            return PublishStatusEnum.INIT;
        }
    }

    /**
     * 生成推荐回滚计划接口
     *
     * @param rollbackPlanParam 推荐回滚参数
     * @return 返回推荐结果
     */
    @PostMapping("/recommend")
    @ResponseBody
    public ResultVo recommend(@RequestBody RollbackPlanParam rollbackPlanParam) {
        try {
            logger.info("recommend rollback plan , param = {}", JSON.toJSONString(rollbackPlanParam.toString()));

            List<DeployPlan> rollbackList = rollbackPlanParam.getRollbackList();
            Integer projectId = rollbackPlanParam.getProjectId();
            if (projectId == null || projectId <= 0) {
                logger.info("get deploy plan by projectId fail , 参数错误 , param = {}", JSON.toJSONString(rollbackPlanParam.toString()));
                return ResultVo.Builder.FAIL().initErrCodeAndMsg("2000", "参数错误");
            }

            List<DeployPlanVo> retRollbackPlanVoList;
            List<DeployPlan> deployPlanList = deployPlanService.listPublishPlanByProjectId(projectId);
            if (deployPlanList == null || deployPlanList.size() <= 0) {
                logger.info("get deploy plan by projectId no data , param = {}", projectId);
                return ResultVo.Builder.SUCC().initSuccData(new ArrayList<>());
            }

            List<DeployPlanVo> deployPlanVoList = deployPlanList.stream().map(deployPlan -> {
                return convert2Vo(deployPlan, Integer.valueOf(0));
            }).collect(Collectors.toList());

            deployPlanVoList.sort(new Comparator<DeployPlanVo>() {
                @Override
                public int compare(DeployPlanVo o1, DeployPlanVo o2) {
                    return o1.getSequenece() - o2.getSequenece();
                }
            });
            // 如果没有历史滚回计划，则生成默认回滚计划
            // 否则，需要考虑历史回滚计划的状态，生成新的回滚计划
            if (CollectionUtils.isEmpty(rollbackList)) {
                retRollbackPlanVoList = getDefaultRollback(deployPlanVoList);
            } else {
                retRollbackPlanVoList = recommend(rollbackList, deployPlanVoList);
            }
            RecommendPlanVo vo = new RecommendPlanVo();
            vo.setStepList(retRollbackPlanVoList);
            logger.info("rollback recommend finish , return = {}", JSON.toJSONString(vo));
            return ResultVo.Builder.SUCC().initSuccData(vo);
        } catch (Exception e) {
            logger.error("get deployPlan by projectId exception , Exception = {}", e);
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("4000", "系统异常");
        }
    }

    /**
     * 根据发布计划和当前回滚计划生成新的回滚计划
     *
     * @param rollbackList 上次回滚计划列表
     * @param publishList  当前发布计划列表
     * @return
     */
    private List<DeployPlanVo> recommend(List<DeployPlan> rollbackList, List<DeployPlanVo> publishList) {
        // 上次回滚数据，主键使用 appCode + groupName，
        // 用于查询上次回滚计划ID和回滚状态
        //
        Map<String, List<DeployPlan>> lastCodeGroupMap = new HashMap<>();
        for (DeployPlan rollback : rollbackList) {
            String appCode = rollback.getAppCode();
            String groupName = rollback.getAppGroup() != null ? rollback.getAppGroup().trim() : null;
            String codeGroup = appCode + separator + groupName;

            List<DeployPlan> lastRollbackList = lastCodeGroupMap.get(codeGroup);
            if (lastRollbackList == null) {
                lastRollbackList = new ArrayList<>();
                lastCodeGroupMap.put(codeGroup, lastRollbackList);
            }
            lastRollbackList.add(rollback);
        }
        Integer projectId = publishList.get(0).getProjectId();

        // 应用缓存，使用appCode作为key
        // 用于获取最新的RTag
        Map<String, ProjectTask> projectTaskMap = new HashMap<>();
        ProjectTask condition = new ProjectTask();
        condition.setDelFlag(1);
        condition.setProjectId(projectId);
        List<ProjectTask> projectTaskList = projectTaskService.listByCondition(condition);
        if (CollectionUtils.isEmpty(projectTaskList)) {
            throw new RuntimeException("project task is empty, projectId is " + projectId + ".");
        }

        projectTaskList.stream().forEach(projectTask -> {
            String appCode = projectTask.getAppCode();
            projectTaskMap.put(appCode, projectTask);
        });

        ////////////////////////////////////////////////////////////////////////////////////////////
        //                                       准备数据结束                                       //
        ////////////////////////////////////////////////////////////////////////////////////////////

        // 返回的回滚计划列表
        List<DeployPlanVo> rollbackPlanList = new ArrayList<>();

        // 遍历当前发布计划，作为新回滚计划的依据
        for (int index = publishList.size() - 1; index >= 0; index--) {
            DeployPlanVo publish = publishList.get(index);

            String appCode = publish.getAppCode();
            ProjectTask projectTask = projectTaskMap.get(appCode);
            if (projectTask == null) {
                throw new RuntimeException("app code '" + appCode + "' is not exist!");
            }

            String appRTag = projectTask.getAppRtag();
            String groupName = publish.getAppGroup() != null ? publish.getAppGroup().trim() : null;
            String codeGroup = appCode + separator + groupName;

            List<DeployPlan> rollback = lastCodeGroupMap.get(codeGroup);
            if (CollectionUtils.isEmpty(rollback)) {
                rollbackPlanList.add(getNewRollbackPlan(publish, appRTag));
            } else {
                rollbackPlanList.add(getRollbackPlan(publish, rollback, appRTag));
            }
        }
        return rollbackPlanList;
    }

    private DeployPlanVo getRollbackPlan(DeployPlanVo publish, List<DeployPlan> rollbackList, String appRTag) {

        List<PlanMachineMappingVo> publishMachineList = publish.getPlanMachineMappings();

        Loop_Rollback:
        for (DeployPlan rollback : rollbackList) {
            if (!PublishStatusEnum.RELEASE_SUCCESS.getCode().equals(rollback.getDeployStatus()) && !Integer.valueOf(0).equals(rollback.getDisable())) {
                continue;
            } else {
                List<PlanMachineMapping> rollbackMachineList = rollback.getPlanMachineMappings();
                if (CollectionUtils.isEmpty(publishMachineList)) {
                    if (CollectionUtils.isEmpty(rollbackMachineList)) {
                        DeployPlanVo rollbackVo = new DeployPlanVo();
                        BeanUtils.copyProperties(rollback, rollbackVo);
                        return rollbackVo;
                    }
                } else {
                    if (CollectionUtils.isEmpty(publishMachineList)) {
                        break;
                    }

                    if (publishMachineList.size() != rollbackMachineList.size()) {
                        break;
                    }

                    Map<String, PlanMachineMapping> rollbackMachineMap = new HashMap<>(rollbackMachineList.size());
                    rollbackMachineList.stream().forEach(rollbackMachine -> {
                        rollbackMachineMap.put(rollbackMachine.getServiceIps(), rollbackMachine);
                    });

                    List<PlanMachineMappingVo> newRollbackMachineList = new ArrayList<>();
                    for (int index = publishMachineList.size() - 1; index >= 0; index--) {
                        PlanMachineMappingVo publishMachine = publishMachineList.get(index);
                        String ip = publishMachine.getServiceIps();
                        PlanMachineMapping rollbackMachine = rollbackMachineMap.get(ip);
                        if (rollbackMachine == null) {
                            break Loop_Rollback;
                        }
                        if (!PublishStatusEnum.RELEASE_SUCCESS.getCode().equals(rollbackMachine.getDeployStatus())) {
                            break Loop_Rollback;
                        }

                        PlanMachineMappingVo newRollbackMachine = new PlanMachineMappingVo();
                        BeanUtils.copyProperties(rollbackMachine, newRollbackMachine);
                        newRollbackMachineList.add(newRollbackMachine);
                    }

                    DeployPlanVo newRollbackPlan = new DeployPlanVo();
                    BeanUtils.copyProperties(rollback, newRollbackPlan);
                    newRollbackPlan.setPlanMachineMappings(newRollbackMachineList);
                    return newRollbackPlan;
                }
            }
        }

        return getNewRollbackPlan(publish, appRTag);
    }


    /**
     * 是否需要重新生成回滚计划
     *
     * @param rollback 历史回滚计划
     * @param publish  当前发布计划
     * @return 是否需要生成新回滚计划
     */
    private boolean reGenerate(DeployPlan rollback, DeployPlan publish) {
        if (!PublishStatusEnum.RELEASE_SUCCESS.getCode().equals(rollback.getDeployStatus())) {
            return true;
        }
        List<PlanMachineMapping> rollbackMachineList = rollback.getPlanMachineMappings();
        List<PlanMachineMapping> publishMachineList = publish.getPlanMachineMappings();
        if (CollectionUtils.isEmpty(publishMachineList)) {
            if (CollectionUtils.isEmpty(rollbackMachineList)) {
                return false;
            }
        }

        return true;
    }

    /**
     * 根据发布计划生成对应的回滚计划
     *
     * @param deployPlan 发布计划
     * @param appRTag    回滚Tag
     * @return 回滚计划
     */
    private DeployPlanVo getNewRollbackPlan(DeployPlanVo deployPlan, String appRTag) {
        Integer projectId = deployPlan.getProjectId();
        String appCode = deployPlan.getAppCode();
        String groupName = deployPlan.getAppGroup() != null ? deployPlan.getAppGroup().trim() : null;
        Integer projectTaskId = deployPlan.getProjectTaskId();
        String appBTag = deployPlan.getAppBtag();

        List<PlanMachineMappingVo> planMachineMappingList = deployPlan.getPlanMachineMappings();

        DeployPlanVo newRollback = new DeployPlanVo();
        newRollback.setAppGroup(groupName);
        newRollback.setRollbackFlag(1);
        newRollback.setProjectId(projectId);
        newRollback.setProjectTaskId(projectTaskId);
        newRollback.setAppCode(appCode);
        newRollback.setAppRtag(appRTag);
        newRollback.setDeployStatus(PublishStatusEnum.INIT.getCode());
        newRollback.setDeployStatusDesc(PublishStatusEnum.INIT.getValue());
        newRollback.setAppBtag(appBTag);
        newRollback.setDisable(1);
        newRollback.setWaitTime(0);

        if (!CollectionUtils.isEmpty(planMachineMappingList)) {
            StringBuilder builder = new StringBuilder();
            List<PlanMachineMappingVo> newPlanMachineMappingVoList = new ArrayList<>();
            for (int i = planMachineMappingList.size() - 1; i >= 0; i--) {
                PlanMachineMappingVo planMachineMappingVo = planMachineMappingList.get(i);

                PlanMachineMappingVo newPlanMachineMappingVo = new PlanMachineMappingVo();
                newPlanMachineMappingVo.setRollbackFlag(1);
                newPlanMachineMappingVo.setDeployStatus(PublishStatusEnum.INIT.getCode());
                newPlanMachineMappingVo.setDeployStatusDesc(PublishStatusEnum.convertRelease2RollbackDesc(PublishStatusEnum.INIT.getCode()));
                newPlanMachineMappingVo.setProjectId(projectId);
                newPlanMachineMappingVo.setProjectTaskId(projectTaskId);
                newPlanMachineMappingVo.setAppGroup(planMachineMappingVo.getAppGroup());
                newPlanMachineMappingVo.setServiceIps(planMachineMappingVo.getServiceIps());
                newPlanMachineMappingVo.setAppTag(appRTag);

                if (builder.length() > 0) {
                    builder.append(",");
                }
                builder.append(planMachineMappingVo.getServiceIps());
                newPlanMachineMappingVoList.add(newPlanMachineMappingVo);
            }
            newRollback.setServiceIps(builder.toString());
            newRollback.setPlanMachineMappings(newPlanMachineMappingVoList);
        }
        return newRollback;
    }

    /**
     * 获取默认回滚计划
     *
     * @param deployPlanVoList 发布计划列表
     * @return 返回默认回滚计划
     */
    private List<DeployPlanVo> getDefaultRollback(List<DeployPlanVo> deployPlanVoList) {
        List<DeployPlanVo> list = new ArrayList<>();
        for (int index = deployPlanVoList.size() - 1, sequence = 1; index >= 0; index--, sequence++) {
            DeployPlanVo deployPlanVo = deployPlanVoList.get(index);
            deployPlanVo.setId(null);
            deployPlanVo.setDeployStatus(PublishStatusEnum.INIT.getCode());
            deployPlanVo.setDeployStatusDesc(PublishStatusEnum.convertRelease2RollbackDesc(PublishStatusEnum.INIT.getCode()));
            deployPlanVo.setSequenece(sequence);
            deployPlanVo.setRollbackFlag(1);
            deployPlanVo.setCreateTime(null);
            deployPlanVo.setUpdateTime(null);
            deployPlanVo.setSubDeployStatus(null);

            List<PlanMachineMappingVo> planMachineMappingList = deployPlanVo.getPlanMachineMappings();
            List<PlanMachineMappingVo> planMachineMappingVoList = new ArrayList<>();
            if (!CollectionUtils.isEmpty(planMachineMappingList)) {
                StringBuilder builder = new StringBuilder();
                for (int i = planMachineMappingList.size() - 1; i >= 0; i--) {
                    PlanMachineMappingVo planMachineMappingVo = planMachineMappingList.get(i);
                    planMachineMappingVo.setId(null);
                    planMachineMappingVo.setPlanId(null);
                    planMachineMappingVo.setDeployStatus(PublishStatusEnum.INIT.getCode());
                    planMachineMappingVo.setDeployStatusDesc(PublishStatusEnum.convertRelease2RollbackDesc(PublishStatusEnum.INIT.getCode()));
                    planMachineMappingVo.setRollbackFlag(1);
                    planMachineMappingVo.setCreateTime(null);
                    planMachineMappingVo.setUpdateTime(null);

                    planMachineMappingVoList.add(planMachineMappingVo);

                    if (builder.length() > 0) {
                        builder.append(",");
                    }
                    builder.append(planMachineMappingVo.getServiceIps());
                }
                deployPlanVo.setServiceIps(builder.toString());
            }
            deployPlanVo.setPlanMachineMappings(planMachineMappingVoList);

            list.add(deployPlanVo);
        }
        return list;
    }

    private DeployPlanVo convert2Vo(DeployPlan deployPlan, Integer rollbackFlag) {
        DeployPlanVo deployPlanVo = new DeployPlanVo();
        BeanUtils.copyProperties(deployPlan, deployPlanVo);
        if (PublishStatusEnum.RELEASE_FAIL.getCode().equals(deployPlan.getDeployStatus())) {
            deployPlanVo.setDeployStatusDesc(PublishStatusEnum.convertRelease2RollbackDesc(deployPlan.getDeployStatus()) + "-" + PublishStatusEnum.convertRelease2RollbackDesc(deployPlan.getSubDeployStatus()));
        } else {
            deployPlanVo.setDeployStatusDesc(PublishStatusEnum.convertRelease2RollbackDesc(deployPlan.getDeployStatus()));
        }

        List<PlanMachineMapping> planMachineMappingList;
        if (Integer.valueOf(0).equals(rollbackFlag)) {
            planMachineMappingList = planMachineMappingService.listPublishByPlanId(deployPlan.getId());
        } else {
            planMachineMappingList = planMachineMappingService.listRollbackByPlanId(deployPlan.getId());
        }

        if (planMachineMappingList == null || planMachineMappingList.size() <= 0) {
            logger.info("plan no planMachineMapping , param = {}", deployPlan.toString());
            deployPlanVo.setPlanMachineMappings(new ArrayList<>());
            return deployPlanVo;
        }
        List<PlanMachineMappingVo> planMachineMappingVoList = planMachineMappingList.stream().map(planMachineMapping -> {
            return covert2Vo(planMachineMapping);
        }).collect(Collectors.toList());
        deployPlanVo.setPlanMachineMappings(planMachineMappingVoList);
        return deployPlanVo;
    }

    private PlanMachineMappingVo covert2Vo(PlanMachineMapping planMachineMapping) {
        PlanMachineMappingVo planMachineMappingVo = new PlanMachineMappingVo();
        BeanUtils.copyProperties(planMachineMapping, planMachineMappingVo);
        planMachineMappingVo.setDeployStatusDesc(PublishStatusEnum.convertRelease2RollbackDesc(planMachineMapping.getDeployStatus()));
        return planMachineMappingVo;
    }
}
