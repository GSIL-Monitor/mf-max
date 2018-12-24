package com.mryx.matrix.codeanalyzer.core.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mryx.common.utils.HttpClientUtil;
import com.mryx.common.utils.HttpPoolClient;
import com.mryx.common.utils.SignUtils;
import com.mryx.matrix.codeanalyzer.core.dao.P3cCodeScanJobDao;
import com.mryx.matrix.codeanalyzer.core.service.CodeService;
import com.mryx.matrix.codeanalyzer.core.service.JobManagerService;
import com.mryx.matrix.codeanalyzer.core.service.P3cCodeScanJobService;
import com.mryx.matrix.codeanalyzer.core.service.ProjectCodeScanTaskService;
import com.mryx.matrix.codeanalyzer.core.task.P3cScanJob;
import com.mryx.matrix.codeanalyzer.domain.CodeScanJob;
import com.mryx.matrix.codeanalyzer.domain.CodeScanJobRecord;
import com.mryx.matrix.codeanalyzer.dto.*;
import com.mryx.matrix.codeanalyzer.enums.RunStatusEnum;
import com.mryx.matrix.codeanalyzer.enums.ScanModeEnum;
import com.mryx.matrix.codeanalyzer.enums.ScanTypeEnum;
import com.mryx.matrix.codeanalyzer.utils.GitlabUtil;
import com.mryx.matrix.common.domain.ResultVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service("p3cCodeScanJobService")
public class P3cCodeScanJobServiceImpl implements P3cCodeScanJobService {
    private final HttpPoolClient HTTP_POOL_CLIENT = HttpClientUtil.create(2000, 2000, 5, 5, 1, 500);

    @Value("${app_detail_remote}")
    private String appDetailRemote;

    @Value("${pmd_scan_remote}")
    private String pmdScanRemote;

    @Value("${list_all_first_dept}")
    private String listAllFirstDept;

    @Value("${get_dept_app_tree}")
    private String getDeptAppTree;

    @Resource
    private ProjectCodeScanTaskService projectCodeScanTaskService;

    @Resource
    private CodeService codeService;

    @Resource
    private JobManagerService jobManagerService;

    @Resource
    private P3cCodeScanJobService p3cCodeScanJobService;

    @Resource
    private P3cCodeScanJobDao p3cCodeScanJobDao;


    @Override
    public String getUser(CodeScanJob codeScanJob, HttpServletRequest httpServletRequest) {
        String userAgent = httpServletRequest.getHeader("user-agent");
        log.info("userAgent is {}", userAgent);
        String ip = projectCodeScanTaskService.getHttpIp(httpServletRequest);
        log.info("IP地址是 {}", ip);
        log.info("accessToken is {}", codeScanJob.getAccessToken());
        String userName = projectCodeScanTaskService.getUser(codeScanJob.getAccessToken(), userAgent, ip);
        log.info("用户名是 {}", userName);
        return userName;
    }

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
                    ResultVo<String> removeDuplicate = removeDuplicateAppCodeAndBranch(codeScanJob);
                    if ("1".equals(removeDuplicate.getCode())) {
                        return ResultVo.Builder.FAIL().initErrCodeAndMsg("15", "获取数据库中的应用和分支失败，暂时无法添加任务");
                    } else if ("2".equals(removeDuplicate.getCode())) {
                        return ResultVo.Builder.FAIL().initErrCodeAndMsg("16", "该应用的分支已存在定时扫描任务中");
                    } else {
                        log.info("Code is {} Message is {}", removeDuplicate.getCode(), removeDuplicate.getMessage());
                    }
                    synchronized (ProjectCodeScanTaskServiceImpl.class) {
                        Integer insertCode = insertCodeScanJob(codeScanJob);
                        if (insertCode > 0) {
                            log.info("p3c扫描任务入库成功，{}", insertCode.toString());
                            Integer jobId = getJobIdByAppCode(codeScanJob);
                            if (jobId > 0) {
                                log.info("jobId is {}", jobId);
                                Integer typeOfScan = codeScanJob.getTypeOfScan();
                                if (typeOfScan != null) {
                                    return sendP3cScanJob(codeScanJob, jobId, projectCodeScanTaskService);
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
                    return ResultVo.Builder.FAIL().initErrCodeAndMsg("2", "代码分支不存在");
                }
            } else {
                log.error("git地址获取异常");
                return ResultVo.Builder.FAIL().initErrCodeAndMsg("3", "git地址获取异常");
            }
        } catch (Exception e) {
            log.error("p3c代码扫描任务异常失败{}", e);
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("4", "p3c代码扫描任务异常失败");
        }
        return ResultVo.Builder.SUCC();
    }

