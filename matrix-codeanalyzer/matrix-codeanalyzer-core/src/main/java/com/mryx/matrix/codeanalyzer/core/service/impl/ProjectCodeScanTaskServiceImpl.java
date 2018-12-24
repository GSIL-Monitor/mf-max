package com.mryx.matrix.codeanalyzer.core.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.mryx.common.utils.HttpPoolClient;
import com.mryx.grampus.ccs.domain.OauthUser;
import com.mryx.matrix.codeanalyzer.core.dao.CodeDao;
import com.mryx.matrix.codeanalyzer.core.dao.ProjectCodeScanTaskDao;
import com.mryx.matrix.codeanalyzer.core.service.CodeService;
import com.mryx.matrix.codeanalyzer.core.service.JobManagerService;
import com.mryx.matrix.codeanalyzer.core.service.ProjectCodeScanTaskService;
import com.mryx.matrix.codeanalyzer.core.service.ProjectPmdScanTaskService;
import com.mryx.matrix.codeanalyzer.core.task.P3cScanJob;
import com.mryx.matrix.codeanalyzer.core.task.PmdScanJob;
import com.mryx.matrix.codeanalyzer.core.task.SonarScanJob;
import com.mryx.matrix.codeanalyzer.domain.CodeScanJob;
import com.mryx.matrix.codeanalyzer.domain.CodeScanJobRecord;
import com.mryx.matrix.codeanalyzer.domain.CodeScanResult;
import com.mryx.matrix.codeanalyzer.utils.GitlabUtil;
import com.mryx.matrix.common.domain.ResultVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import com.mryx.common.utils.HttpClientUtil;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.concurrent.*;

@Slf4j
@Service("projectCodeScanTaskService")
public class ProjectCodeScanTaskServiceImpl implements ProjectCodeScanTaskService {
    private static final ExecutorService EXECUTE_SHELL = new ThreadPoolExecutor(10, 20, 10, TimeUnit.MINUTES, new LinkedBlockingQueue<>(10));
    private final HttpPoolClient HTTP_POOL_CLIENT = HttpClientUtil.create(2000, 2000, 5, 5, 1, 500);

    private final static Integer SCORE = 100;
    private final static Integer BLOCKER_SCORE = 10;
    private final static Integer CRITICAL_SCORE = 10;
    private final static Integer MAJOR_SCORE = 10;
    private final static Integer MINOR_SCORE = 10;
    private final static Integer INFO_SCORE = 10;

    @Resource
    JobManagerService jobManagerService;

    @Resource
    private ProjectCodeScanTaskDao projectCodeScanTaskDao;

    @Resource
    private CodeDao codeDao;

    @Resource
    private CodeService codeService;

    @Resource
    private ProjectCodeScanTaskService projectCodeScanTaskService;

    @Resource
    private ProjectPmdScanTaskService projectPmdScanTaskService;

    @Value("${app_detail_remote}")
    private String appDetailRemote;

    @Value("${code_scan_callback_remote}")
    private String codeScanCallbackRemote;

    @Value("${deal_accesstocken_url}")
    private String dealAccesstockenUrl;

    @Value("${pmd_scan_remote}")
    private String pmdScanRemote;

