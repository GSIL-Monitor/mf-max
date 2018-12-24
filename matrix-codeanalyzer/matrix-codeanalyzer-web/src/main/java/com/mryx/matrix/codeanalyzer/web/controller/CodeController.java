package com.mryx.matrix.codeanalyzer.web.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.mryx.common.utils.HttpClientUtil;
import com.mryx.common.utils.HttpPoolClient;
import com.mryx.common.utils.SignUtils;
import com.mryx.matrix.codeanalyzer.core.service.*;
import com.mryx.matrix.codeanalyzer.core.task.CodeReviewTask;
import com.mryx.matrix.codeanalyzer.core.task.CodeScanTask;
import com.mryx.matrix.codeanalyzer.domain.CodeScanResult;
import com.mryx.matrix.codeanalyzer.domain.ProjectPmdScanTask;
import com.mryx.matrix.codeanalyzer.dto.CodeScanResultDto;
import com.mryx.matrix.codeanalyzer.enums.RunStatusEnum;
import com.mryx.matrix.codeanalyzer.enums.ScanTypeEnum;
import com.mryx.matrix.codeanalyzer.web.vo.CodeScanResultVo;
import com.mryx.matrix.codeanalyzer.web.vo.Pagination;
import com.mryx.matrix.common.domain.ResultVo;
import com.mryx.matrix.publish.domain.ProjectTask;
import org.apache.curator.shaded.com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author lina02
 * @date 2018/9/26
 */
@RestController
@RequestMapping("/api/code/analyzer/")
public class CodeController {
    private static final Logger log = LoggerFactory.getLogger(CodeController.class);
    private static final HttpPoolClient HTTP_POOL_CLIENT = HttpClientUtil.create(2000, 2000, 5, 5, 1, 500);

    @Autowired
    private CodeService codeService;

    @Autowired
    private CodeReviewResultService codeReviewResultService;

    @Resource
    private ProjectCodeScanTaskService projectCodeScanTaskService;

    @Resource
    private ProjectPmdScanTaskService projectPmdScanTaskService;

    @Value("${code_scan_status_remote}")
    private String codeScanStatusRemote;

    @Value("${code_review_remote}")
    private String codeReviewRemote;

    @Value("${app_projecttasks_remote}")
    private String appProjectTasksRemote;

    @Value("${app_detail_remote}")
    private String appDetailRemote;

    @Value("${code_scan_create_remote}")
    private String codeScanCreateRemote;

    @Value("${code_scan_check_remote}")
    private String codeScanCheckRemote;

    @Value("${code_scan_callback_remote}")
    private String codeScanCallbackRemote;

    @Value("${code_review_create_remote}")
    private String codeReviewCreateRemote;

    @Value("${app_remote}")
    private String appRemote;

