package com.mryx.matrix.codeanalyzer.web.controller;

import com.mryx.common.utils.HttpClientUtil;
import com.mryx.common.utils.HttpPoolClient;
import com.mryx.matrix.codeanalyzer.core.service.P3cCodeScanJobService;
import com.mryx.matrix.codeanalyzer.domain.CodeScanJob;
import com.mryx.matrix.codeanalyzer.dto.AppDto;
import com.mryx.matrix.codeanalyzer.dto.CodeScanJobDto;
import com.mryx.matrix.codeanalyzer.dto.CodeScanJobRecordDto;
import com.mryx.matrix.codeanalyzer.enums.RunStatusEnum;
import com.mryx.matrix.codeanalyzer.enums.ScanModeEnum;
import com.mryx.matrix.codeanalyzer.enums.ScanTypeEnum;
import com.mryx.matrix.codeanalyzer.web.vo.CodeScanJobVo;
import com.mryx.matrix.codeanalyzer.web.vo.Pagination;
import com.mryx.matrix.common.domain.ResultVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author lina02
 * @date 2018/12/6
 */
@RestController
@RequestMapping("/api/code/scan/")
public class P3cController {
    private static final Logger log = LoggerFactory.getLogger(CodeController.class);
    private static final HttpPoolClient HTTP_POOL_CLIENT = HttpClientUtil.create(2000, 2000, 5, 5, 1, 500);

    @Resource
    private P3cCodeScanJobService p3cCodeScanJobService;