    @Override
    public ResultVo<String> createCodeScanJob(CodeScanJob codeScanJob) {
        if (codeScanJob == null) {
            log.error("参数异常");
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("1", "参数异常");
        }
        String gitAddress = "";
        try {
            gitAddress = codeService.getGitAddress(appDetailRemote, codeScanJob.getAppCode()).getData();
            log.info("GIT ADDRESS IS {}", gitAddress);
            if (gitAddress != null && !"".equals(gitAddress)) {
                if (GitlabUtil.exist(gitAddress, codeScanJob.getCodeBranch())) {
                    codeScanJob.setGitAddress(gitAddress);
                    codeScanJob.setJobStatus(2);
                    log.info("Job中codeScanJob入参值是{}", codeScanJob);
                    synchronized (ProjectCodeScanTaskServiceImpl.class) {
                        Integer insertCode = projectCodeScanTaskService.insertCodeScanJob(codeScanJob);
                        if (insertCode > 0) {
                            log.info("p3c扫描任务入库成功，{}", insertCode.toString());
                            Integer jobId = projectCodeScanTaskService.getJobIdByAppCode(codeScanJob);
                            if (jobId > 0) {
                                log.info("jobId is {}", jobId);
                                Integer typeOfScan = codeScanJob.getTypeOfScan();
                                if (typeOfScan != null) {
                                    if (typeOfScan == 2) {
                                        log.info("typeOfScan is {}", typeOfScan);
                                        return sendP3cScanJob(codeScanJob, jobId, projectCodeScanTaskService);
                                    }
                                }
                            } else {
                                log.error("获取jobID失败");
                                return ResultVo.Builder.FAIL().initErrCodeAndMsg("6", "获取jobID失败");
                            }
                        } else {
                            log.error("p3c代码扫描任务入库失败");
                            return ResultVo.Builder.FAIL().initErrCodeAndMsg("5", "p3c代码扫描任务入库失败");
                        }
                    }
                } else {
                    log.info("代码分支不存在");
                    return ResultVo.Builder.FAIL().initErrCodeAndMsg("4", "代码分支不存在");
                }
            } else {
                log.error("git地址获取异常");
                return ResultVo.Builder.FAIL().initErrCodeAndMsg("3", "git地址获取异常");
            }
        } catch (Exception e) {
            log.error("p3c代码扫描任务异常失败");
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("2", "p3c代码扫描任务异常失败");
        }
        return ResultVo.Builder.SUCC();
    }

    private ResultVo<String> sendPmdScanJob(CodeScanResult codeScanResult, Integer jobId, ProjectPmdScanTaskService projectPmdScanTaskService) {
        try {
            Callable pmdCallable = new Callable() {
                @Override
                public ResultVo<String> call() throws Exception {
                    log.info("【pmd定时扫描任务启动】");
                    Boolean addJob = jobManagerService.addJob(PmdScanJob.class, pmdScanRemote, codeScanResult, jobId.toString(), projectPmdScanTaskService);
                    if (addJob) {
                        log.info("【pmd定时任务添加成功】");
                        return ResultVo.Builder.SUCC().initSuccDataAndMsg("0", "pmd定时任务添加成功");
                    } else {
                        log.error("pmd定时任务添加失败");
                        return ResultVo.Builder.FAIL().initErrCodeAndMsg("11", "pmd定时任务添加失败");
                    }
                }
            };
            EXECUTE_SHELL.submit(pmdCallable);
        } catch (Exception e) {
            log.error("pmd定时任务添加失败");
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("7", "pmd定时任务添加失败");
        }
        return ResultVo.Builder.SUCC().initSuccDataAndMsg("0", "pmd定时任务添加成功");
    }

    private ResultVo<String> sendSonarScanJob(CodeScanJob codeScanJob, Integer jobId) {
        try {
            log.info("【sonar定时扫描任务启动】");
            Boolean addJob = jobManagerService.addJob(SonarScanJob.class, codeScanCallbackRemote, codeScanJob, jobId.toString());
            if (addJob) {
                log.info("【sonar定时任务添加成功】");
                return ResultVo.Builder.SUCC().initSuccDataAndMsg("0", "sonar定时任务添加成功");
            } else {
                log.error("sonar定时任务添加失败");
                return ResultVo.Builder.FAIL().initErrCodeAndMsg("11", "sonar定时任务添加失败");
            }
        } catch (Exception e) {
            log.error("sonar定时任务添加失败");
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("11", "sonar定时任务添加失败");
        }
    }

