package com.mryx.matrix.publish.web.controller;

import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.core.JsonFactory;
import com.mryx.matrix.publish.core.service.DeployPlanService;
import com.mryx.matrix.publish.core.service.ProjectTaskBatchService;
import com.mryx.matrix.publish.core.service.ProjectTaskService;
import com.mryx.matrix.publish.domain.DeployPlan;
import com.mryx.matrix.publish.domain.ProjectTask;
import com.mryx.matrix.common.domain.ResultVo;
import com.mryx.matrix.publish.domain.ProjectTaskBatch;
import com.mryx.matrix.publish.enums.PublishStatusEnum;
import com.mryx.matrix.publish.web.vo.ProjectTaskVo;
import com.mryx.matrix.publish.web.vo.PublishSequeneceVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author dinglu
 * @date 2018/9/10
 */

@RestController
@RequestMapping("/api/publish/projectTask/")
public class ProjectTaskController {

    private static final Logger logger = LoggerFactory.getLogger(ProjectTaskController.class);

    @Resource
    ProjectTaskService projectTaskService;

    @Resource
    ProjectTaskBatchService projectTaskBatchService;

    @Resource
    DeployPlanService deployPlanService;

    @PostMapping("/save")
    @ResponseBody
    public ResultVo save(@RequestBody List<ProjectTaskVo> projectTaskVoList) {
        try {
            if (projectTaskVoList.isEmpty() || projectTaskVoList.size() <= 0) {
                logger.info("projectTask save参数异常, param = {}", projectTaskVoList.toString());
                return ResultVo.Builder.FAIL().initErrCodeAndMsg("2000", "参数错误");
            }
            ProjectTask projectTask = new ProjectTask();
            projectTask.setProjectId(projectTaskVoList.get(0).getProjectId());
            List<ProjectTask> projectTasks = projectTaskService.listByCondition(projectTask);
            projectTaskService.batchDelete(projectTasks);
            List<ProjectTask> projectTaskList = projectTaskVoList.stream().map(projectTaskVo -> {
                ProjectTask projectTask1 = new ProjectTask();
                BeanUtils.copyProperties(projectTaskVo, projectTask1);
                if (projectTaskVo.getIsDeploy()) {
                    projectTask1.setDelFlag(0);
                } else {
                    projectTask1.setDelFlag(1);
                }
                return projectTask1;
            }).collect(Collectors.toList());
            int result = projectTaskService.batchInsert(projectTaskList);
            if (result <= 0) {
                return ResultVo.Builder.FAIL().initErrCodeAndMsg("2001", "保存失败");
            } else {
                return ResultVo.Builder.SUCC();
            }
        } catch (Exception e) {
            logger.error("projectTask save Exception,Exception = {}", e);
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("4000", "系统异常");
        }
    }

    @PostMapping("/insert")
    @ResponseBody
    public ResultVo insert(@RequestBody ProjectTask projectTask) {
        try {
            if (projectTask == null || StringUtils.isEmpty(projectTask.getAppCode())
                    || StringUtils.isEmpty(projectTask.getAppBranch())) {
                logger.info("projectTask insert fail , param = {}", projectTask);
                return ResultVo.Builder.FAIL().initErrCodeAndMsg("2000", "参数错误");
            }
            int result = projectTaskService.insert(projectTask);
            if (result <= 0) {
                logger.info("projectTask insert fail , param = {}", projectTask);
                return ResultVo.Builder.FAIL().initErrCodeAndMsg("2001", "新增失败");
            }
            return ResultVo.Builder.SUCC();
        } catch (Exception e) {
            logger.error("projectTask insert exception , e = {}", e);
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("4000", "系统异常");
        }
    }

