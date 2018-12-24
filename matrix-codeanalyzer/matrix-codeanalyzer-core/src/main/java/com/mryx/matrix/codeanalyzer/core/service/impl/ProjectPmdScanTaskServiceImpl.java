package com.mryx.matrix.codeanalyzer.core.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.mryx.common.utils.HttpPoolClient;
import com.mryx.grampus.ccs.domain.OauthUser;
import com.mryx.matrix.codeanalyzer.core.dao.ProjectPmdScanTaskDao;
import com.mryx.matrix.codeanalyzer.core.service.CodeService;
import com.mryx.matrix.codeanalyzer.core.service.ProjectPmdScanTaskService;
import com.mryx.matrix.codeanalyzer.domain.CodeScanResult;
import com.mryx.matrix.codeanalyzer.domain.ProjectPmdScanTask;
import com.mryx.matrix.codeanalyzer.utils.GitlabUtil;
import com.mryx.matrix.codeanalyzer.utils.GlobalTaskId;
import com.mryx.matrix.common.domain.ResultVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;

@Slf4j
@Service("projectPmdScanTaskService")
public class ProjectPmdScanTaskServiceImpl implements ProjectPmdScanTaskService {
    private final HttpPoolClient HTTP_POOL_CLIENT = com.mryx.common.utils.HttpClientUtil.create(2000, 2000, 5, 5, 1, 500);

    @Value("${deal_accesstocken_url}")
    private String dealAccesstockenUrl;

    @Value("${app_detail_remote}")
    private String appDetailRemote;

    @Value("${pmd_scan_remote}")
    private String pmdScanRemote;

    @Resource
    private CodeService codeService;

    @Resource
    private ProjectPmdScanTaskService projectPmdScanTaskService;

    @Resource
    private ProjectPmdScanTaskDao projectPmdScanTaskDao;