    private ResultVo<String> sendP3cScan(Map<String, Object> parameters) {
        try {
            Optional<String> p3cResult = codeService.codeScanRequest(pmdScanRemote, parameters);
            if (p3cResult.isPresent()) {
                log.info("p3c扫描任务创建成功，已经成功调用对方接口,{} ", p3cResult);
                return ResultVo.Builder.SUCC().initSuccDataAndMsg("0", "p3c扫描任务创建成功，已经成功调用对方接口");
            } else {
                log.error("p3c扫描任务创建请求失败");
                return ResultVo.Builder.FAIL().initErrCodeAndMsg("9", "p3c扫描任务创建请求失败");
            }
        } catch (Exception e) {
            log.error("p3c扫描任务创建请求失败");
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("9", "p3c扫描任务创建请求失败");
        }
    }

    private ResultVo<String> sendP3cScanJob(CodeScanJob codeScanJob, Integer jobId, ProjectCodeScanTaskService projectCodeScanTaskService) {
        try {
            log.info("【P3c定时任务添加............】");
            Boolean addJob = jobManagerService.addJob(P3cScanJob.class, pmdScanRemote, codeScanJob, jobId.toString(), projectCodeScanTaskService);
            if (addJob) {
                log.info("【P3c定时任务添加成功】");
                return ResultVo.Builder.SUCC().initSuccDataAndMsg("0", "P3c定时任务添加成功");
            } else {
                log.error("P3c定时任务添加失败");
                return ResultVo.Builder.FAIL().initErrCodeAndMsg("8", "P3c定时任务添加失败");
            }
        } catch (Exception e) {
            log.error("P3c定时任务添加异常");
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("7", "P3c定时任务添加异常");
        }
    }

    @Override
    public List<CodeScanResult> getCodeScanInfo() {
        return projectCodeScanTaskDao.getCodeScanInfo();
    }

    @Override
    public Integer insertCodeScanJob(CodeScanJob codeScanJob) {
        return projectCodeScanTaskDao.insertCodeScanJob(codeScanJob);
    }

    @Override
    public Integer insertProjectCodeScanTask(CodeScanResult codeScanResult) {
        return projectCodeScanTaskDao.insertProjectCodeScanTask(codeScanResult);
    }