    private ResultVo<String> removeDuplicateAppCodeAndBranch(CodeScanJob codeScanJob) {
        List<CodeScanJob> codeScanJobs = p3cCodeScanJobDao.getAllAppCodeAndBranch();
        log.info("{}", codeScanJobs);
        if (codeScanJobs.isEmpty()) {
            log.error("获取数据库中的应用和分支失败");
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("1", "获取数据库中的应用和分支失败");
        }
        for (CodeScanJob csj : codeScanJobs) {
            if (csj.getAppCode().equals(codeScanJob.getAppCode()) && csj.getCodeBranch().equals(codeScanJob.getCodeBranch())) {
                log.error("{}应用的分支{}已存在", codeScanJob.getAppCode(), codeScanJob.getCodeBranch());
                return ResultVo.Builder.FAIL().initErrCodeAndMsg("2", "该应用的分支已存在");
            }
        }
        return ResultVo.Builder.SUCC().initSuccDataAndMsg("0", "该应用的分支还没有添加到任务中");
    }

    private Integer getJobIdByAppCode(CodeScanJob codeScanJob) {
        return p3cCodeScanJobDao.getJobIdByAppCode(codeScanJob);
    }

    @Override
    public ResultVo<String> updateScanResult(CodeScanJobRecordDto codeScanJobRecordDto) {
        log.info("P3C扫描回传结果codeScanJobRecordDto = {}", codeScanJobRecordDto);
        if (codeScanJobRecordDto == null || codeScanJobRecordDto.getId() == null || codeScanJobRecordDto.getId().equals(0)) {
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("1", "参数错误！");
        }
        try {
            log.info("p3c尝试更新代码扫描结果");
            String sendId = codeScanJobRecordDto.getId();
            String ret = codeScanJobRecordDto.getRet();
            String code = codeScanJobRecordDto.getCode();
            CodeScanJobRecord data = codeScanJobRecordDto.getData();
            log.info("sendId = {},code = {} ,ret = {},CodeScanJobRecord = {}", sendId, code, ret, data);
            if (sendId != null) {
                CodeScanJob codeScanJob = new CodeScanJob();
                CodeScanJobRecord codeScanJobRecord = new CodeScanJobRecord();
                String[] id = sendId.split("a");
                Integer jobId = Integer.valueOf(id[0]);
                Integer recordId = Integer.valueOf(id[1]);
                codeScanJob.setId(jobId);
                codeScanJobRecord.setId(recordId);
                codeScanJob.setCodeScanTime(new Date());
                codeScanJobRecord.setCodeScanTime(new Date());
                if ("0".equals(code) || "3001".equals(code)) {
                    codeScanJobRecord.setJobStatus(0);
                    codeScanJob.setJobStatus(0);
                } else {
                    codeScanJobRecord.setJobStatus(1);
                    codeScanJob.setJobStatus(1);
                }
                if (data != null) {
                    if (data.getComponentKey() == null) {
                        if (data.getCodeLine() == null) {
                            Integer blocker = data.getBlocker();
                            Integer critical = data.getCritical();
                            Integer major = data.getMajor();
                            Integer minor = data.getMinor();
                            Integer info = data.getInfo();
                            String codeScanResultUrl = data.getCodeScanResultUrl();
                            setP3cScanData(codeScanJob, codeScanJobRecord, blocker, critical, major, minor, info, codeScanResultUrl);
                        } else {
                            String codeLine = data.getCodeLine();
                            String duplicateLine = data.getDuplicateLine();
                            codeScanJob.setCodeLine(codeLine);
                            codeScanJob.setDuplicateLine(duplicateLine);
                            codeScanJobRecord.setCodeLine(codeLine);
                            codeScanJobRecord.setDuplicateLine(duplicateLine);
                        }
                    }
                }
                Integer updateJob = updateCodeScanJob(codeScanJob);
                if (updateJob > 0) {
                    log.info("P3C任务更新成功");
                } else {
                    log.error("P3C任务更新失败");
                    return ResultVo.Builder.FAIL().initErrCodeAndMsg("5", "P3C任务更新失败");
                }
                Integer updateRecord = updateCodeScanJobRecord(codeScanJobRecord);
                if (updateRecord > 0) {
                    log.info("P3C任务结果记录更新成功");
                } else {
                    log.error("P3C任务结果记录更新失败");
                    return ResultVo.Builder.FAIL().initErrCodeAndMsg("6", "P3C任务结果记录更新失败");
                }
            } else {
                log.info("p3c回调结果中sendId有误");
                return ResultVo.Builder.FAIL().initErrCodeAndMsg("3", "p3c回调结果中sendId有误");
            }
        } catch (Exception e) {
            log.error("p3c扫描结果更新失败", e);
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("2", "p3c扫描结果更新失败！");
        }
        return ResultVo.Builder.SUCC().initSuccDataAndMsg("0", "p3c扫描结果更新成功");
    }