    @PostMapping("/update")
    @ResponseBody
    public ResultVo update(@RequestBody ProjectTask projectTask) {
        try {
            if (projectTask == null || projectTask.getId() == null || projectTask.getId() <= 0) {
                logger.info("projectTask update fail , param = {}", projectTask);
                return ResultVo.Builder.FAIL().initErrCodeAndMsg("2000", "参数错误");
            }
            int result = projectTaskService.updateById(projectTask);
            if (result <= 0) {
                return ResultVo.Builder.FAIL().initErrCodeAndMsg("2001", "更新失败");
            }

            final String appRTag = projectTask.getAppRtag();
            if (!StringUtils.isEmpty(appRTag)) {
                DeployPlan condition = new DeployPlan();
                condition.setRollbackFlag(1);
                condition.setDelFlag(1);
                condition.setProjectId(projectTask.getProjectId());
                condition.setAppCode(projectTask.getAppCode());
                List<DeployPlan> deployPlanList = deployPlanService.listByCondition(condition);
                if (!CollectionUtils.isEmpty(deployPlanList)) {
                    deployPlanList.stream().forEach(deployPlan -> {
                        deployPlan.setAppRtag(appRTag);
                        deployPlanService.updateById(deployPlan);
                    });
                }
            }
            return ResultVo.Builder.SUCC();
        } catch (Exception e) {
            logger.error("projectTask update exception , e = {}", e);
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("4000", "系统异常");
        }
    }

    @PostMapping("/getByProjectId")
    @ResponseBody
    public ResultVo getByProjectId(@RequestBody ProjectTask projectTask) {
        ResultVo resultVo = new ResultVo();
        try {
            if (projectTask == null) {
                logger.info("projectTask list参数异常, projectTask = {}", projectTask);
                return resultVo.initErrCodeAndMsg("2000", "参数错误");
            }
            List<ProjectTask> projectTaskList = projectTaskService.listByCondition(projectTask);
            /*for (ProjectTask projectTask1: projectTaskList) {
                if (projectTask1.getId() == null || projectTask1.getId() <= 0){
                    continue;
                }
                List<ProjectTaskBatch> projectTaskBatchList = projectTaskBatchService.getByProjectTaskId(projectTask1.getId());
                projectTask1.setProjectTaskBatchList(projectTaskBatchList);
            }*/
            List<ProjectTaskVo> projectTaskVoList = projectTaskList.stream().map(projectTask1 -> {
                ProjectTaskVo projectTaskVo = new ProjectTaskVo();
                BeanUtils.copyProperties(projectTask1, projectTaskVo);
                List<ProjectTaskBatch> projectTaskBatchList = projectTaskBatchService.getByProjectTaskId(projectTask1.getId());
                projectTaskVo.setProjectTaskBatchList(projectTaskBatchList);
                if (projectTask1.getTaskStatus().equals(PublishStatusEnum.BETA_FAIL.getCode()) && projectTask1.getSubTaskStatus() != null) {
                    projectTaskVo.setTaskStatusDesc(PublishStatusEnum.getValueByCode(PublishStatusEnum.BETA_FAIL.getCode()) + "-"
                            + PublishStatusEnum.getValueByCode(projectTask1.getSubTaskStatus()));
                } else {
                    projectTaskVo.setTaskStatusDesc(PublishStatusEnum.getValueByCode(projectTask1.getTaskStatus()));
                }
                if (projectTask1.getReleaseTaskStatus().equals(PublishStatusEnum.RELEASE_FAIL.getCode()) && projectTask1.getSubReleaseTaskStatus() != null) {
                    projectTaskVo.setReleaseTaskStatusDesc(PublishStatusEnum.getValueByCode(PublishStatusEnum.RELEASE_FAIL.getCode())
                            + "-" + PublishStatusEnum.getValueByCode(projectTask1.getSubReleaseTaskStatus()));
                } else {
                    projectTaskVo.setReleaseTaskStatusDesc(PublishStatusEnum.getValueByCode(projectTask1.getReleaseTaskStatus()));
                }
                if (projectTask1.getIsDeploy() == 0) {
                    projectTaskVo.setIsDeploy(true);
                } else {
                    projectTaskVo.setIsDeploy(false);
                }
                return projectTaskVo;
            }).collect(Collectors.toList());
            return ResultVo.Builder.SUCC().initSuccData(projectTaskVoList);
        } catch (Exception e) {
            logger.error("projectTask list Exception,e ={}", e);
            return resultVo.initErrCodeAndMsg("4000", "系统异常");
        }
    }

    @PostMapping("/pageTotal")
    @ResponseBody
    public ResultVo pageTotal(@RequestBody ProjectTask projectTask) {
        ResultVo resultVo = new ResultVo();
        try {
            int count = projectTaskService.pageTotal(projectTask);
            return ResultVo.Builder.SUCC().initSuccData(Integer.toString(count));
        } catch (Exception e) {
            return resultVo.initErrCodeAndMsg("4000", "系统异常");
        }
    }