    @Override
    public ResultVo<String> createCodeScanTask(CodeScanResult codeScanResult) {
        if (codeScanResult == null) {
            log.error("参数异常");
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("1", "参数异常");
        }
        log.info("CodeScanResult入参值是{}", codeScanResult);
        String gitAddress = "";
        if (StringUtils.isEmpty(codeScanResult.getId())) {
            try {
                gitAddress = codeService.getGitAddress(appDetailRemote, codeScanResult.getAppCode()).getData();
                log.info("GIT ADDRESS IS {}", gitAddress);
                if (gitAddress != null && !"".equals(gitAddress)) {
                    if (GitlabUtil.exist(gitAddress, codeScanResult.getCodeBranch())) {
                        codeScanResult.setGitAddress(gitAddress);
                        codeScanResult.setManualOrAutomatic(1);
                        codeScanResult.setCodeScanTime(new Date());
                        codeScanResult.setStatus(2);
                        codeScanResult.setIsMaster(0);
                        log.info("CodeScanResult is {}", codeScanResult);
                        synchronized (ProjectCodeScanTaskServiceImpl.class) {
                            Integer insertCode = projectCodeScanTaskService.insertProjectCodeScanTask(codeScanResult);
                            if (insertCode > 0) {
                                log.info("代码扫描任务入库成功，{}", insertCode.toString());
                                log.info("appCode is {}", codeScanResult.getAppCode());
                                Integer jobId = projectCodeScanTaskService.getIdByAppCode(codeScanResult);
                                if (jobId > 0) {
                                    Map<String, Object> parameters = codeService.setParameters(gitAddress, codeScanResult.getCodeBranch(), jobId.toString());
                                    log.info("调用对方代码扫描接口的入参是{}", parameters.toString());
                                    Integer typeOfScan = codeScanResult.getTypeOfScan();
                                    if (typeOfScan != null) {
                                        if (typeOfScan == 0) {
                                            return sendSonarScan(parameters);
                                        } else if (typeOfScan == 1) {
                                            return sendPmdScan(codeScanResult, jobId, parameters);
                                        }
                                    }
                                } else {
                                    log.error("获取jobID失败");
                                    return ResultVo.Builder.FAIL().initErrCodeAndMsg("6", "获取jobID失败");
                                }
                            } else {
                                log.error("代码扫描任务入库失败");
                                return ResultVo.Builder.FAIL().initErrCodeAndMsg("5", "代码扫描任务入库失败");
                            }
                        }
                    } else {
                        log.info("代码分支不存在");
                        return ResultVo.Builder.FAIL().initErrCodeAndMsg("4", "代码分支不存在");
                    }
                } else {
                    log.error("git 地址获取异常");
                    return ResultVo.Builder.FAIL().initErrCodeAndMsg("3", "git地址获取异常");
                }
            } catch (Exception e) {
                log.error("代码扫描任务异常失败");
                return ResultVo.Builder.FAIL().initErrCodeAndMsg("2", "代码扫描任务异常失败");
            }
        } else {
            gitAddress = codeService.getGitAddress(appDetailRemote, codeScanResult.getAppCode()).getData();
            log.info("GIT ADDRESS IS {}", gitAddress);
            if (gitAddress != null && !"".equals(gitAddress)) {
                Integer typeOfScan = codeScanResult.getTypeOfScan();
                if (typeOfScan != null) {
                    codeScanResult.setCodeScanTime(new Date());
                    codeScanResult.setStatus(2);
                    Integer updateResult = projectCodeScanTaskService.updateProjectCodeScanTask(codeScanResult);
                    if (updateResult > 0) {
                        log.info("重新运行时codeScanResult更新成功");
                        Map<String, Object> parameters = codeService.setParameters(gitAddress, codeScanResult.getCodeBranch(), codeScanResult.getId().toString());
                        log.info("重新运行时调用对方代码扫描接口的入参是{}", parameters.toString());
                        if (typeOfScan == 0) {
                            return retrySendSonarScan(parameters);
                        } else if (typeOfScan == 1) {
                            return retrySendPmdScan(codeScanResult, parameters);
                        }
                    } else {
                        log.error("重新运行时codeScanResult更新失败");
                        return ResultVo.Builder.FAIL().initErrCodeAndMsg("11", "重新运行时ProjectCodeScanTask更新运行人失败");
                    }
                } else {
                    log.error("获取jobID失败");
                    return ResultVo.Builder.FAIL().initErrCodeAndMsg("5", "获取jobID失败");
                }
            } else {
                log.error("重新运行时git 地址获取异常");
                return ResultVo.Builder.FAIL().initErrCodeAndMsg("3", "重新运行时git地址获取异常");
            }
        }
        return ResultVo.Builder.SUCC();
    }

