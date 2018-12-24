package com.mryx.matrix.publish.web.controller;

import com.alibaba.fastjson.JSON;
import com.mryx.matrix.common.domain.ResultVo;
import com.mryx.matrix.publish.core.service.DeployPlanService;
import com.mryx.matrix.publish.core.service.PlanMachineMappingService;
import com.mryx.matrix.publish.core.service.ProjectTaskService;
import com.mryx.matrix.publish.core.service.ReleaseDelpoyRecordService;
import com.mryx.matrix.publish.domain.*;
import com.mryx.matrix.publish.enums.PublishStatusEnum;
import com.mryx.matrix.publish.web.vo.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 发布计划controller
 *
 * @author dinglu
 * @date 2018/10/19
 */
@RestController
@RequestMapping("/api/publish/deployPlan")
public class DeployPlanController {

    private static final Logger logger = LoggerFactory.getLogger(DeployPlanController.class);

    @Resource
    DeployPlanService deployPlanService;

    @Resource
    PlanMachineMappingService planMachineMappingService;

    @Resource
    ProjectTaskService projectTaskService;

    @Resource
    ReleaseDelpoyRecordService releaseDelpoyRecordService;

    @PostMapping("/getById")
    @ResponseBody
    public ResultVo getById(@RequestParam("id") Integer id) {
        try {
            logger.info("get deployPlan by id , param = {}", id);
            if (id == null || id <= 0) {
                logger.info("get deployPlan by id fail ,参数错误 , param = {}", id);
                return ResultVo.Builder.FAIL().initErrCodeAndMsg("2000", "参数错误");
            }
            DeployPlan deployPlan = deployPlanService.getById(id);
            if (deployPlan == null) {
                logger.info("get deployPlan by id no data , param = {}", id);
                return ResultVo.Builder.SUCC().initSuccData(new DeployPlanVo());
            }
            DeployPlanVo deployPlanVo = convertTo(deployPlan);
            return ResultVo.Builder.SUCC().initSuccData(deployPlanVo);
        } catch (Exception e) {
            logger.error("get deployPlan by id exception , Exception = {}", e);
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("4000", "系统异常");
        }
    }