    /**
     * 代码扫描
     * TODO 待优化 分布式事务
     *
     * @param projectTasks
     * @return
     */
    @RequestMapping(value = "/scan", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    //@PostMapping(value = "/scan",produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    // public ResultVo<String> codeScan(@RequestBody ProjectTask projectTask) {
    public ResultVo<String> codeScan(@RequestBody List<ProjectTask> projectTasks) {
        log.info("代码检查请求参数: {}", JSON.toJSONString(projectTasks));
        if (projectTasks == null || projectTasks.isEmpty()) {
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("1", "参数错误！");
        }
        long sleepTime = 0;
        for (ProjectTask projectTask : projectTasks) {
            if (projectTask == null || projectTask.getId() == null || projectTask.getId() == 0) {
                continue;
            }
            log.info("project task {}", projectTask);
            //TODO 返回值
            //new Thread(new CodeScanTask(codeService, codeScanCreateRemote, codeScanStatusRemote, appDetailRemote, projectTask)).start();
            new Thread(new CodeScanTask(codeService, codeScanCallbackRemote, appDetailRemote, appProjectTasksRemote, projectTask)).start();
            sleepTime += 50000;
        }
        return ResultVo.Builder.SUCC();
    }

    /**
     * 创建代码评审
     *
     * @param projectTasks
     * @return
     */
    @RequestMapping(value = "/addCodeReview", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    public ResultVo<String> addCodeReview(@RequestBody List<ProjectTask> projectTasks) {
        log.info("addCodeReview: {}", JSON.toJSONString(projectTasks));
        if (projectTasks == null || projectTasks.isEmpty()) {
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("1", "参数错误！");
        }
        for (ProjectTask projectTask : projectTasks) {
            if (projectTask == null || projectTask.getId() == null || projectTask.getId() == 0) {
                continue;
            }
            log.info("ProjectTasks {}", projectTask);
            //TODO 返回值
            new Thread(new CodeReviewTask(codeService, projectTask, codeReviewResultService, appProjectTasksRemote, appDetailRemote, codeReviewCreateRemote)).start();
        }
        return ResultVo.Builder.SUCC();
    }

    /**
     * 获取代码评审状态
     *
     * @param projectTask
     * @return
     */
    @RequestMapping(value = "/getCodeReviewStatus", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    public ResultVo<String> getCodeReviewStatus(@RequestBody ProjectTask projectTask) {
        log.info("getCodeReviewStatus: {}", JSON.toJSONString(projectTask));
        if (projectTask == null) {
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("1", "参数错误！");
        }
        String result = codeReviewResultService.getCodeReviewStatus(codeService, codeReviewResultService, projectTask, appProjectTasksRemote, appDetailRemote, codeReviewRemote);
        return ResultVo.Builder.SUCC().initSuccData(result);
    }

    /**
     * 根据代码评审结果决定是否发布
     * *
     *
     * @return
     * @Param projectTasks
     */
    @RequestMapping(value = "/isPublish", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    public ResultVo<String> isPublish(@RequestBody List<ProjectTask> projectTasks) {
        log.info("isPublish:{}", JSON.toJSONString(projectTasks));
        /**
         * 结果为2，确认发布，结果为其他的（0，1，3），拒绝发布
         */
        if (projectTasks == null || projectTasks.isEmpty()) {
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("1", "参数错误！");
        }
        int count = 0;
        for (ProjectTask projectTask : projectTasks) {
            String status = codeReviewResultService.getCodeReviewStatus(projectTask.getId());
            log.info(status);
            if ("2".equals(status)) {
                count++;
                if (projectTasks.size() == count) {
                    return ResultVo.Builder.SUCC();
                }
            } else {
                return ResultVo.Builder.FAIL().initErrCodeAndMsg("1", "代码评审失败，拒绝发布！");
            }
        }
        return ResultVo.Builder.SUCC();
    }

    /**
     * 通知 更新CodeScanStatus状态
     *
     * @param codeScanResultDto
     * @return
     */
//    @PostMapping(value = "updateCodeScanStatus", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @RequestMapping(value = "/updateCodeScanStatus", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    public ResultVo updateCodeScanStatus(@RequestBody CodeScanResultDto codeScanResultDto) {
        log.info("代码扫描结果回调接口成功");
        return codeService.updateCodeScanStatus(codeScanResultDto);
    }

    @RequestMapping(value = "/compareResult", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    public ResultVo<String> compareResult(@RequestBody ProjectTask projectTask) {
        log.info("成功调用获取比对结果接口");
        Map<String, String> result = codeService.compareResult(projectTask);
        if (result != null) {
            log.info(result.toString());
            if ("true".equals(result.get("flag"))) {
                log.info("获取比对结果成功:{}", result.get("message"));
                return ResultVo.Builder.SUCC().initSuccData(result.get("message"));
            } else {
                return ResultVo.Builder.FAIL().initErrCodeAndMsg("1", result.get("message"));
            }
        } else {
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("1", "获取比对结果失败");
        }
    }

    /**
     * @param requestBodyParams
     * @return
     */
    @RequestMapping(value = "/getAppCode", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    public ResultVo getAppcode(@RequestBody Map<String, String> requestBodyParams) {
        try {
            String appCode = requestBodyParams.get("appCode");
            Map<String, Object> mapInfo = new HashMap();
            mapInfo.put("appCode", appCode);
            mapInfo = SignUtils.addSignParam(mapInfo);
            Optional appList = HTTP_POOL_CLIENT.postJson(appRemote, JSONObject.toJSONString(mapInfo));
            if (appList.isPresent()) {
                JSONObject apps = (JSONObject) JSONObject.parse(appList.get().toString());
                log.info("{}",apps);
                return ResultVo.Builder.SUCC().initSuccData(apps.get("data"));
            }
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("1", "未查询到应用信息");
        } catch (Exception e) {
            log.error("更新异常", e);
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("1", "更新发生异常");
        }
    }

    /**
     * @param codeScanResult
     * @param httpServletRequest
     * @return
     */
    @RequestMapping(value = "/scanTask", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    public ResultVo<String> projectCodeScanTask(@RequestBody CodeScanResult codeScanResult, HttpServletRequest httpServletRequest) {
        log.info("codeScanResult is {}", codeScanResult);
        try {
            if (codeScanResult == null) {
                log.error("参数异常");
                return ResultVo.Builder.FAIL().initErrCodeAndMsg("1", "参数异常");
            } else {
                String userAgent = httpServletRequest.getHeader("user-agent");
                log.info("userAgent is {}", userAgent);
                String ip = projectCodeScanTaskService.getHttpIp(httpServletRequest);
                log.info("IP地址是 {}", ip);
                log.info("accessToken is {}", codeScanResult.getAccessToken());
                String userName = projectCodeScanTaskService.getUser(codeScanResult.getAccessToken(), userAgent, ip);
                log.info("本次任务运行人是 {}", userName);
                codeScanResult.setUserName(userName);
                ResultVo<String> result = projectCodeScanTaskService.createCodeScanTask(codeScanResult);

                log.info("创建单个代码扫描任务返回结果是:{}", result.getRet() + " CODE :" + result.getCode() + " DATA :" + result.getData());
                if ("success".equals(result.getRet())) {
                    log.info("单个代码扫描任务已创建");
                    return ResultVo.Builder.SUCC().initSuccDataAndMsg("0", "单个代码扫描任务创建成功");
                } else if ("fail".equals(result.getRet())) {
                    if ("3".equals(result.getCode())) {
                        return ResultVo.Builder.FAIL().initErrCodeAndMsg("3", "git地址获取异常,无法创建扫描");
                    } else if ("2".equals(result.getCode())) {
                        return ResultVo.Builder.FAIL().initErrCodeAndMsg("2", "代码扫描任务异常失败");
                    } else if ("4".equals(result.getCode())) {
                        return ResultVo.Builder.FAIL().initErrCodeAndMsg("4", "代码分支不存在,无法创建扫描");
                    }
                }
            }
        } catch (Exception e) {
            log.error("单个代码扫描任务异常失败");
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("10", "单个代码扫描任务异常失败");
        }
        return ResultVo.Builder.SUCC().initSuccDataAndMsg("0", "单个代码扫描任务创建成功");
    }

    /**
     * @param codeScanResult
     * @return
     */
    @RequestMapping(value = "/getScanTask", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    public ResultVo<String> getCodeScanTask(@RequestBody CodeScanResult codeScanResult) {
        if (codeScanResult == null) {
            log.info("输入参数codeScanResult为空");
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("1", "输入参数codeScanResult为空");
        }
        log.info("getScanTask的入参是{}", codeScanResult);
        log.info("PageNo is {}", codeScanResult.getPageNo());
        log.info("PageSize is {}", codeScanResult.getPageSize());
        log.info("StartSize is {}", codeScanResult.getStartOfPage());
        try {
            Integer total = projectCodeScanTaskService.pageTotal(codeScanResult);
            log.info("page total is {}", total);
            Pagination<CodeScanResultVo> pagination = new Pagination<>();
            pagination.setPageSize(codeScanResult.getPageSize());
            pagination.setTotalPageForTotalSize(total);
            if (total == 0) {
                pagination.setDataList(new ArrayList<>());
                return ResultVo.Builder.SUCC().initSuccData(pagination);
            }
            List<CodeScanResult> codeScanTaskResults = projectCodeScanTaskService.getCodeScanTask(codeScanResult);
            log.info(codeScanTaskResults.toString());
            List<CodeScanResultVo> codeScanResultVos = codeScanTaskResults.stream().map(vo -> {
                CodeScanResultVo codeScanResultVo = new CodeScanResultVo();
                BeanUtils.copyProperties(vo, codeScanResultVo);
                codeScanResultVo.setStatusDesc(RunStatusEnum.getName(vo.getStatus()));
                codeScanResultVo.setScanTypeDesc((ScanTypeEnum.getName(vo.getTypeOfScan())));
                return codeScanResultVo;
            }).collect(Collectors.toList());
            log.info(codeScanResultVos.toString());
            if (codeScanResultVos == null) {
                return ResultVo.Builder.FAIL().initErrCodeAndMsg("1", "结果为空");
            } else {
                pagination.setDataList(codeScanResultVos);
                return ResultVo.Builder.SUCC().initSuccData(pagination);
            }
        } catch (Exception e) {
            log.error("{}", e);
        }
        return ResultVo.Builder.SUCC();
    }

    @RequestMapping(value = "/updateMaster", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    public ResultVo<String> updateMaster(@RequestBody Map<String, Integer> parameter) {
        if (parameter == null || parameter.isEmpty()) {
            log.error("1", "输入参数parameter为空");
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("1", "输入参数parameter为空");
        }
        try {
            Integer projectTaskId = parameter.get("projectTaskId");
            Integer isMaster = parameter.get("isMaster");
            log.info("projectTaskId is {}", projectTaskId);
            log.info("isMaster is {}", isMaster);
            if (projectTaskId == null || projectTaskId == 0) {
                log.error("projectTaskId为空");
                return ResultVo.Builder.FAIL().initErrCodeAndMsg("2", "projectTaskId为空");
            } else if (isMaster == null) {
                log.error("isMaster为空");
                return ResultVo.Builder.FAIL().initErrCodeAndMsg("2", "isMaster为空");
            } else {
                CodeScanResult codeScanResult = new CodeScanResult();
                Map<String, Integer> para = Maps.newHashMap();
                para.put("projectTaskId", projectTaskId);
                Integer id = 0;
                if (isMaster == 1) {
                    para.put("isMaster", 0);
                    id = codeService.getIdByProjectTaskIdAndIsMaster(para);
                } else if (isMaster == 0) {
                    para.put("isMaster", 1);
                    id = codeService.getIdByProjectTaskIdAndIsMaster(para);
                }
                if (id > 0) {
                    log.info("查询到的主键ID是{}", id);
                    codeScanResult.setId(id);
                    codeScanResult.setIsMaster(isMaster);
                    Integer updateResult = codeService.updateMaster(codeScanResult);
                    if (updateResult > 0) {
                        log.info("发布状态更新成功,{}", updateResult);
                        return ResultVo.Builder.SUCC().initSuccDataAndMsg("0", "发布状态更新成功");
                    } else {
                        log.error("发布状态更新失败");
                        return ResultVo.Builder.SUCC().initSuccDataAndMsg("3", "发布状态更新失败");
                    }
                } else {
                    log.error("查询主键ID失败");
                    return ResultVo.Builder.FAIL().initErrCodeAndMsg("4", "查询主键ID失败");
                }
            }
        } catch (Exception e) {
            log.error("发布状态更新异常失败");
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("5", "发布状态更新异常失败");
        }
    }


    /**
     * PMD扫描
     *
     * @param projectPmdScanTask
     * @return
     */
    @RequestMapping(value = "/pmdScan", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    public ResultVo<String> pmdScanTask(@RequestBody ProjectPmdScanTask projectPmdScanTask, HttpServletRequest httpServletRequest) {
        log.info("ProjectPmdScanTask is {}", projectPmdScanTask);
        try {
            if (projectPmdScanTask == null) {
                log.error("参数异常");
                return ResultVo.Builder.FAIL().initErrCodeAndMsg("1", "参数异常");
            } else {
                String userAgent = httpServletRequest.getHeader("user-agent");
                log.info("userAgent is {}", userAgent);
                String ip = projectCodeScanTaskService.getHttpIp(httpServletRequest);
                log.info("IP地址是 {}", ip);
                log.info("accessToken is {}", projectPmdScanTask.getAccessToken());
                String userName = projectCodeScanTaskService.getUser(projectPmdScanTask.getAccessToken(), userAgent, ip);
                log.info("本次PMDs扫描任务运行人是 {}", userName);
                projectPmdScanTask.setUserName(userName);
                ResultVo<String> result = projectPmdScanTaskService.createPmdScanTask(projectPmdScanTask);
                log.info("创建PMD扫描任务返回结果是:{}", result.getRet() + " CODE :" + result.getCode() + " DATA :" + result.getData());
                if ("success".equals(result.getRet())) {
                    log.info("PMD扫描任务已创建");
                    return ResultVo.Builder.SUCC().initSuccDataAndMsg("0", "PMD扫描任务创建成功");
                } else if ("fail".equals(result.getRet())) {
                    if ("10".equals(result.getCode())) {
                        return ResultVo.Builder.FAIL().initErrCodeAndMsg("10", "git地址获取异常,无法创建PMD扫描");
                    } else if ("7".equals(result.getCode())) {
                        return ResultVo.Builder.FAIL().initErrCodeAndMsg("7", "PMD扫描任务异常失败");
                    } else if ("9".equals(result.getCode())) {
                        return ResultVo.Builder.FAIL().initErrCodeAndMsg("9", "代码分支不存在,无法创建PMD扫描");
                    }
                }
            }
        } catch (Exception e) {
            log.error("PMD扫描任务异常失败");
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("1", "PMD扫描任务异常失败");
        }
        return ResultVo.Builder.SUCC().initSuccDataAndMsg("0", "PMD扫描任务创建成功");
    }

    /**
     * @param projectPmdScanTask
     * @return
     */
    @RequestMapping(value = "/getPmdScanTask", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    public ResultVo<String> getPmdScanTask(@RequestBody ProjectPmdScanTask projectPmdScanTask) {
        if (projectPmdScanTask == null) {
            log.info("输入参数projectPmdScanTask为空");
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("1", "输入参数projectPmdScanTask为空");
        }
        log.info("PageNo is {}", projectPmdScanTask.getPageNo());
        log.info("PageSize is {}", projectPmdScanTask.getPageSize());
        log.info("StartSize is {}", projectPmdScanTask.getStartOfPage());
        try {
            Integer total = projectPmdScanTaskService.pageTotal(projectPmdScanTask);
            log.info("page total is {}", total);
            Pagination<ProjectPmdScanTask> pagination = new Pagination<>();
            pagination.setPageSize(projectPmdScanTask.getPageSize());
            pagination.setTotalPageForTotalSize(total);
            if (total == 0) {
                pagination.setDataList(new ArrayList<>());
                return ResultVo.Builder.SUCC().initSuccData(pagination);
            }
            List<ProjectPmdScanTask> pmdScanTask = projectPmdScanTaskService.getPmdScanTask(projectPmdScanTask);

            log.info(pmdScanTask.toString());
            if (pmdScanTask == null) {
                return ResultVo.Builder.FAIL().initErrCodeAndMsg("1", "结果为空");
            } else {
                pagination.setDataList(pmdScanTask);
                return ResultVo.Builder.SUCC().initSuccData(pagination);
            }
        } catch (Exception e) {
            log.error("{}", e);
        }
        return ResultVo.Builder.SUCC();
    }
}