    @Override
    public ResultVo<String> manualRunP3cJob(CodeScanJob codeScanJob) {
        if (codeScanJob == null) {
            log.error("参数异常");
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("1", "参数异常");
        }
        log.info("手动运行p3c扫描任务时CodeScanResult入参值是{}", codeScanJob);
        Integer jobId = codeScanJob.getId();
        String gitAddress = "";
        if (!StringUtils.isEmpty(jobId)) {
            try {
                gitAddress = codeService.getGitAddress(appDetailRemote, codeScanJob.getAppCode()).getData();
                log.info("GIT ADDRESS IS {}", gitAddress);
                if (gitAddress != null && !"".equals(gitAddress)) {
                    codeScanJob.setCodeScanTime(new Date());
                    codeScanJob.setJobStatus(2);
                    Integer updateResult = projectCodeScanTaskService.updateCodeScanJob(codeScanJob);
                    if (updateResult > 0) {
                        log.info("手动运行p3c扫描任务时codeScanResult更新成功");
                        CodeScanJobRecord codeScanJobRecord = new CodeScanJobRecord();
                        codeScanJobRecord.setJobId(jobId);
                        codeScanJobRecord.setManualOrAutomatic(0);
                        codeScanJobRecord.setCodeScanTime(new Date());
                        codeScanJobRecord.setTypeOfScan(2);
                        codeScanJobRecord.setJobStatus(2);
                        synchronized (ProjectCodeScanTaskServiceImpl.class) {
                            Integer insertRecord = projectCodeScanTaskService.insertP3cRecord(codeScanJobRecord);
                            if (insertRecord > 0) {
                                log.info("jobID is {}", jobId);
                                Integer recordId = projectCodeScanTaskService.getRecordIdByJobId(codeScanJobRecord);
                                if (recordId > 0) {
                                    log.info("recordId is {}", recordId);
                                    String sendId = jobId + "a" + recordId;
                                    Map<String, Object> parameters = codeService.setParameters(gitAddress, codeScanJob.getCodeBranch(), sendId);
                                    log.info("手动运行p3c扫描请求参数是{}", parameters);
                                    Optional<String> scanResult = HTTP_POOL_CLIENT.postJson(pmdScanRemote, JSONObject.toJSONString(parameters));
                                    if (scanResult.isPresent()) {
                                        log.info("手动运行p3c扫描任务创建成功，已经成功调用对方接口,{} ", scanResult);
                                        return ResultVo.Builder.SUCC().initSuccDataAndMsg("0", "手动运行p3c扫描任务创建成功，已经成功调用对方接口");
                                    } else {
                                        log.error("手动运行p3c扫描任务创建请求失败");
                                        return ResultVo.Builder.FAIL().initErrCodeAndMsg("5", "手动运行p3c扫描任务创建请求失败");
                                    }
                                } else {
                                    log.error("获取任务记录ID失败");
                                    return ResultVo.Builder.FAIL().initErrCodeAndMsg("6", "获取任务记录ID失败");
                                }
                            } else {
                                log.error("任务记录入库失败");
                                return ResultVo.Builder.FAIL().initErrCodeAndMsg("7", "任务记录入库失败");
                            }
                        }
                    } else {
                        log.error("手动运行p3c任务时codeScanJob更新失败");
                        return ResultVo.Builder.FAIL().initErrCodeAndMsg("8", "手动运行p3c任务时codeScanJob更新失败");
                    }
                } else {
                    log.error("手动运行p3c任务时git地址获取异常");
                    return ResultVo.Builder.FAIL().initErrCodeAndMsg("3", "手动运行p3c任务时git地址获取异常");
                }
            } catch (Exception e) {
                log.error("手动运行p3c任务异常失败");
                return ResultVo.Builder.FAIL().initErrCodeAndMsg("2", "手动运行p3c任务异常失败");
            }
        } else {
            log.error("重新运行p3c扫描时没有获取到任务ID");
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("4", "重新运行p3c扫描时没有获取到任务ID");
        }
    }

    private ResultVo<String> retrySendPmdScan(CodeScanResult codeScanResult, Map<String, Object> parameters) {
        try {
            Callable pmdCallable = new Callable() {
                @Override
                public ResultVo<String> call() throws Exception {
                    Optional<String> pmdScanResult = codeService.codeScanRequest(pmdScanRemote, parameters);
                    log.info("重新运行时pmd扫描任务发送成功");
                    if (pmdScanResult.isPresent()) {
                        log.info("重新运行时pmd扫描任务已经成功返回结果,{} ", pmdScanResult);
                        return updatePmdScanResult(codeScanResult, pmdScanResult);
                    } else {
                        log.error("重新运行时pmd扫描任务创建请求失败");
                        return ResultVo.Builder.FAIL().initErrCodeAndMsg("8", "重新运行时pmd扫描任务创建请求失败");
                    }
                }
            };
            EXECUTE_SHELL.submit(pmdCallable);
        } catch (Exception e) {
            log.error("重新运行时pmd扫描任务创建请求失败");
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("7", "重新运行时pmd扫描任务创建请求失败");
        }
        return ResultVo.Builder.SUCC().initSuccDataAndMsg("0", "pmd扫描任务发送成功");
    }