    @PostMapping("/getByProjectId")
    @ResponseBody
    public ResultVo getByProjectId(@RequestBody DeployPlan deployPlan) {
        try {
            logger.info("get deployPlan by projectId , param = {}", deployPlan.toString());
            Integer projectId = deployPlan.getProjectId();
            if (projectId == null || projectId <= 0) {
                logger.info("get deployPlan by projectId fail , 参数错误 , param = {}", projectId);
                return ResultVo.Builder.FAIL().initErrCodeAndMsg("2000", "参数错误");
            }
            List<DeployPlan> deployPlanList = deployPlanService.listPublishPlanByProjectId(projectId);
            if (deployPlanList == null || deployPlanList.size() <= 0) {
                logger.info("get deployPlan by projectId no data , param = {}", projectId);
                return ResultVo.Builder.SUCC().initSuccData(new ArrayList<>());
            }
            List<DeployPlanVo> deployPlanVoList = deployPlanList.stream().map(deployPlan1 -> {
                DeployPlanVo deployPlanVo = convertTo(deployPlan1);
                return deployPlanVo;
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

    @PostMapping("/listByCondition")
    @ResponseBody
    public ResultVo listByCondition(@RequestBody DeployPlan deployPlan) {
        try {
            logger.info("list deployPlan by condition , param = {}", deployPlan.toString());
            if (deployPlan == null) {
                logger.info("list deployPlan by condition fail , 参数错误 , param = {}", deployPlan.toString());
                return ResultVo.Builder.FAIL().initErrCodeAndMsg("2000", "参数错误");
            }
            deployPlan.setDelFlag(1);
            List<DeployPlan> deployPlanList = deployPlanService.listByCondition(deployPlan);
            if (deployPlanList == null || deployPlanList.size() <= 0) {
                logger.info("list deployPlan by condition no data , param = {}", deployPlan.toString());
                return ResultVo.Builder.SUCC().initSuccData(new ArrayList<>());
            }
            List<DeployPlanVo> deployPlanVoList = deployPlanList.stream().map(deployPlan1 -> {
                DeployPlanVo deployPlanVo = convertTo(deployPlan1);
                return deployPlanVo;
            }).collect(Collectors.toList());
            return ResultVo.Builder.SUCC().initSuccData(deployPlanVoList);
        } catch (Exception e) {
            logger.error("list deployPlan by conditions exception , Exception = {}", e);
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("4000", "系统异常");
        }
    }

    @PostMapping("/list")
    @ResponseBody
    public ResultVo list(@RequestBody DeployPlan deployPlan) {
        try {
            logger.info("list deployPlan by page , param = {}", deployPlan.toString());
            if (deployPlan == null) {
                logger.info("list deployPlan by page fail , 参数错误 , param = {}", deployPlan.toString());
                return ResultVo.Builder.FAIL().initErrCodeAndMsg("2000", "参数错误");
            }
            deployPlan.setDelFlag(1);
            int total = deployPlanService.pageTotal(deployPlan);
            Pagination<DeployPlanVo> pagination = new Pagination<>();
            pagination.setPageSize(deployPlan.getPageSize());
            pagination.setTotalPageForTotalSize(total);
            if (total <= 0) {
                logger.info("list deployPlan by page no data , param = {}", deployPlan.toString());
                pagination.setDataList(new ArrayList<>());
                return ResultVo.Builder.SUCC().initSuccData(pagination);
            }
            List<DeployPlan> deployPlanList = deployPlanService.listByPage(deployPlan);
            List<DeployPlanVo> deployPlanVoList = deployPlanList.stream().map(deployPlan1 -> {
                DeployPlanVo deployPlanVo = convertTo(deployPlan1);
                return deployPlanVo;
            }).collect(Collectors.toList());
            pagination.setDataList(deployPlanVoList);
            return ResultVo.Builder.SUCC().initSuccData(pagination);
        } catch (Exception e) {
            logger.error("list deployPlan by page exception , Exception = {}", e);
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("4000", "系统异常");
        }
    }

    @PostMapping("/forbidden")
    @ResponseBody
    public ResultVo forbidden(@RequestBody DeployPlan deployPlan) {
        try {
            logger.info("forbidden deployPlan , param = {}", deployPlan.toString());
            if (deployPlan == null || deployPlan.getId() == null || deployPlan.getId() <= 0 || deployPlan.getDisable() == null) {
                logger.info("forbidden deployPlan fail , 参数错误 , param = {}", deployPlan.toString());
                return ResultVo.Builder.FAIL().initErrCodeAndMsg("2000", "参数错误");
            }
            int result = deployPlanService.updateById(deployPlan);
            if (result <= 0) {
                logger.info("forbidden deployPlan fail , 更新失败 , param = {}", deployPlan.toString());
                return ResultVo.Builder.FAIL().initErrCodeAndMsg("2001", "更新失败");
            }
            return ResultVo.Builder.SUCC().initSuccData("更新成功");
        } catch (Exception e) {
            logger.error("forbidden deployPlan exception , Exception = {}", e);
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("4000", "系统异常");
        }
    }

    private DeployPlanVo convertTo(DeployPlan deployPlan) {
        DeployPlanVo deployPlanVo = new DeployPlanVo();
        BeanUtils.copyProperties(deployPlan, deployPlanVo);
        if (deployPlan.getDeployStatus().equals(PublishStatusEnum.RELEASE_FAIL) && deployPlan.getSubDeployStatus() != null) {
            deployPlanVo.setDeployStatusDesc(PublishStatusEnum.getValueByCode(deployPlan.getDeployStatus()) + "-" + PublishStatusEnum.getValueByCode(deployPlan.getSubDeployStatus()));
        } else {
            deployPlanVo.setDeployStatusDesc(PublishStatusEnum.getValueByCode(deployPlan.getDeployStatus()));
        }
        List<PlanMachineMapping> planMachineMappingList = planMachineMappingService.listPublishByPlanId(deployPlan.getId());
        if (planMachineMappingList == null || planMachineMappingList.size() <= 0) {
            logger.info("deployPlan no planMachineMapping , param = {}", deployPlan.toString());
            deployPlanVo.setPlanMachineMappings(new ArrayList<>());
            return deployPlanVo;
        }
        List<PlanMachineMappingVo> planMachineMappingVoList = planMachineMappingList.stream().map(planMachineMapping -> {
            PlanMachineMappingVo planMachineMappingVo = covertTo(planMachineMapping);
            return planMachineMappingVo;
        }).collect(Collectors.toList());
        deployPlanVo.setPlanMachineMappings(planMachineMappingVoList);
        return deployPlanVo;
    }

    private PlanMachineMappingVo covertTo(PlanMachineMapping planMachineMapping) {
        PlanMachineMappingVo planMachineMappingVo = new PlanMachineMappingVo();
        BeanUtils.copyProperties(planMachineMapping, planMachineMappingVo);
        planMachineMappingVo.setDeployStatusDesc(PublishStatusEnum.getValueByCode(planMachineMapping.getDeployStatus()));
        return planMachineMappingVo;
    }

    /**
     * @param deployPlans
     * @return
     */
    @PostMapping("/save")
    @ResponseBody
    public ResultVo save(@RequestBody DeployPlanParam deployPlans) {
        try {
            logger.info("save deploy plan , param = {}", JSON.toJSONString(deployPlans.toString()));

            List<DeployPlan> newDeployPlanList = deployPlans.getStepList();
            if (CollectionUtils.isEmpty(newDeployPlanList)) {
                logger.info("save deploy plan fail , 参数错误 ， param = {}", deployPlans.toString());
                return ResultVo.Builder.FAIL().initErrCodeAndMsg("2000", "参数错误");
            }

            Integer projectId = newDeployPlanList.get(0).getProjectId();
            if (projectId == null || projectId <= 0) {
                logger.info("save deploy plan fail , 参数错误 ， projectId = {}", projectId);
                return ResultVo.Builder.FAIL().initErrCodeAndMsg("2000", "参数错误");
            }

            if (!releaseCheck(projectId)) {
                logger.error("存在发布中的任务，无法保存发布计划");
                return ResultVo.Builder.FAIL().initErrCodeAndMsg("3000", "存在发布中的任务，无法保存发布计划");
            }

            // 根据项目ID获取上次所有发布计划
            DeployPlan last = new DeployPlan();
            last.setProjectId(projectId);
            last.setDelFlag(1);
            last.setRollbackFlag(0);
            List<DeployPlan> oldDeployPlanList = deployPlanService.listByCondition(last);
            ///
            // 有计划被拆分时，可能       ：new.size() > old.size();
            // 有计划被合并（移动）时，可能：new.size() < old.size();
            //

            Map<Integer, DeployPlan> oldDeployPlanMap = new HashMap<Integer, DeployPlan>();
            if (!CollectionUtils.isEmpty(oldDeployPlanList)) {
                oldDeployPlanList.stream().forEach(deployPlan -> {
                    Integer id = deployPlan.getId();
                    oldDeployPlanMap.put(id, deployPlan);
                });
            }

            // 待新增发布计划列表
            List<DeployPlan> insertDeployPlanList = new ArrayList<>();
            for (int index = 0; index < newDeployPlanList.size(); index++) {
                DeployPlan newDeployPlan = newDeployPlanList.get(index);
                Integer deployPlanId = newDeployPlan.getId();
                List<PlanMachineMapping> planMachineMappingList = newDeployPlan.getPlanMachineMappings();
                StringBuilder builder = new StringBuilder();
                planMachineMappingList.stream().forEach(planMachineMapping -> {
                    if (builder.length() > 0) {
                        builder.append(",");
                    }
                    builder.append(planMachineMapping.getServiceIps());
                });
                String newIps = builder.toString();
                newDeployPlan.setServiceIps(newIps);
                newDeployPlan.setSequenece(index + 1);

                DeployPlan oldDeployPlan = oldDeployPlanMap.get(deployPlanId);
                if (oldDeployPlan == null) {
                    insertDeployPlanList.add(newDeployPlan);
                    continue;
                }

                Integer projectTaskId = oldDeployPlan.getProjectTaskId();
                ProjectTask projectTask = projectTaskService.getById(projectTaskId);
                if (projectTask == null) {
                    logger.error("查找不到应用信息，projectTaskID=" + projectTask);
                } else {
                    // 如果发布计划被禁用：只更新更新旧计划顺序，其他属性不变
                    // 由于禁用的接口调用时，实时修改数据，所以这里新旧发布状态认为一致
                    // 且禁用的发布计划不允许修改
                    boolean b_Disable = Integer.valueOf(0).equals(newDeployPlan.getDisable());

                    // 如果发布计划的应用已经发布完成，且BTag没有发生变化，且IP地址没有发生变化：只更新更新旧计划顺序，其他属性不变
                    Integer releaseTaskStatus = newDeployPlan.getDeployStatus();
                    if (b_Disable || PublishStatusEnum.RELEASE_SUCCESS.getCode().equals(releaseTaskStatus)) {
                        String newBTag = projectTask.getAppBtag();
                        String oldBTag = oldDeployPlan.getAppBtag();
                        if (b_Disable || newBTag.equals(oldBTag)) {
                            String oldIps = oldDeployPlan.getServiceIps();
                            if (!oldIps.equals(newIps)) {
                                // 如果IP列表发生变化，需要更新该计划中的机器列表
                                PlanMachineMapping condition = new PlanMachineMapping();
                                condition.setDelFlag(1);
                                condition.setPlanId(deployPlanId);
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
                                int initCount = 0;

                                for (PlanMachineMapping planMachineMapping : planMachineMappingList) {
                                    planMachineMapping.setProjectTaskId(projectTaskId);
                                    planMachineMapping.setProjectId(projectId);
                                    planMachineMapping.setId(null);
                                    planMachineMapping.setDelFlag(1);
                                    planMachineMapping.setPlanId(deployPlanId);
                                    planMachineMapping.setRollbackFlag(0);

                                    Integer deployStatus = planMachineMapping.getDeployStatus();
                                    if (PublishStatusEnum.RELEASE_SUCCESS.getCode().equals(deployStatus)) {
                                        successCount++;
                                    } else if (PublishStatusEnum.RELEASE_FAIL.getCode().equals(deployStatus)) {
                                        failureCount++;
                                    } else if (PublishStatusEnum.RELEASE_WAIT.getCode().equals(deployStatus)) {
                                        waitCount++;
                                    } else if (PublishStatusEnum.INIT.getCode().equals(deployStatus)) {
                                        initCount++;
                                    }
                                    planMachineMappingService.insert(planMachineMapping);
                                }

                                int machineCount = planMachineMappingList.size();
                                if (successCount == machineCount) {
                                    oldDeployPlan.setDeployStatus(PublishStatusEnum.RELEASE_SUCCESS.getCode());
                                } else if (failureCount > 0) {
                                    oldDeployPlan.setDeployStatus(PublishStatusEnum.RELEASE_FAIL.getCode());
                                } else if (successCount > 0) {
                                    oldDeployPlan.setDeployStatus(PublishStatusEnum.RELEASE_PART_SUCCESS.getCode());
                                } else if (waitCount > 0) {
                                    oldDeployPlan.setDeployStatus(PublishStatusEnum.RELEASE_WAIT.getCode());
                                } else {
                                    oldDeployPlan.setDeployStatus(PublishStatusEnum.INIT.getCode());
                                }
                                oldDeployPlan.setMachineCount(machineCount);
                                oldDeployPlan.setServiceIps(newIps);
                            }
                            oldDeployPlan.setSequenece(index + 1);
                            oldDeployPlan.setWaitTime(newDeployPlan.getWaitTime());
                            deployPlanService.updateById(oldDeployPlan);
                            oldDeployPlanMap.remove(deployPlanId);
                            continue;
                        }
                    }
                }
                insertDeployPlanList.add(newDeployPlan);
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
//                    condition.setDisable(1);
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
                    int initCount = 0;
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
                        } else if (PublishStatusEnum.INIT.getCode().equals(deployStatus)) {
                            initCount++;
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
            logger.error("save deploy plan exception , Exception = {}", e);
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("4000", "系统异常");
        }
    }

    /**
     * 根据发布服务器状态获取当前批次状态
     *
     * @param planMachineMappingList
     * @return
     */
    private PublishStatusEnum getPlanStatus(List<PlanMachineMappingVo> planMachineMappingList) {
        int successCount = 0;
        int failureCount = 0;
        int waitCount = 0;
        int initCount = 0;
        for (PlanMachineMappingVo planMachineMapping : planMachineMappingList) {
            Integer deployStatus = planMachineMapping.getDeployStatus();
            if (PublishStatusEnum.RELEASE_SUCCESS.getCode().equals(deployStatus)) {
                successCount++;
            } else if (PublishStatusEnum.RELEASE_FAIL.getCode().equals(deployStatus)) {
                failureCount++;
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
     * 生成推荐发布计划接口
     *
     * @param deployPlanParam
     * @return
     */
    @PostMapping("/recommend")
    @ResponseBody
    public ResultVo recommend(@RequestBody DeployPlanParam deployPlanParam) {
        logger.info("recommend deploy plan , param = {}", JSON.toJSONString(deployPlanParam.toString()));

        List<RecommendProjectTask> projectTaskList = deployPlanParam.getProjectTask();
        if (CollectionUtils.isEmpty(projectTaskList)) {
            logger.error("参数错误，应用列表为空");
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("2000", "参数错误，应用列表为空");
        }

        Integer projectId = projectTaskList.get(0).getProjectId();
        if (!releaseCheck(projectId)) {
            logger.error("存在发布中的任务，无法生成发布计划");
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("3000", "存在发布中的任务，无法生成发布计划");
        }

        try {
            // 不用生成发布计划的应用-组-IP,如果发现已经在集合中，说明已经发布过，不用重新发布
            Set<String> unNeedGenerateSet = new HashSet<>();

            // 返回给页面发布计划列表
            List<DeployPlanVo> returnDeployPlanList = new ArrayList<>();

            // 页面传递过来的发布计划列表
            List<DeployPlan> deployPlanList = deployPlanParam.getStepList();
            if (CollectionUtils.isEmpty(deployPlanList)) {
                for (RecommendProjectTask projectTask : projectTaskList) {
                    generateDefaultPlan(projectTask, returnDeployPlanList, unNeedGenerateSet);
                }
            } else {
                // 记录已经生成发布计划的app code，如果有新应用被添加，可以使用该Set做识别
                // 如果已经生成发布的应用，则不能重复生成发布计划
                // Set<String> originPlanSet = new HashSet<>();

                // 最新的应用数据，主键使用 appCode + groupName + hostIp，
                // 遍历发布计划时从该集合中检查，如果没有发现，说明组信息或者IP信息发生了变化
                // 粒度细化到具体某一台服务器，该Map中的所有机器最终需要被全部分配
                Map<String, RecommendProjectTask> latestCodeGroupIpMap = new HashMap<>();

                // 最新的应用数据，主键使用 appCode
                // Map<String, RecommendProjectTask> latestCodeMap = new HashMap<>();
                for (RecommendProjectTask projectTask : projectTaskList) {
                    String appCode = projectTask.getAppCode();
                    /// latestCodeMap.put(appCode, projectTask);

                    List<GroupInfo> groupInfoList = projectTask.getGroupInfo();
                    if (!CollectionUtils.isEmpty(groupInfoList)) {
                        for (GroupInfo groupInfo : groupInfoList) {
                            String groupName = groupInfo.getGroupName() == null ? "" : groupInfo.getGroupName().trim();

                            List<ServerInfo> serverInfoList = groupInfo.getServerInfo();
                            if (!CollectionUtils.isEmpty(serverInfoList)) {
                                for (ServerInfo serverInfo : serverInfoList) {
                                    String hostIp = serverInfo.getHostIp();
                                    String key = appCode + groupName + hostIp;
                                    latestCodeGroupIpMap.put(key, projectTask);
                                }
                            }
                        }
                    }
                }

                // 重新生成的应用-组映射
                Map<String, DeployPlanVo> newAppGroupMap = new HashMap<>();
                for (int index = 0; index < deployPlanList.size(); index++) {
                    DeployPlan oldDeployPlan = deployPlanList.get(index);
                    String appCode = oldDeployPlan.getAppCode();
                    String groupName = oldDeployPlan.getAppGroup() == null ? "" : oldDeployPlan.getAppGroup().trim();

                    List<PlanMachineMapping> planMachineMappingList = oldDeployPlan.getPlanMachineMappings();
                    if (!CollectionUtils.isEmpty(planMachineMappingList)) {
                        Integer projectTaskId = oldDeployPlan.getProjectTaskId();
                        ProjectTask projectTask = projectTaskService.getById(projectTaskId);
                        if (projectTask == null) {
                            logger.info("应用记录不存在 , projectTaskId = {} ", projectTaskId);
                            // 无法找到应用
                        } else if (projectTask.getDelFlag() == 0) {
                            logger.info("应用已经被删除 , projectTaskId = {} ", projectTaskId);
                            // 应用已经被删除
                        } else {
                            String appCodeGroup = appCode + groupName;
                            String appBTag = projectTask.getAppBtag();

                            String planBTag = oldDeployPlan.getAppBtag();
                            Integer disable = oldDeployPlan.getDisable();

                            // 满足不生成发布计划的对象
                            DeployPlanVo notGenerateDeployPlan = new DeployPlanVo();
                            for (PlanMachineMapping planMachineMapping : planMachineMappingList) {
                                String hostIp = planMachineMapping.getServiceIps();
                                if (StringUtils.isEmpty(groupName)) {
                                    groupName = planMachineMapping.getAppGroup();
                                    appCodeGroup = appCode + groupName;
                                }
                                String key = appCodeGroup + hostIp;
                                RecommendProjectTask originProjectTask = latestCodeGroupIpMap.get(key);
                                // 如果原始记录中没有，说明该计划的分组信息或者IP信息已经发生变化
                                // 所以，该发布计划作废；
                                // 只有不为空时，才对该发布的机器进行验证。
                                if (originProjectTask != null) {
                                    // 将已处理的原始数据从集合中删除掉
                                    latestCodeGroupIpMap.remove(key);

                                    Integer deployStatus = planMachineMapping.getDeployStatus();
                                    boolean notGenerate =
                                            (PublishStatusEnum.RELEASE_SUCCESS.getCode().equals(deployStatus) && appBTag.equals(planBTag))
                                                    || Integer.valueOf(0).equals(disable);
                                    // 机器的发布状态为发布完成并且BTag没有变化，或者该批次被禁用，这个批次不用重新生成新的发布计划
                                    if (notGenerate) {
                                        List<PlanMachineMappingVo> newPlanMachineMappingList = notGenerateDeployPlan.getPlanMachineMappings();
                                        if (newPlanMachineMappingList == null) {
                                            newPlanMachineMappingList = new ArrayList<>();
                                            BeanUtils.copyProperties(oldDeployPlan, notGenerateDeployPlan);
                                            notGenerateDeployPlan.setPlanMachineMappings(newPlanMachineMappingList);
                                            notGenerateDeployPlan.setProjectTaskId(projectTaskId);
                                            returnDeployPlanList.add(notGenerateDeployPlan);
                                        }

                                        // 将不用重新生成发布计划的服务器放到返回的发布计划中
                                        PlanMachineMappingVo planMachineMappingVo = new PlanMachineMappingVo();
                                        BeanUtils.copyProperties(planMachineMapping, planMachineMappingVo);
                                        planMachineMappingVo.setProjectId(projectId);
                                        planMachineMappingVo.setProjectTaskId(projectTaskId);
                                        newPlanMachineMappingList.add(planMachineMappingVo);
                                    } else {
                                        DeployPlanVo generateDeployPlan = newAppGroupMap.get(appCodeGroup);
                                        if (generateDeployPlan == null) {
                                            generateDeployPlan = new DeployPlanVo();
                                            generateDeployPlan.setAppBtag(appBTag);
                                            generateDeployPlan.setDelFlag(1);
                                            generateDeployPlan.setDisable(1);
                                            generateDeployPlan.setAppCode(appCode);
                                            generateDeployPlan.setAppGroup(groupName);
                                            generateDeployPlan.setProjectId(projectId);
                                            generateDeployPlan.setProjectTaskId(projectTaskId);
                                            generateDeployPlan.setWaitTime(0);
                                            generateDeployPlan.setDeployStatus(PublishStatusEnum.INIT.getCode());
                                            generateDeployPlan.setDeployStatusDesc(PublishStatusEnum.INIT.getValue());

                                            newAppGroupMap.put(appCodeGroup, generateDeployPlan);
                                            returnDeployPlanList.add(generateDeployPlan);
                                        }
                                        List<PlanMachineMappingVo> newPlanMachineMappingList = generateDeployPlan.getPlanMachineMappings();
                                        if (newPlanMachineMappingList == null) {
                                            newPlanMachineMappingList = new ArrayList<>();
                                            generateDeployPlan.setPlanMachineMappings(newPlanMachineMappingList);
                                        }
                                        PlanMachineMappingVo planMachineMappingVo = new PlanMachineMappingVo();
                                        BeanUtils.copyProperties(planMachineMapping, planMachineMappingVo);
                                        planMachineMappingVo.setDeployStatus(PublishStatusEnum.INIT.getCode());
                                        planMachineMappingVo.setDeployStatusDesc(PublishStatusEnum.INIT.getValue());
                                        planMachineMappingVo.setProjectId(projectId);
                                        planMachineMappingVo.setProjectTaskId(projectTaskId);
                                        planMachineMappingVo.setId(null);
                                        planMachineMappingVo.setPlanId(null);
                                        newPlanMachineMappingList.add(planMachineMappingVo);
                                        StringBuilder builder = new StringBuilder();
                                        newPlanMachineMappingList.stream().forEach(machineMappingVo -> {
                                            if (builder.length() > 0) {
                                                builder.append(",");
                                            }
                                            builder.append(machineMappingVo.getServiceIps());
                                        });
                                        generateDeployPlan.setServiceIps(builder.toString());
                                        generateDeployPlan.setMachineCount(newPlanMachineMappingList.size());
                                    }
                                }
                            }

                            List<PlanMachineMappingVo> newPlanMachineMappingList = notGenerateDeployPlan.getPlanMachineMappings();
                            if (!CollectionUtils.isEmpty(newPlanMachineMappingList)) {
                                StringBuilder builder = new StringBuilder();
                                newPlanMachineMappingList.stream().forEach(planMachineMapping -> {
                                    if (builder.length() > 0) {
                                        builder.append(",");
                                    }
                                    builder.append(planMachineMapping.getServiceIps());
                                });
                                notGenerateDeployPlan.setServiceIps(builder.toString());
                                notGenerateDeployPlan.setMachineCount(newPlanMachineMappingList.size());
                                notGenerateDeployPlan.setDeployStatus(this.getPlanStatus(newPlanMachineMappingList).getCode());
                                notGenerateDeployPlan.setDeployStatusDesc(PublishStatusEnum.getValueByCode(notGenerateDeployPlan.getDeployStatus()));
                            }
                        }
                    }
                }

                // 如果应用的分组列表中有新增数据，该集合才会有数据
                for (Map.Entry<String, RecommendProjectTask> entry : latestCodeGroupIpMap.entrySet()) {
                    String key = entry.getKey();
                    RecommendProjectTask oldProjectTask = entry.getValue();
                    Integer projectTaskId = oldProjectTask.getId();
                    List<GroupInfo> groupInfoList = oldProjectTask.getGroupInfo();
                    ProjectTask projectTask = projectTaskService.getById(projectTaskId);
                    if (projectTask == null) {
                        logger.info("应用记录不存在 , projectTaskId = {} ", projectTaskId);
                        // 无法找到应用
                    } else if (projectTask.getDelFlag() == 0) {
                        logger.info("应用已经被删除 , projectTaskId = {} ", projectTaskId);
                        // 应用已经被删除
                    } else {
                        String appCode = projectTask.getAppCode();
                        String appBTag = projectTask.getAppBtag();
                        projectId = projectTask.getProjectId();
                        for (GroupInfo groupInfo : groupInfoList) {
                            String groupName = groupInfo.getGroupName();
                            String appCodeGroup = appCode + groupName;

                            List<ServerInfo> serverInfoList = groupInfo.getServerInfo();
                            IP_LOOP:
                            for (ServerInfo serverInfo : serverInfoList) {
                                String hostIp = serverInfo.getHostIp();
                                if (!key.equals(appCodeGroup + hostIp)) {
                                    continue;
                                }
                                DeployPlanVo generateDeployPlan = newAppGroupMap.get(appCodeGroup);
                                if (generateDeployPlan == null) {
                                    generateDeployPlan = new DeployPlanVo();
                                    generateDeployPlan.setAppBtag(appBTag);
                                    generateDeployPlan.setDelFlag(1);
                                    generateDeployPlan.setDisable(1);
                                    generateDeployPlan.setAppCode(appCode);
                                    generateDeployPlan.setAppGroup(groupName);
                                    generateDeployPlan.setProjectId(projectId);
                                    generateDeployPlan.setProjectTaskId(projectTaskId);
                                    generateDeployPlan.setWaitTime(0);
                                    generateDeployPlan.setDeployStatus(PublishStatusEnum.INIT.getCode());
                                    generateDeployPlan.setDeployStatusDesc(PublishStatusEnum.INIT.getValue());

                                    newAppGroupMap.put(appCodeGroup, generateDeployPlan);
                                    returnDeployPlanList.add(generateDeployPlan);
                                }
                                List<PlanMachineMappingVo> newPlanMachineMappingList = generateDeployPlan.getPlanMachineMappings();
                                if (newPlanMachineMappingList == null) {
                                    newPlanMachineMappingList = new ArrayList<>();
                                    generateDeployPlan.setPlanMachineMappings(newPlanMachineMappingList);
                                } else {
                                    for (PlanMachineMappingVo planMachineMappingVo : newPlanMachineMappingList) {
                                        if (hostIp.equals(planMachineMappingVo.getServiceIps())) {
                                            // 如果该批次中已经包含该服务器，跳过
                                            continue IP_LOOP;
                                        }
                                    }
                                }
                                PlanMachineMappingVo planMachineMappingVo = new PlanMachineMappingVo();
                                planMachineMappingVo.setServiceIps(hostIp);
                                planMachineMappingVo.setAppGroup(groupName);
                                planMachineMappingVo.setHostName(serverInfo.getHostName());
                                planMachineMappingVo.setHostNameCn(serverInfo.getHostNameCn());
                                planMachineMappingVo.setDisable(1);
                                planMachineMappingVo.setDelFlag(1);
                                planMachineMappingVo.setDeployStatus(PublishStatusEnum.INIT.getCode());
                                planMachineMappingVo.setDeployStatusDesc(PublishStatusEnum.INIT.getValue());
                                planMachineMappingVo.setAppTag(appBTag);
                                planMachineMappingVo.setProjectId(projectId);
                                planMachineMappingVo.setProjectTaskId(projectTaskId);
                                newPlanMachineMappingList.add(planMachineMappingVo);
                                StringBuilder builder = new StringBuilder();
                                newPlanMachineMappingList.stream().forEach(machineMappingVo -> {
                                    if (builder.length() > 0) {
                                        builder.append(",");
                                    }
                                    builder.append(machineMappingVo.getServiceIps());
                                });
                                generateDeployPlan.setServiceIps(builder.toString());
                                generateDeployPlan.setMachineCount(newPlanMachineMappingList.size());
                            }
                        }
                    }
                }
            }
            RecommendPlanVo vo = new RecommendPlanVo();
            vo.setStepList(returnDeployPlanList);
            logger.info("recommend finish , return = {}", JSON.toJSONString(vo));
            return ResultVo.Builder.SUCC().initSuccData(vo);
        } catch (Exception e) {
            logger.info("recommend fail: {}", e);
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("1000", "系统错误");
        }
    }

    /**
     * 生成默认发布计划
     *
     * @param projectTask   单应用
     * @param retDeployPlan 待返回的发布计划列表
     */
    private void generateDefaultPlan(RecommendProjectTask projectTask, List<DeployPlanVo> retDeployPlan, Set<String> unNeedGenerateSet) {
        List<GroupInfo> groupInfoList = projectTask.getGroupInfo();
        Integer projectId = projectTask.getProjectId();
        Integer projectTaskId = projectTask.getId();
        String appCode = projectTask.getAppCode();
        String appBTag = projectTask.getAppBtag();

        // TODO API 是否有分组 ？？？
        if (!CollectionUtils.isEmpty(groupInfoList)) {
            groupInfoList.stream().forEach(groupInfo -> {
                DeployPlanVo deployPlan = new DeployPlanVo();

                final String appGroup = groupInfo.getGroupName() == null ? "" : groupInfo.getGroupName().trim();

                deployPlan.setAppBtag(appBTag);
                deployPlan.setAppCode(appCode);
                deployPlan.setWaitTime(0);
                deployPlan.setDelFlag(1);
                deployPlan.setDeployStatus(PublishStatusEnum.INIT.getCode());
                deployPlan.setDeployStatusDesc(PublishStatusEnum.INIT.getValue());
                deployPlan.setDisable(1);
                deployPlan.setProjectId(projectId);
                deployPlan.setAppGroup(appGroup);
                deployPlan.setProjectTaskId(projectTaskId);

                List<ServerInfo> serverInfoList = groupInfo.getServerInfo();
                if (!CollectionUtils.isEmpty(serverInfoList)) {
                    StringBuilder buffer = new StringBuilder();
                    serverInfoList.stream().forEach(serverInfo -> {
                        String ip = serverInfo.getHostIp();

                        String key = appCode + appGroup + ip;
                        if (unNeedGenerateSet.contains(key)) {
                            return;
                        } else {
                            unNeedGenerateSet.add(key);
                        }

                        PlanMachineMappingVo planMachineMapping = new PlanMachineMappingVo();
                        BeanUtils.copyProperties(projectTask, planMachineMapping);
                        planMachineMapping.setId(null);
                        planMachineMapping.setProjectTaskId(projectTaskId);
                        planMachineMapping.setAppGroup(appGroup);
                        planMachineMapping.setServiceIps(ip);
                        planMachineMapping.setHostName(serverInfo.getHostName());
                        planMachineMapping.setHostNameCn(serverInfo.getHostNameCn());
                        planMachineMapping.setAppTag(appBTag);
                        planMachineMapping.setDisable(1);
                        planMachineMapping.setDeployStatus(PublishStatusEnum.INIT.getCode());
                        planMachineMapping.setDeployStatusDesc(PublishStatusEnum.INIT.getValue());
                        if (buffer.length() > 0) {
                            buffer.append(",");
                        }
                        buffer.append(ip);

                        List<PlanMachineMappingVo> planMachineMappingList = deployPlan.getPlanMachineMappings();
                        if (CollectionUtils.isEmpty(planMachineMappingList)) {
                            planMachineMappingList = new ArrayList<PlanMachineMappingVo>();
                            deployPlan.setPlanMachineMappings(planMachineMappingList);
                        }
                        planMachineMappingList.add(planMachineMapping);
                    });
                    deployPlan.setServiceIps(buffer.toString());
                }

                // 只有部署服务器列表不为空的情况下，才能生成新的发布计划
                if (!CollectionUtils.isEmpty(deployPlan.getPlanMachineMappings())) {
                    retDeployPlan.add(deployPlan);
                }
            });
        } else {
            // TODO jar工程可以不用发布机器 ?
//            if (PackageType.JAR.getValue().equalsIgnoreCase("jar")) {
//                DeployPlanVo deployPlan = new DeployPlanVo();
//
//                deployPlan.setAppBtag(appBTag);
//                deployPlan.setAppCode(appCode);
//                deployPlan.setWaitTime(0);
//                deployPlan.setDelFlag(1);
//                deployPlan.setDeployStatus(PublishStatusEnum.INIT.getCode());
//                deployPlan.setDeployStatusDesc(PublishStatusEnum.INIT.getValue());
//                deployPlan.setDisable(1);
//                deployPlan.setProjectId(projectId);
//                deployPlan.setProjectTaskId(projectTaskId);
//
//                retDeployPlan.add(deployPlan);
//            }

        }
    }

    private boolean releaseCheck(Integer projectId) {
        ReleaseDelpoyRecord releaseDelpoyRecord = new ReleaseDelpoyRecord();
        releaseDelpoyRecord.setProjectId(projectId);
        releaseDelpoyRecord.setRollbackFlag(0);
        List<ReleaseDelpoyRecord> releaseDelpoyRecordList = releaseDelpoyRecordService.listByProjectStatus(releaseDelpoyRecord);
        if (!releaseDelpoyRecordList.isEmpty()) {
            return false;
        }
        return true;
    }
}