    @Override
    public Integer p3cTotal(CodeScanJob codeScanJob) {
        return p3cCodeScanJobDao.p3cTotal(codeScanJob);
    }

    @Override
    public List<CodeScanJob> getCodeScanJob(CodeScanJob codeScanJob) {
        return p3cCodeScanJobDao.getCodeScanJob(codeScanJob);
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
                    codeScanJob.setJobStatus(5);
                    Integer updateResult = updateCodeScanJob(codeScanJob);
                    if (updateResult > 0) {
                        log.info("手动运行p3c扫描任务时codeScanResult更新成功");
                        CodeScanJobRecord codeScanJobRecord = new CodeScanJobRecord();
                        codeScanJobRecord.setJobId(jobId);
                        codeScanJobRecord.setManualOrAutomatic(0);
                        codeScanJobRecord.setCodeScanTime(new Date());
                        codeScanJobRecord.setTypeOfScan(2);
                        codeScanJobRecord.setJobStatus(5);
                        synchronized (ProjectCodeScanTaskServiceImpl.class) {
                            Integer insertRecord = insertP3cRecord(codeScanJobRecord);
                            if (insertRecord > 0) {
                                log.info("jobID is {}", jobId);
                                Integer recordId = getRecordIdByJobId(codeScanJobRecord);
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
                log.error("手动运行p3c任务异常失败  {}", e);
                return ResultVo.Builder.FAIL().initErrCodeAndMsg("2", "手动运行p3c任务异常失败");
            }
        } else {
            log.error("重新运行p3c扫描时没有获取到任务ID");
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("4", "重新运行p3c扫描时没有获取到任务ID");
        }
    }

    private Integer getRecordIdByJobId(CodeScanJobRecord codeScanJobRecord) {
        return p3cCodeScanJobDao.getRecordIdByJobId(codeScanJobRecord);
    }

    @Override
    public CodeScanJob getCodeScanJobByJobId(CodeScanJob codeScanJob) {
        return p3cCodeScanJobDao.getCodeScanJobByJobId(codeScanJob);
    }

    @Override
    public ResultVo<String> saveCodeScanJob(CodeScanJob codeScanJob) {
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
                    log.info("Job中codeScanJob入参值是{}", codeScanJob);
                    Integer update = p3cCodeScanJobDao.saveCodeScanJob(codeScanJob);
                    if (update > 0) {
                        log.info("修改p3c扫描任务成功");
                        return ResultVo.Builder.SUCC().initSuccDataAndMsg("0", "修改p3c扫描任务成功");
                    } else {
                        log.error("修改p3c扫描任务失败");
                        return ResultVo.Builder.FAIL().initErrCodeAndMsg("5", "修改p3c扫描任务失败");
                    }
                } else {
                    log.info("代码分支不存在");
                    return ResultVo.Builder.FAIL().initErrCodeAndMsg("2", "代码分支不存在");
                }
            } else {
                log.error("git地址获取异常");
                return ResultVo.Builder.FAIL().initErrCodeAndMsg("3", "git地址获取异常");
            }
        } catch (Exception e) {
            log.error("p3c代码扫描任务异常失败{}", e);
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("4", "p3c代码扫描任务异常失败");
        }
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
                Integer deleteJob = updateP3cDeletedStatus(codeScanJob);
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
    public List<CodeScanJobRecord> getP3cDataWeek(CodeScanJob codeScanJob) {
        log.info("参数codeScanJob is {}", codeScanJob);
        if (null == codeScanJob || null == codeScanJob.getId() || codeScanJob.getId() <= 0) {
            log.error("参数codeScanJob为空");
            return null;
        } else {
            Integer jobId = codeScanJob.getId();
            if (jobId != null && jobId > 0) {
                CodeScanJobRecord codeScanJobRecord = new CodeScanJobRecord();
                codeScanJobRecord.setJobId(jobId);
                List<CodeScanJobRecord> codeScanJobRecords = p3cCodeScanJobDao.getP3cDataWeek(codeScanJobRecord);
                if (null != codeScanJobRecords) {
                    return codeScanJobRecords;
                } else {
                    log.error("查询最近10次任务记录结果为空");
                    return null;
                }
            } else {
                log.error("查询最近10次任务记录结果的jobId为空");
                return null;
            }
        }
    }

    @Override
    public CodeScanJobDto getP3cData(CodeScanJob codeScanJob) {
        CodeScanJob codeScanJobResult = getCodeScanJobByJobId(codeScanJob);
        List<CodeScanJobRecord> results = getP3cDataWeek(codeScanJob);
        CodeScanJobDto codeScanJobDto = new CodeScanJobDto();
        if (codeScanJobResult != null) {
            if (results != null) {
                List<CodeScanJobRecordDataDto> lists = results.stream().map(para -> {
                    CodeScanJobRecordDataDto vo = new CodeScanJobRecordDataDto();
                    BeanUtils.copyProperties(para, vo);
                    SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    String datetime = sf.format(para.getCodeScanTime());
                    vo.setCodeScanTime(datetime);
                    return vo;
                }).collect(Collectors.toList());
                if ("NA".equals(codeScanJobResult.getDuplicateLine())) {
                    codeScanJobResult.setDuplicateLine("0");
                }
                BeanUtils.copyProperties(codeScanJobResult, codeScanJobDto);
                codeScanJobDto.setJobStatusDesc(RunStatusEnum.getName(codeScanJobResult.getJobStatus()));
                codeScanJobDto.setTypeOfScanDesc(ScanTypeEnum.getName(codeScanJobResult.getTypeOfScan()));
                codeScanJobDto.setTypeOfModeDesc(ScanModeEnum.getName(codeScanJobResult.getModeOfScan()));
                codeScanJobDto.setPolyLine(lists);
                setPieData(codeScanJobResult, codeScanJobDto);
                return codeScanJobDto;
            } else {
                log.error("最近10次扫描数据为空");
                return null;
            }
        } else {
            log.error("任务基本信息为空");
            return null;
        }
    }

    @Override
    public ResultVo<String> saveCodeScanJobInfo(CodeScanJob codeScanJob, String userName) {
        if (codeScanJob.getId() != null && codeScanJob.getId() > 0) {
            codeScanJob.setModifyUserName(userName);
            ResultVo<String> updateJob = saveCodeScanJob(codeScanJob);
            if ("0".equals(updateJob.getCode())) {
                log.info("修改p3c扫描任务成功");
                return ResultVo.Builder.SUCC().initSuccDataAndMsg("0", "修改p3c扫描任务成功");
            } else if ("2".equals(updateJob.getCode()) || "3".equals(updateJob.getCode()) || "4".equals(updateJob.getCode()) || "5".equals(updateJob.getCode())) {
                log.error(updateJob.getMessage());
                return ResultVo.Builder.FAIL().initErrCodeAndMsg(updateJob.getCode(), updateJob.getMessage());
            } else {
                return ResultVo.Builder.FAIL().initErrCodeAndMsg("6", "修改p3c扫描任务失败");
            }
        } else {
            codeScanJob.setUserName(userName);
            codeScanJob.setModifyUserName(userName);
            codeScanJob.setRunUserName(userName);
            codeScanJob.setTimeTrigger("0 0 12 * * ?");
            ResultVo<String> result = createCodeScanJob(codeScanJob);
            if ("0".equals(result.getCode())) {
                return ResultVo.Builder.SUCC().initSuccDataAndMsg("0", "定时任务添加成功");
            } else if ("2".equals(result.getCode()) || "3".equals(result.getCode()) || "15".equals(result.getCode()) || "16".equals(result.getCode())) {
                return ResultVo.Builder.FAIL().initErrCodeAndMsg(result.getCode(), result.getMessage());
            } else {
                return ResultVo.Builder.FAIL().initErrCodeAndMsg("5", "定时任务添加失败");
            }
        }
    }

    @Override
    public ResultVo<String> updateCodeScanJobRuningStatus() {
        List<CodeScanJob> codeScanJobs = p3cCodeScanJobDao.getCodeScanJobRunningStatusJob();
        if (codeScanJobs.isEmpty()) {
            log.info("CodeScanJob中没有正在运行的任务");
            return ResultVo.Builder.SUCC().initSuccDataAndMsg("0", "CodeScanJob中没有正在运行的任务");
        }
        List<CodeScanJob> vo = new ArrayList<>();
        for (CodeScanJob codeScanJob : codeScanJobs) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date());
            long nowTime = calendar.getTimeInMillis();
            Date date = codeScanJob.getUpdateTime();
            calendar.setTime(date);
            long updateTime = calendar.getTimeInMillis();
            if (nowTime - updateTime > 1800000) {
                CodeScanJob dto = new CodeScanJob();
                dto.setId(codeScanJob.getId());
                dto.setJobStatus(6);
                vo.add(dto);
            }
        }
        if (vo.isEmpty()) {
            log.info("CodeScanJob中没有正在运行的任务");
            return ResultVo.Builder.SUCC().initSuccDataAndMsg("0", "CodeScanJob中没有正在运行的任务");
        }
        Integer size = vo.size();
        log.info("{}个CodeScanJob需要更新超时状态", size);
        for (CodeScanJob codeScanJob : vo) {
            Integer updateStatus = p3cCodeScanJobDao.updateCodeScanJobRuningStatus(codeScanJob);
            if (updateStatus > 0) {
                --size;
            } else {
                log.error("运行超时状态更新失败");
            }
        }
        if (size == 0) {
            log.info("CodeScanJob的运行超时状态更新成功");
            return ResultVo.Builder.SUCC().initSuccDataAndMsg("0", "CodeScanJob的运行超时状态更新成功");
        } else {
            log.error("CodeScanJob的运行超时状态更新失败");
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("1", "CodeScanJob的运行超时状态更新失败");
        }
    }

    @Override
    public ResultVo<String> updateCodeScanJobRecordRuningStatus() {
        List<CodeScanJobRecord> codeScanJobRecords = p3cCodeScanJobDao.getCodeScanJobRecordRunningStatusJob();
        if (codeScanJobRecords.isEmpty()) {
            log.error("CodeScanJobRecord中没有正在运行的任务");
            return ResultVo.Builder.SUCC().initSuccDataAndMsg("0", "CodeScanJobRecord中没有正在运行的任务");
        }
        List<CodeScanJobRecord> vo = new ArrayList<>();
        for (CodeScanJobRecord codeScanJobRecord : codeScanJobRecords) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date());
            long nowTime = calendar.getTimeInMillis();
            Date date = codeScanJobRecord.getUpdateTime();
            calendar.setTime(date);
            long updateTime = calendar.getTimeInMillis();
            if (nowTime - updateTime > 1800000) {
                CodeScanJobRecord dto = new CodeScanJobRecord();
                dto.setId(codeScanJobRecord.getId());
                dto.setJobStatus(6);
                vo.add(dto);
            }
        }
        if (vo.isEmpty()) {
            log.info("CodeScanJobRecord中没有正在运行的任务");
            return ResultVo.Builder.SUCC().initSuccDataAndMsg("0", "CodeScanJobRecord中没有正在运行的任务");
        }
        Integer size = vo.size();
        log.info("{}个CodeScanJobRecord需要更新超时状态", size);
        for (CodeScanJobRecord codeScanJobRecord : vo) {
            Integer updateStatus = p3cCodeScanJobDao.updateCodeScanRecordJobRuningStatus(codeScanJobRecord);
            if (updateStatus > 0) {
                --size;
            } else {
                log.error("运行超时状态更新失败");
            }
        }
        if (size == 0) {
            log.info("CodeScanJobRecord的运行超时状态更新成功");
            return ResultVo.Builder.SUCC().initSuccDataAndMsg("0", "CodeScanJobRecord的运行超时状态更新成功");
        } else {
            log.error("CodeScanJobRecord的运行超时状态更新失败");
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("1", "CodeScanJobRecord的运行超时状态更新失败");
        }
    }

    @Override
    public List<CodeScanJob> getAllCodeScanJob(Map<String, Integer> conditions) {
        return p3cCodeScanJobDao.getAllCodeScanJob(conditions);
    }

    @Override
    public Integer updateAddSuccess(CodeScanJob codeScanJob) {
        return p3cCodeScanJobDao.updateAddSuccess(codeScanJob);
    }

    @Override
    public List<AppDto> getAllAppCode() {
        List<AppDto> appCodes = Lists.newArrayList();
        try {
            List<Integer> allFirstDepts = getAllFirstDeptIds();
            if (!allFirstDepts.isEmpty()) {
                for (Integer deptId : allFirstDepts) {
                    log.info("-----------------------------------------");
                    log.info("部门ID is {}", deptId);
                    JSONObject jsonObject = (JSONObject) getDeptAppTree(deptId).getData();
                    if (!jsonObject.isEmpty()) {
                        log.info("开始解析");
                        resolveJSONObject(jsonObject, appCodes);
                    }
                }
            }
        } catch (Exception e) {
            log.error("{}", e);
        }
        if (appCodes.isEmpty()) {
            return null;
        } else {
            return appCodes;
        }
    }

    private ResultVo resolveJSONObject(JSONObject jsonObject, List<AppDto> appCodes) {
        if (jsonObject.get("apps") != null) {
            JSONArray jsonArray = (JSONArray) jsonObject.get("apps");
            for (Object obj : jsonArray) {
                AppDto appDto = JSONObject.parseObject(obj.toString(), AppDto.class);
                log.info("{}", appDto);
                appCodes.add(appDto);
            }
        }
        if (jsonObject.get("children") != null) {
            JSONArray jsonArray = (JSONArray) jsonObject.get("children");
            for (Object obj : jsonArray) {
                resolveJSONObject((JSONObject) obj, appCodes);
            }
        }
        return ResultVo.Builder.SUCC();
    }

    private ResultVo getDeptAppTree(Integer deptId) {
        try {
            Map<String, Object> mapInfo = new HashMap();
            mapInfo.put("deptId", deptId);
            mapInfo = SignUtils.addSignParam(mapInfo);
            Optional getDeptAppTrees = HTTP_POOL_CLIENT.postJson(getDeptAppTree, JSONObject.toJSONString(mapInfo));
            if (getDeptAppTrees.isPresent()) {
                JSONObject apps = (JSONObject) JSONObject.parse(getDeptAppTrees.get().toString());
                return ResultVo.Builder.SUCC().initSuccData(apps.get("data"));
            } else {
                log.error("获取部门应用失败");
                return ResultVo.Builder.FAIL().initErrCodeAndMsg("1", "获取部门应用失败");
            }
        } catch (Exception e) {
            log.error("{}", e);
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("2", "获取部门应用异常");
        }
    }

    private List<Integer> getAllFirstDeptIds() {
        try {
            ResultVo allFirstDepts = getAllFirstDepts();
            List<Integer> deptIds = Lists.newArrayList();
            if (allFirstDepts.getData() != null) {
                JSONArray jsonArray = JSON.parseArray(allFirstDepts.getData().toString());
                for (Object jsonObject : jsonArray) {
                    FirstDeptDto firstDeptDto = JSONObject.parseObject(jsonObject.toString(), FirstDeptDto.class);
                    deptIds.add(firstDeptDto.getDeptId());
                }
            }
            if (!deptIds.isEmpty()) {
                log.info("{}", deptIds.toString());
                return deptIds;
            }
        } catch (Exception e) {
            log.error("{}", e);
        }
        return null;
    }

    private ResultVo getAllFirstDepts() {
        try {
            Map<String, Object> mapInfo = new HashMap();
            mapInfo = SignUtils.addSignParam(mapInfo);
            Optional allFirstDepts = HTTP_POOL_CLIENT.postJson(listAllFirstDept, JSONObject.toJSONString(mapInfo));
            if (allFirstDepts.isPresent()) {
                JSONObject apps = (JSONObject) JSONObject.parse(allFirstDepts.get().toString());
                log.info("{}", apps);
                return ResultVo.Builder.SUCC().initSuccData(apps.getJSONArray("data"));
            } else {
                log.error("获取所有的一级部门失败");
                return ResultVo.Builder.FAIL().initErrCodeAndMsg("1", "获取所有一级部门失败");
            }
        } catch (Exception e) {
            log.error("{}", e);
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("2", "获取所有一级部门发生异常");
        }
    }

    private void setPieData(CodeScanJob codeScanJobResult, CodeScanJobDto codeScanJobDto) {
        PieDto blockers = new PieDto();
        PieDto criticals = new PieDto();
        PieDto majors = new PieDto();
        blockers.setItem("blocker致命缺陷");
        blockers.setCount(codeScanJobResult.getBlocker());
        criticals.setItem("critical严重缺陷");
        criticals.setCount(codeScanJobResult.getCritical());
        majors.setItem("major一般缺陷");
        majors.setCount(codeScanJobResult.getMajor());
        List<PieDto> pie = new ArrayList<>();
        pie.add(blockers);
        pie.add(criticals);
        pie.add(majors);
        codeScanJobDto.setPieData(pie);
    }

    private Integer updateP3cDeletedStatus(CodeScanJob codeScanJob) {
        return p3cCodeScanJobDao.updateP3cDeletedStatus(codeScanJob);
    }

    /**
     * @param codeScanJobRecord
     * @return
     */
    private Integer insertP3cRecord(CodeScanJobRecord codeScanJobRecord) {
        return p3cCodeScanJobDao.insertCodeScanJobRecord(codeScanJobRecord);
    }

    /**
     * @param codeScanJobRecord
     * @return
     */
    private Integer updateCodeScanJobRecord(CodeScanJobRecord codeScanJobRecord) {
        return p3cCodeScanJobDao.updateCodeScanJobRecord(codeScanJobRecord);
    }

    /**
     * @param codeScanJob
     * @return
     */
    private Integer updateCodeScanJob(CodeScanJob codeScanJob) {
        return p3cCodeScanJobDao.updateCodeScanJob(codeScanJob);
    }

    /**
     * @param codeScanJob
     * @param codeScanJobRecord
     * @param blocker
     * @param critical
     * @param major
     * @param minor
     * @param info
     */
    private void setP3cScanData(CodeScanJob codeScanJob, CodeScanJobRecord codeScanJobRecord, Integer blocker, Integer critical, Integer major, Integer minor, Integer info, String codeScanResultUrl) {
        codeScanJobRecord.setBlocker(blocker);
        codeScanJobRecord.setCritical(critical);
        codeScanJobRecord.setMajor(major);
        codeScanJobRecord.setMinor(minor);
        codeScanJobRecord.setInfo(info);
        codeScanJobRecord.setCodeScanResultUrl(codeScanResultUrl);
        codeScanJob.setBlocker(blocker);
        codeScanJob.setCritical(critical);
        codeScanJob.setMajor(major);
        codeScanJob.setMinor(minor);
        codeScanJob.setInfo(info);
        codeScanJob.setCodeScanResultUrl(codeScanResultUrl);
        Integer problem = blocker + critical + major + minor + info;
        Integer total = blocker + critical + major;
        Double blockerRatio = (blocker / total) * 1.0;
        Double criticalRatio = (critical / total) * 1.0;
        Double majorRatio = (major / total) * 1.0;
        Double score = 0.0;
        if (blockerRatio == 0) {
            score += 1.0;
        } else if (0 < blockerRatio && blockerRatio < 0.1) {
            score += 0.5;
        }
        if (0 < criticalRatio && criticalRatio < 0.2) {
            score += 1.0;
        } else if (0.2 < criticalRatio && criticalRatio < 0.3) {
            score += 0.5;
        }
        if (0 < majorRatio && majorRatio < 0.2) {
            score += 1.0;
        } else if (0.2 < majorRatio && majorRatio < 0.3) {
            score += 0.5;
        }
        codeScanJob.setHealth(score);
        codeScanJob.setProblem(problem);
    }

    /**
     * @param codeScanJob
     * @param jobId
     * @param projectCodeScanTaskService
     * @return
     */
    private ResultVo<String> sendP3cScanJob(CodeScanJob codeScanJob, Integer jobId, ProjectCodeScanTaskService projectCodeScanTaskService) {
        try {
            log.info("【P3c定时任务添加............】{}", pmdScanRemote);
            Boolean addJob = jobManagerService.addJob(P3cScanJob.class, pmdScanRemote, codeScanJob, jobId.toString(), projectCodeScanTaskService);
            if (addJob) {
                log.info("【P3c定时任务添加成功】");
                return ResultVo.Builder.SUCC().initSuccDataAndMsg("0", "P3c定时任务添加成功");
            } else {
                log.error("P3c定时任务添加失败");
//                codeScanJob.setAddSuccess(1);
//                Integer updateAddSuccess = p3cCodeScanJobService.updateAddSuccess(codeScanJob);
//                if (updateAddSuccess > 0) {
//                    log.info("重启时已有的P3c定时任务添加失败时更新addSuccess字段为1(添加失败)成功");
//                } else {
//                    log.error("重启时已有的P3c定时任务添加失败时更新addSuccess字段为1(添加失败)失败");
//                }
                return ResultVo.Builder.FAIL().initErrCodeAndMsg("8", "P3c定时任务添加失败");
            }
        } catch (Exception e) {
            log.error("P3c定时任务添加异常");
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("7", "P3c定时任务添加异常");
        }
    }

    /**
     * @param codeScanJob
     * @return
     */
    private Integer insertCodeScanJob(CodeScanJob codeScanJob) {
        return p3cCodeScanJobDao.insertCodeScanJob(codeScanJob);
    }
}