    private ResultVo<String> retrySendSonarScan(Map<String, Object> parameters) {
        try {
            Optional<String> scanResult = codeService.codeScanRequest(codeScanCallbackRemote, parameters);
            if (scanResult.isPresent()) {
                log.info("重新运行时代码扫描任务创建成功，已经成功调用对方接口,{} ", scanResult);
                return ResultVo.Builder.SUCC().initSuccData("重新运行时代码扫描任务创建成功，已经成功调用对方接口");
            } else {
                log.error("重新运行时代码扫描任务创建请求失败");
                return ResultVo.Builder.FAIL().initErrCodeAndMsg("9", "重新运行时代码扫描任务创建请求失败");
            }
        } catch (Exception e) {
            log.error("重新运行时代码扫描任务创建请求失败");
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("9", "重新运行时代码扫描任务创建请求失败");
        }
    }

    private ResultVo<String> sendPmdScan(CodeScanResult codeScanResult, Integer jobId, Map<String, Object> parameters) {
        try {
            Callable pmdCallable = new Callable() {
                @Override
                public ResultVo<String> call() throws Exception {
                    Optional<String> pmdScanResult = codeService.codeScanRequest(pmdScanRemote, parameters);
                    log.info("pmd扫描任务发送成功");
                    if (pmdScanResult.isPresent()) {
                        log.info("pmd扫描任务已经成功返回结果,{} ", pmdScanResult);
                        codeScanResult.setId(jobId);
                        return updatePmdScanResult(codeScanResult, pmdScanResult);
                    } else {
                        log.error("pmd扫描任务创建请求失败");
                        return ResultVo.Builder.FAIL().initErrCodeAndMsg("8", "pmd扫描任务创建请求失败");
                    }
                }
            };
            EXECUTE_SHELL.submit(pmdCallable);
        } catch (Exception e) {
            log.error("pmd扫描任务创建请求失败");
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("7", "pmd扫描任务创建请求失败");
        }
        return ResultVo.Builder.SUCC().initSuccDataAndMsg("0", "pmd扫描任务发送成功");
    }

    private ResultVo<String> sendSonarScan(Map<String, Object> parameters) {
        try {
            Optional<String> scanResult = codeService.codeScanRequest(codeScanCallbackRemote, parameters);
            if (scanResult.isPresent()) {
                log.info("代码扫描任务创建成功，已经成功调用对方接口,{} ", scanResult);
                return ResultVo.Builder.SUCC().initSuccData("代码扫描任务创建成功，已经成功调用对方接口");
            } else {
                log.error("代码扫描任务创建请求失败");
                return ResultVo.Builder.FAIL().initErrCodeAndMsg("9", "代码扫描任务创建请求失败");
            }
        } catch (Exception e) {
            log.error("代码扫描任务创建请求失败");
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("9", "代码扫描任务创建请求失败");
        }
    }