    @Override
    public ResultVo<String> createPmdScanTask(ProjectPmdScanTask projectPmdScanTask) {
        if (projectPmdScanTask == null) {
            log.error("PMD参数异常");
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("1", "PMD参数异常");
        }
        log.info("projectPmdScanTask入参值是{}", projectPmdScanTask);
        String gitAddress = "";
        Integer jobId = 0;
        if (projectPmdScanTask.getId() == null || projectPmdScanTask.getId() == 0) {
            try {
                gitAddress = codeService.getGitAddress(appDetailRemote, projectPmdScanTask.getAppCode()).getData();
                log.info("GIT ADDRESS IS {}", gitAddress);
                if (gitAddress != null && !"".equals(gitAddress)) {
                    if (GitlabUtil.exist(gitAddress, projectPmdScanTask.getCodeBranch())) {
                        projectPmdScanTask.setGitAddress(gitAddress);
                        projectPmdScanTask.setScanTime(new Date());
                        projectPmdScanTask.setStatus(2);
                        projectPmdScanTask.setIsMaster(0);
                        ++GlobalTaskId.taskId;
                        projectPmdScanTask.setTaskId(GlobalTaskId.taskId);
                        synchronized (ProjectPmdScanTaskServiceImpl.class) {
                            Integer insertTask = projectPmdScanTaskService.insertProjectPmdScanTask(projectPmdScanTask);
                            if (insertTask > 0) {
                                log.info("PMD扫描任务insert成功");
                                jobId = projectPmdScanTaskService.getJobIdByAppCode(projectPmdScanTask.getAppCode());
                            } else {
                                log.error("PMD扫描任务入库失败");
                                return ResultVo.Builder.FAIL().initErrCodeAndMsg("2", "代码扫描任务入库失败");
                            }
                        }
                        log.info("gitAddress is {},appBranch is {},projectPmdScanTaskId is {}", gitAddress, projectPmdScanTask.getCodeBranch(), jobId);
                        Map<String, Object> parameters = codeService.setParameters(gitAddress, projectPmdScanTask.getCodeBranch(), jobId.toString());
                        log.info("调用对方PMD扫描接口的入参是{}", parameters.toString());
                        try {
                            Optional<String> pmdScanResult = codeService.codeScanRequest(pmdScanRemote, parameters);
                            if (pmdScanResult.isPresent()) {
                                log.info("pmd扫描任务已经成功返回结果,{} ", pmdScanResult);
                                projectPmdScanTask.setId(jobId);
                                return updatePmdScanResult(projectPmdScanTask, pmdScanResult);
                            } else {
                                log.error("pmd扫描任务创建请求失败");
                                return ResultVo.Builder.FAIL().initErrCodeAndMsg("3", "pmd扫描任务创建请求失败");
                            }
                        } catch (Exception e) {
                            log.error("pmd扫描任务创建请求失败");
                            return ResultVo.Builder.FAIL().initErrCodeAndMsg("4", "pmd扫描任务创建请求失败");
                        }
                    } else {
                        log.info("代码分支不存在");
                        return ResultVo.Builder.FAIL().initErrCodeAndMsg("5", "代码分支不存在");
                    }
                } else {
                    log.error("git 地址获取异常");
                    return ResultVo.Builder.FAIL().initErrCodeAndMsg("6", "git地址获取异常");
                }
            } catch (Exception e) {
                log.error("pmd扫描任务异常失败");
                return ResultVo.Builder.FAIL().initErrCodeAndMsg("7", "pmd扫描任务异常失败");
            }
        } else {
            gitAddress = codeService.getGitAddress(appDetailRemote, projectPmdScanTask.getAppCode()).getData();
            log.info("GIT ADDRESS IS {}", gitAddress);
            if (gitAddress != null && !"".equals(gitAddress)) {
                projectPmdScanTask.setScanTime(new Date());
                projectPmdScanTask.setStatus(2);
                Integer updateTask = projectPmdScanTaskService.updateProjectPmdScanTaskStatus(projectPmdScanTask);
                if (updateTask > 0) {
                    log.info("ProjectCodeScanTask更新成功");
                    Map<String, Object> parameters = codeService.setParameters(gitAddress, projectPmdScanTask.getCodeBranch(), projectPmdScanTask.getId().toString());
                    log.info("重新运行时调用对方pmd扫描接口的入参是{}", parameters.toString());
                    try {
                        Optional<String> pmdScanResult = codeService.codeScanRequest(pmdScanRemote, parameters);
                        if (pmdScanResult.isPresent()) {
                            log.info("重新运行时pmd扫描任务已经成功返回结果,{} ", pmdScanResult);
                            return updatePmdScanResult(projectPmdScanTask, pmdScanResult);
                        } else {
                            log.error("重新运行时pmd扫描任务创建请求失败");
                            return ResultVo.Builder.FAIL().initErrCodeAndMsg("3", "重新运行时pmd扫描任务创建请求失败");
                        }
                    } catch (Exception e) {
                        log.error("重新运行时pmd扫描任务创建请求失败");
                        return ResultVo.Builder.FAIL().initErrCodeAndMsg("4", "重新运行时pmd扫描任务创建请求失败");
                    }
                } else {
                    log.error("重新运行时ProjectPmdScanTask更新失败");
                    return ResultVo.Builder.FAIL().initErrCodeAndMsg("8", "重新运行时ProjectPmdScanTask更新失败");
                }
            } else {
                log.error("重新运行时git 地址获取异常");
                return ResultVo.Builder.FAIL().initErrCodeAndMsg("6", "重新运行时git地址获取异常");
            }
        }
    }