    /**
     * 添加p3c扫描规范任务
     *
     * @param codeScanJob
     * @param httpServletRequest
     * @return
     */
    @RequestMapping(value = "/addCodeScanJob", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    public ResultVo<String> addCodeScanJob(@RequestBody CodeScanJob codeScanJob, HttpServletRequest httpServletRequest) {
        log.info("codeScanJob is {}", codeScanJob);
        if (codeScanJob == null) {
            log.error("参数异常");
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("1", "参数异常");
        }
        try {
            String userName = p3cCodeScanJobService.getUser(codeScanJob, httpServletRequest);
            if (userName == null) {
                log.error("userName为空");
                return ResultVo.Builder.FAIL().initErrCodeAndMsg("11", "userName为空");
            }
            return p3cCodeScanJobService.saveCodeScanJobInfo(codeScanJob, userName);
        } catch (Exception e) {
            log.error("定时任务添加异常失败 {}", e);
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("4", "定时任务添加异常失败");
        }
    }

    /**
     * @param codeScanJobRecordDto
     * @return
     */
    @RequestMapping(value = "/updateScanResult", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    public ResultVo<String> updateP3cScanResult(@RequestBody CodeScanJobRecordDto codeScanJobRecordDto) {
        log.info("P3C扫描结果回调接口成功");
        return p3cCodeScanJobService.updateScanResult(codeScanJobRecordDto);
    }

    @RequestMapping(value = "/getP3cScanList", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    public ResultVo<String> getP3cScanList(@RequestBody CodeScanJob codeScanJob) {
        if (codeScanJob == null) {
            log.info("输入参数codeScanJob为空");
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("1", "输入参数codeScanJob为空");
        }
        log.info("codeScanJob的入参是{}", codeScanJob);
        log.info("PageNo is {}", codeScanJob.getPageNo());
        log.info("PageSize is {}", codeScanJob.getPageSize());
        log.info("StartSize is {}", codeScanJob.getStartOfPage());
        try {
            Integer p3ctotal = p3cCodeScanJobService.p3cTotal(codeScanJob);
            log.info("page total is {}", p3ctotal);
            Pagination<CodeScanJobVo> pagination = new Pagination<>();
            pagination.setPageSize(codeScanJob.getPageSize());
            pagination.setTotalPageForTotalSize(p3ctotal);
            if (p3ctotal == 0) {
                pagination.setDataList(new ArrayList<>());
                return ResultVo.Builder.SUCC().initSuccData(pagination);
            }
            List<CodeScanJob> codeScanJobs = p3cCodeScanJobService.getCodeScanJob(codeScanJob);
            List<CodeScanJobVo> codeScanJobVos = codeScanJobs.stream().map(vo -> {
                CodeScanJobVo codeScanJobVo = new CodeScanJobVo();
                BeanUtils.copyProperties(vo, codeScanJobVo);
                String duplicateLine = vo.getDuplicateLine();
                if ("NA".equals(duplicateLine)) {
                    codeScanJobVo.setDuplicateLine("0");
                }
                codeScanJobVo.setJobStatusDesc(RunStatusEnum.getName(vo.getJobStatus()));
                codeScanJobVo.setTypeOfScanDesc(ScanTypeEnum.getName(vo.getTypeOfScan()));
                codeScanJobVo.setTypeOfModeDesc(ScanModeEnum.getName(vo.getModeOfScan()));
                return codeScanJobVo;
            }).collect(Collectors.toList());
            log.info(codeScanJobVos.toString());
            if (codeScanJobVos.isEmpty()) {
                return ResultVo.Builder.FAIL().initErrCodeAndMsg("2", "结果为空");
            } else {
                pagination.setDataList(codeScanJobVos);
                return ResultVo.Builder.SUCC().initSuccData(pagination);
            }
        } catch (Exception e) {
            log.error("{获取任务列表失败}", e);
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("3", "获取任务列表失败");
        }
    }

    @RequestMapping(value = "/manualRunP3c", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    public ResultVo<String> manualRunP3c(@RequestBody CodeScanJob codeScanJob, HttpServletRequest httpServletRequest) {
        log.info("codeScanJob is {}", codeScanJob);
        if (codeScanJob == null) {
            log.error("参数异常");
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("1", "参数异常");
        }
        try {
            String userName = p3cCodeScanJobService.getUser(codeScanJob, httpServletRequest);
            ResultVo<String> result = p3cCodeScanJobService.manualRunP3cJob(codeScanJob);
            log.info("手动运行p3c扫描任务返回结果是:{}", result.getRet() + " CODE :" + result.getCode() + " DATA :" + result.getData());
            if ("success".equals(result.getRet())) {
                log.info("手动运行p3c扫描任务已创建");
                return ResultVo.Builder.SUCC().initSuccDataAndMsg("0", "手动运行p3c扫描任务已创建");
            } else if ("fail".equals(result.getRet())) {
                if ("3".equals(result.getCode())) {
                    return ResultVo.Builder.FAIL().initErrCodeAndMsg("3", result.getMessage());
                } else if ("2".equals(result.getCode())) {
                    return ResultVo.Builder.FAIL().initErrCodeAndMsg("2", result.getMessage());
                } else if ("4".equals(result.getCode())) {
                    return ResultVo.Builder.FAIL().initErrCodeAndMsg("4", result.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("手动运行p3c扫描任务异常失败");
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("10", "手动运行p3c扫描任务异常失败");
        }
        return ResultVo.Builder.SUCC().initSuccDataAndMsg("0", "手动运行p3c扫描任务创建成功");
    }

//    @RequestMapping(value = "/runAllJob", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
//    public ResultVo<String> runAllJob(@RequestBody HttpServletRequest httpServletRequest) {
//        try {
//            String userName = p3cCodeScanJobService.getUser(codeScanJob, httpServletRequest);
//            ResultVo<String> result = p3cCodeScanJobService.manualRunP3cJob(codeScanJob);
//            log.info("手动运行p3c扫描任务返回结果是:{}", result.getRet() + " CODE :" + result.getCode() + " DATA :" + result.getData());
//            if ("success".equals(result.getRet())) {
//                log.info("手动运行p3c扫描任务已创建");
//                return ResultVo.Builder.SUCC().initSuccDataAndMsg("0", "手动运行p3c扫描任务已创建");
//            } else if ("fail".equals(result.getRet())) {
//                if ("3".equals(result.getCode())) {
//                    return ResultVo.Builder.FAIL().initErrCodeAndMsg("3", result.getMessage());
//                } else if ("2".equals(result.getCode())) {
//                    return ResultVo.Builder.FAIL().initErrCodeAndMsg("2", result.getMessage());
//                } else if ("4".equals(result.getCode())) {
//                    return ResultVo.Builder.FAIL().initErrCodeAndMsg("4", result.getMessage());
//                }
//            }
//        } catch (Exception e) {
//            log.error("手动运行p3c扫描任务异常失败");
//            return ResultVo.Builder.FAIL().initErrCodeAndMsg("10", "手动运行p3c扫描任务异常失败");
//        }
//        return ResultVo.Builder.SUCC().initSuccDataAndMsg("0", "手动运行p3c扫描任务创建成功");
//    }

    @RequestMapping(value = "/editP3cScanJob", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    public ResultVo<String> editP3cScanJob(@RequestBody CodeScanJob codeScanJob) {
        log.info("codeScanJob is {}", codeScanJob);
        if (codeScanJob == null) {
            log.error("参数异常");
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("1", "参数异常");
        }
        try {
            Integer jobId = codeScanJob.getId();
            if (jobId > 0) {
                log.info("编辑的jobId是 {}", jobId);
                CodeScanJob codeScanJobResult = p3cCodeScanJobService.getCodeScanJobByJobId(codeScanJob);
                if (codeScanJobResult != null) {
                    return ResultVo.Builder.SUCC().initSuccData(codeScanJobResult);
                } else {
                    log.error("依据jobId获取任务时失败");
                    return ResultVo.Builder.FAIL().initErrCodeAndMsg("3", "依据jobId获取任务时失败");
                }
            } else {
                log.error("编辑时获取jobId失败");
                return ResultVo.Builder.FAIL().initErrCodeAndMsg("2", "编辑时获取jobId失败");
            }
        } catch (Exception e) {
            log.error("编辑P3C任务异常失败");
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("10", "编辑P3C任务异常失败");
        }
    }

    @RequestMapping(value = "/deleteP3cScanJob", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    public ResultVo<String> deleteP3cScanJob(@RequestBody CodeScanJob codeScanJob, HttpServletRequest httpServletRequest) {
        log.info("codeScanJob is {}", codeScanJob);
        if (null == codeScanJob) {
            log.error("参数异常");
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("1", "参数异常");
        }
        try {
            String userName = p3cCodeScanJobService.getUser(codeScanJob, httpServletRequest);
            codeScanJob.setDeleteUserName(userName);
            ResultVo<String> deleteJob = p3cCodeScanJobService.deleteP3cCodeScanJob(codeScanJob);
            if ("0".equals(deleteJob.getCode())) {
                log.info("删除p3c扫描任务成功");
                return ResultVo.Builder.SUCC().initSuccDataAndMsg("0", "删除p3c扫描任务成功");
            } else {
                log.error("删除p3c扫描任务失败");
                return ResultVo.Builder.FAIL().initErrCodeAndMsg("2", "删除p3c扫描任务失败");
            }
        } catch (Exception e) {
            log.error("删除p3c扫描任务异常失败");
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("3", "删除p3c扫描任务异常失败");
        }
    }

    /**
     * @param codeScanJob
     * @return
     */
    @RequestMapping(value = "/getP3cDataWeek", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    public ResultVo<String> getDataWeek(@RequestBody CodeScanJob codeScanJob) {
        log.info("codeScanJob is {}", codeScanJob);
        if (null == codeScanJob || null == codeScanJob.getId() || codeScanJob.getId() <= 0) {
            log.error("参数异常");
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("1", "参数异常");
        }
        try {
            CodeScanJobDto codeScanJobDto = p3cCodeScanJobService.getP3cData(codeScanJob);
            if (null == codeScanJobDto) {
                return ResultVo.Builder.FAIL().initErrCodeAndMsg("1", "结果为空");
            } else {
                return ResultVo.Builder.SUCC().initSuccData(codeScanJobDto);
            }
        } catch (Exception e) {
            log.error("获取最近10次扫描数据失败");
        }
        return ResultVo.Builder.FAIL().initErrCodeAndMsg("2", "获取最近10次扫描数据失败");
    }

    @RequestMapping(value = "/adminAddCodeScanJob", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    public ResultVo<String> adminAddCodeScanJob() {
        List<AppDto> appCodes = p3cCodeScanJobService.getAllAppCode();
        if (appCodes.isEmpty()) {
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("1", "返回结果为空");
        } else {
            log.info("{}", appCodes.size());
            for (AppDto app : appCodes) {
                CodeScanJob codeScanJob = new CodeScanJob();
                codeScanJob.setJobName(app.getAppName());
                codeScanJob.setAppCode(app.getAppCode());
                codeScanJob.setCodeBranch("master");
                codeScanJob.setTypeOfScan(2);
                p3cCodeScanJobService.saveCodeScanJobInfo(codeScanJob, "admin");
            }
            return ResultVo.Builder.SUCC().initSuccData(appCodes);
        }
    }

    @RequestMapping(value = "/testUpdate", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    public ResultVo<String> testUpdate(@RequestBody CodeScanJob codeScanJob) {
        return ResultVo.Builder.SUCC();
    }
}