    private ResultVo<String> updatePmdScanResult(CodeScanResult codeScanResult, Optional<String> pmdScanResult) {
        log.info("projectPmdScanTask is {}, pmdScanResult is {} ", codeScanResult, pmdScanResult);
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
                codeScanResult.setBlocker(blocker);
                codeScanResult.setCritical(critical);
                codeScanResult.setMajor(major);
                codeScanResult.setMinor(minor);
                codeScanResult.setInfo(info);
                codeScanResult.setStatus(0);
                String pmdUrl = pmdResult.get("url").toString();
                log.info("pmd url is {}", pmdUrl);
                codeScanResult.setCodeScanResultUrl(pmdUrl);
                Integer updatePmd = projectPmdScanTaskService.updateProjectPmdScanTask(codeScanResult);
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
            codeScanResult.setStatus(1);
            Integer updatePmd = projectPmdScanTaskService.updateProjectPmdScanTaskStatus(codeScanResult);
            if (updatePmd > 0) {
                log.info("PMD扫描结果状态更新成功");
            } else {
                log.error("PMD扫描结果状态更新失败");
            }
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("14", "PMD扫描失败，fail掉了");
        } else if ("fail".equals(pmdResult.get("ret")) && "3001".equals(pmdResult.get("code"))) {
            log.error("非Java代码，无法进行PMD扫描");
            codeScanResult.setStatus(4);
            Integer updatePmd = projectPmdScanTaskService.updateProjectPmdScanTaskStatus(codeScanResult);
            if (updatePmd > 0) {
                log.info("PMD扫描结果状态更新成功");
            } else {
                log.error("PMD扫描结果状态更新失败");
            }
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("15", "非Java代码，无法进行PMD扫描");
        } else {
            log.error("PMD扫描返回值异常");
            codeScanResult.setStatus(1);
            Integer updatePmd = projectPmdScanTaskService.updateProjectPmdScanTaskStatus(codeScanResult);
            if (updatePmd > 0) {
                log.info("PMD扫描结果状态更新成功");
            } else {
                log.error("PMD扫描结果状态更新失败");
            }
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("16", "PMD扫描返回值异常");
        }
    }

    @Override
    public List<CodeScanResult> getCodeScanTask(CodeScanResult codeScanResult) {
        log.info("ProjectCodeScanTaskServiceImpl中codeScanResult的入参是{}", codeScanResult);
        return projectCodeScanTaskDao.getCodeScanTask(codeScanResult);
    }

    @Override
    public List<CodeScanJob> getCodeScanJob(CodeScanJob codeScanJob) {
        log.info("ProjectCodeScanTaskServiceImpl中codeScanJob的入参是{}", codeScanJob);
        return projectCodeScanTaskDao.getCodeScanJob(codeScanJob);
    }

    @Override
    public Integer pageTotal(CodeScanResult codeScanResult) {
        return projectCodeScanTaskDao.pageTotal(codeScanResult);
    }

    @Override
    public Integer p3cTotal(CodeScanJob codeScanJob) {
        return projectCodeScanTaskDao.p3cTotal(codeScanJob);
    }

    @Override
    public String getHttpIp(HttpServletRequest httpServletRequest) {
        String ip = httpServletRequest.getHeader("X-Forwarded-For");
        if (!StringUtils.isEmpty(ip) && !"unKnown".equalsIgnoreCase(ip)) {
            //多次反向代理后会有多个ip值，第一个ip才是真实ip
            int index = ip.indexOf(",");
            if (index != -1) {
                ip = ip.substring(0, index);
            }
        } else {
            ip = httpServletRequest.getHeader("X-Real-IP");
            if (StringUtils.isEmpty(ip) || "unKnown".equalsIgnoreCase(ip)) {
                ip = httpServletRequest.getRemoteAddr();
            }
        }
        return ip;
    }

    @Override
    public String getUser(String accessToken, String userAgent, String ip) {
        Map<String, Object> map = new HashMap<>();
        map.put("accessToken", accessToken);
        map.put("userAgent", userAgent);
        map.put("ip", ip);
        log.info(map.toString());
        Optional<String> optional = HTTP_POOL_CLIENT.postJson(dealAccesstockenUrl, JSONObject.toJSONString(map));
        String response = optional.isPresent() ? optional.get() : "";
        log.info(response);
        ResultVo resultVo = (ResultVo) JSON.parseObject(response, ResultVo.class);
        OauthUser oauthUser = new OauthUser();
        if (resultVo.getCode().equals("0") && resultVo.getRet().equals("success")) {
            oauthUser = (OauthUser) JSON.parseObject(String.valueOf(resultVo.getData()), OauthUser.class);
            if (oauthUser == null || (oauthUser.getUseable() != null && oauthUser.getUseable() != 1)) {
                log.info("获取用户信息已失效，accessToken = {} ,userAgent = {}, ip = {}", accessToken, userAgent, ip);
                return null;
            }
            return oauthUser.getAccount();
        } else {
            log.error("获取用户信息失败");
            return null;
        }
    }

    @Override
    public Integer updateProjectCodeScanTask(CodeScanResult codeScanResult) {
        return projectCodeScanTaskDao.updateProjectCodeScanTask(codeScanResult);
    }

    @Override
    public Integer getIdByAppCode(CodeScanResult codeScanResult) {
        return projectCodeScanTaskDao.getIdByAppCode(codeScanResult);
    }

    @Override
    public Integer getJobIdByAppCode(CodeScanJob codeScanJob) {
        return projectCodeScanTaskDao.getJobIdByAppCode(codeScanJob);
    }

    @Override
    public Integer insertP3cRecord(CodeScanJobRecord codeScanJobRecord) {
        return projectCodeScanTaskDao.insertCodeScanJobRecord(codeScanJobRecord);
    }

    @Override
    public Integer getRecordIdByJobId(CodeScanJobRecord codeScanJobRecord) {
        return projectCodeScanTaskDao.getRecordIdByJobId(codeScanJobRecord);
    }

    @Override
    public Integer updateCodeScanJob(CodeScanJob codeScanJob) {
        return projectCodeScanTaskDao.updateCodeScanJob(codeScanJob);
    }

    @Override
    public Integer saveCodeScanJob(CodeScanJob codeScanJob) {
        return projectCodeScanTaskDao.saveCodeScanJob(codeScanJob);
    }

    @Override
    public Integer updateCodeScanJobRecord(CodeScanJobRecord codeScanJobRecord) {
        return projectCodeScanTaskDao.updateCodeScanJobRecord(codeScanJobRecord);
    }

    @Override
    public CodeScanJob getCodeScanJobByJobId(CodeScanJob codeScanJob) {
        return projectCodeScanTaskDao.getCodeScanJobByJobId(codeScanJob);
    }

    @Override
    public ResultVo<String> deleteP3cCodeScanJob(CodeScanJob codeScanJob) {
        log.info("codeScanJob = {}", codeScanJob);
        if (codeScanJob == null) {
            log.error("参数codeScanJob为空");
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("1", "参数codeScanJob为空");
        }
        try {
            Integer jobId = codeScanJob.getId();
            if (jobId != null && jobId > 0) {
                log.info("删除p3c任务的任务ID是{}", jobId);
                codeScanJob.setDeleted(1);
                Integer deleteJob = projectCodeScanTaskService.updateP3cDeletedStatus(codeScanJob);
                if (deleteJob > 0) {
                    log.info("数据库中deleted置为1成功");
                    Boolean removeJob = jobManagerService.removeJob(codeScanJob);
                    if (removeJob) {
                        log.info("移除p3c定时任务成功");
                        return ResultVo.Builder.SUCC().initSuccDataAndMsg("0", "移除p3c定时任务成功");
                    } else {
                        log.error("移除p3c定时任务失败");
                        return ResultVo.Builder.FAIL().initErrCodeAndMsg("5", "移除p3c定时任务失败");
                    }
                } else {
                    log.error("数据库中deleted置为1失败");
                    return ResultVo.Builder.FAIL().initErrCodeAndMsg("4", "数据库中deleted置为1失败");
                }
            } else {
                log.error("删除p3c任务时没有获取到任务ID");
                return ResultVo.Builder.FAIL().initErrCodeAndMsg("3", "删除p3c任务时没有获取到任务ID");
            }
        } catch (Exception e) {
            log.error("删除p3c任务时异常失败");
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("2", "删除p3c任务时异常失败");
        }
    }

    @Override
    public Integer updateP3cDeletedStatus(CodeScanJob codeScanJob) {
        return projectCodeScanTaskDao.updateP3cDeletedStatus(codeScanJob);
    }
}