    private ResultVo<String> updatePmdScanResult(ProjectPmdScanTask projectPmdScanTask, Optional<String> pmdScanResult) {
        log.info("projectPmdScanTask is {}, pmdScanResult is {} ", projectPmdScanTask, pmdScanResult);
        JSONObject pmdResult = (JSONObject) JSONObject.parse(pmdScanResult.get().toString());
        if ("success".equals(pmdResult.get("ret")) && "0".equals(pmdResult.get("code"))) {
            if (null == pmdResult.get("data") || pmdResult.get("data").toString() == "") {
                return ResultVo.Builder.FAIL().initErrCodeAndMsg("11", "PMD扫描返回值data为空");
            } else {
                JSONObject data = (JSONObject) JSONObject.parse(pmdResult.get("data").toString());
                Integer blocker = Integer.valueOf(data.get("Blocker").toString());
                Integer critical = Integer.valueOf(data.get("Critical").toString());
                Integer major = Integer.valueOf(data.get("Major").toString());
                Integer minor = Integer.valueOf(data.get("Minor").toString());
                Integer info = Integer.valueOf(data.get("Info").toString());
                log.info("blocker is {},critical is {},major is {},minor is {},info is {}", blocker, critical, major, minor, info);
                projectPmdScanTask.setBlocker(blocker);
                projectPmdScanTask.setCritical(critical);
                projectPmdScanTask.setMajor(major);
                projectPmdScanTask.setMinor(minor);
                projectPmdScanTask.setInfo(info);
                projectPmdScanTask.setStatus(0);
                String pmdUrl = pmdResult.get("url").toString();
                log.info("pmd url is {}",pmdUrl);
                projectPmdScanTask.setPmdScanResultUrl(pmdUrl);
                Integer updatePmd = projectPmdScanTaskService.updateProjectPmdScanTask(projectPmdScanTask);
                if (updatePmd > 0) {
                    log.info("更新PMD扫描结果成功");
                    return ResultVo.Builder.SUCC().initSuccDataAndMsg("12", "更新PMD扫描结果成功");
                } else {
                    log.error("更新PMD扫描结果失败");
                    return ResultVo.Builder.FAIL().initErrCodeAndMsg("13", "更新PMD扫描结果失败");
                }
            }
        } else if ("fail".equals(pmdResult.get("ret")) && "1".equals(pmdResult.get("code"))) {
            log.error("PMD扫描失败，fail掉了");
            projectPmdScanTask.setStatus(1);
            Integer updatePmd = projectPmdScanTaskService.updateProjectPmdScanTaskStatus(projectPmdScanTask);
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("10", "PMD扫描失败，fail掉了");
        } else {
            log.error("PMD扫描返回值异常");
            projectPmdScanTask.setStatus(1);
            Integer updatePmd = projectPmdScanTaskService.updateProjectPmdScanTaskStatus(projectPmdScanTask);
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("9", "PMD扫描返回值异常");
        }
    }

    @Override
    public Integer insertProjectPmdScanTask(ProjectPmdScanTask projectPmdScanTask) {
        return projectPmdScanTaskDao.insertProjectPmdScanTask(projectPmdScanTask);
    }

    @Override
    public Integer updateProjectPmdScanTask(CodeScanResult codeScanResult) {
        return projectPmdScanTaskDao.updatePmdScanTask(codeScanResult);
    }

    @Override
    public Integer updateProjectPmdScanTask(ProjectPmdScanTask projectPmdScanTask) {
        return projectPmdScanTaskDao.updateProjectPmdScanTask(projectPmdScanTask);
    }

    @Override
    public Integer updateProjectPmdScanTaskStatus(ProjectPmdScanTask projectPmdScanTask) {
        return projectPmdScanTaskDao.updateProjectPmdScanTaskStatus(projectPmdScanTask);
    }

    @Override
    public Integer updateProjectPmdScanTaskStatus(CodeScanResult codeScanResult) {
        return projectPmdScanTaskDao.updatePmdScanTaskStatus(codeScanResult);
    }

    @Override
    public Integer pageTotal(ProjectPmdScanTask projectPmdScanTask) {
        return projectPmdScanTaskDao.pageTotal(projectPmdScanTask);
    }

    @Override
    public List<ProjectPmdScanTask> getPmdScanTask(ProjectPmdScanTask projectPmdScanTask) {
        return projectPmdScanTaskDao.getPmdScanTask(projectPmdScanTask);
    }

    @Override
    public Integer getJobIdByAppCode(String appCode) {
        return projectPmdScanTaskDao.getJobIdByAppCode(appCode);
    }
}