    @PostMapping("/list")
    @ResponseBody
    public ResultVo list(@RequestBody ProjectTask projectTask) {
        ResultVo resultVo = new ResultVo();
        try {
            List<ProjectTask> projectTaskList = projectTaskService.listPage(projectTask);
            List<ProjectTaskVo> projectTaskVoList = projectTaskList.stream().map(projectTask1 -> {
                ProjectTaskVo projectTaskVo = new ProjectTaskVo();
                BeanUtils.copyProperties(projectTask1, projectTaskVo);
                if (projectTask1.getTaskStatus().equals(PublishStatusEnum.BETA_FAIL.getCode()) && projectTask1.getSubTaskStatus() != null) {
                    projectTaskVo.setTaskStatusDesc(PublishStatusEnum.getValueByCode(PublishStatusEnum.BETA_FAIL.getCode()) + "-"
                            + PublishStatusEnum.getValueByCode(projectTask1.getSubTaskStatus()));
                } else {
                    projectTaskVo.setTaskStatusDesc(PublishStatusEnum.getValueByCode(projectTask1.getTaskStatus()));
                }
                if (projectTask1.getReleaseTaskStatus().equals(PublishStatusEnum.RELEASE_FAIL.getCode())
                        && projectTask1.getSubReleaseTaskStatus() != null) {
                    projectTaskVo.setReleaseTaskStatusDesc(PublishStatusEnum.getValueByCode(PublishStatusEnum.RELEASE_FAIL.getCode())
                            + "-" + PublishStatusEnum.getValueByCode(projectTask1.getSubReleaseTaskStatus()));
                } else {
                    projectTaskVo.setReleaseTaskStatusDesc(PublishStatusEnum.getValueByCode(projectTask1.getReleaseTaskStatus()));
                }
                if (projectTask1.getIsDeploy() == 0) {
                    projectTaskVo.setIsDeploy(true);
                } else {
                    projectTaskVo.setIsDeploy(false);
                }
                return projectTaskVo;
            }).collect(Collectors.toList());
            return ResultVo.Builder.SUCC().initSuccData(projectTaskVoList);
        } catch (Exception e) {
            return resultVo;
        }
    }

    @PostMapping("/getById")
    @ResponseBody
    public ResultVo getById(@RequestBody ProjectTask projectTaskParam) {
        try {
            Integer id = 0;
            if (projectTaskParam != null) {
                id = projectTaskParam.getId();
            }
            if (id == null || id <= 0) {
                logger.info("projectTask get by id fail , param = {}", id);
                return ResultVo.Builder.FAIL().initErrCodeAndMsg("2000", "参数错误");
            }
            ProjectTask projectTask = projectTaskService.getById(id);
            ProjectTaskVo projectTaskVo = new ProjectTaskVo();
            if (projectTask != null) {
                BeanUtils.copyProperties(projectTask, projectTaskVo);
                if (projectTask.getTaskStatus().equals(PublishStatusEnum.BETA_FAIL.getCode()) && projectTask.getSubTaskStatus() != null) {
                    projectTaskVo.setTaskStatusDesc(PublishStatusEnum.getValueByCode(PublishStatusEnum.BETA_FAIL.getCode()) + "-"
                            + PublishStatusEnum.getValueByCode(projectTask.getSubTaskStatus()));
                } else {
                    projectTaskVo.setTaskStatusDesc(PublishStatusEnum.getValueByCode(projectTask.getTaskStatus()));
                }
                if (projectTask.getReleaseTaskStatus().equals(PublishStatusEnum.RELEASE_FAIL.getCode())
                        && projectTask.getSubReleaseTaskStatus() != null) {
                    projectTaskVo.setReleaseTaskStatusDesc(PublishStatusEnum.getValueByCode(PublishStatusEnum.RELEASE_FAIL.getCode())
                            + "-" + PublishStatusEnum.getValueByCode(projectTask.getSubReleaseTaskStatus()));
                } else {
                    projectTaskVo.setReleaseTaskStatusDesc(PublishStatusEnum.getValueByCode(projectTask.getReleaseTaskStatus()));
                }
                if (projectTask.getIsDeploy() == 0) {
                    projectTaskVo.setIsDeploy(true);
                } else {
                    projectTaskVo.setIsDeploy(false);
                }
            }
            return ResultVo.Builder.SUCC().initSuccData(projectTaskVo);
        } catch (Exception e) {
            logger.error("projectTask get by id exception , Exception = {}", e);
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("4000", "系统异常");
        }
    }

}
