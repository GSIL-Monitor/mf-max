package com.mryx.matrix.codeanalyzer.core.task;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import com.mryx.common.utils.HttpClientUtil;
import com.mryx.common.utils.HttpPoolClient;
import com.mryx.matrix.codeanalyzer.core.service.ProjectPmdScanTaskService;
import com.mryx.matrix.codeanalyzer.domain.CodeScanResult;
import com.mryx.matrix.common.domain.ResultVo;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Optional;

public class PmdScanJob implements Job {
    private static Logger logger = LoggerFactory.getLogger(PmdScanJob.class);
    private static final HttpPoolClient HTTP_POOL_CLIENT = HttpClientUtil.create(150000, 150000, 5, 5, 0, 150000);

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        try {
            JobDataMap dataMap = jobExecutionContext.getJobDetail().getJobDataMap();

            String remote = dataMap.getString("remote");
            String gitAddress = dataMap.getString("gitAddress");
            String codeBranch = dataMap.getString("codeBranch");
            String jobId = dataMap.getString("jobId");
            ProjectPmdScanTaskService projectPmdScanTaskService = (ProjectPmdScanTaskService) dataMap.get("projectPmdScanTaskService");
            Map<String, Object> parameters = getParameters(gitAddress, codeBranch, jobId);
            logger.info("本次pmd扫描请求参数是{},{}", remote, parameters);
            Optional<String> pmdScanResult = HTTP_POOL_CLIENT.postJson(remote, JSONObject.toJSONString(parameters));
            logger.info("pmd扫描任务发送成功");
            if (pmdScanResult.isPresent()) {
                logger.info("pmd扫描任务已经成功返回结果,{} ", pmdScanResult);
                CodeScanResult codeScanResult = new CodeScanResult();
                codeScanResult.setId(Integer.valueOf(jobId));
                updatePmdScanResult(codeScanResult, pmdScanResult, projectPmdScanTaskService);
            } else {
                logger.error("pmd扫描任务创建请求失败");
            }
        } catch (Exception e) {
            logger.error("pmd代码扫描任务创建请求失败");
        }

    }

    private Map<String, Object> getParameters(String gitAddress, String codeBranch, String jobId) {
        Map<String, Object> parameters = Maps.newHashMap();
        parameters.put("git", gitAddress);
        parameters.put("branch", codeBranch);
        parameters.put("id", jobId);
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmm");
        String date = df.format(new Date());
        parameters.put("time", date);
        return parameters;
    }

    private ResultVo<String> updatePmdScanResult(CodeScanResult codeScanResult, Optional<String> pmdScanResult, ProjectPmdScanTaskService projectPmdScanTaskService) {
        logger.info("projectPmdScanTask is {}, pmdScanResult is {} ", codeScanResult, pmdScanResult);
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
                logger.info("blocker is {},critical is {},major is {},minor is {},info is {}", blocker, critical, major, minor, info);
                codeScanResult.setBlocker(blocker);
                codeScanResult.setCritical(critical);
                codeScanResult.setMajor(major);
                codeScanResult.setMinor(minor);
                codeScanResult.setInfo(info);
                codeScanResult.setStatus(0);
                String pmdUrl = pmdResult.get("url").toString();
                logger.info("pmd url is {}", pmdUrl);
                codeScanResult.setCodeScanResultUrl(pmdUrl);
                Integer updatePmd = projectPmdScanTaskService.updateProjectPmdScanTask(codeScanResult);
                if (updatePmd > 0) {
                    logger.info("更新PMD扫描结果成功");
                    return ResultVo.Builder.SUCC().initSuccDataAndMsg("12", "更新PMD扫描结果成功");
                } else {
                    logger.error("更新PMD扫描结果失败");
                    return ResultVo.Builder.FAIL().initErrCodeAndMsg("13", "更新PMD扫描结果失败");
                }
            }
        } else if ("fail".equals(pmdResult.get("ret")) && "1".equals(pmdResult.get("code"))) {
            logger.error("PMD扫描失败，fail掉了");
            codeScanResult.setStatus(1);
            Integer updatePmd = projectPmdScanTaskService.updateProjectPmdScanTaskStatus(codeScanResult);
            if (updatePmd > 0) {
                logger.info("PMD扫描结果状态更新成功");
            } else {
                logger.error("PMD扫描结果状态更新失败");
            }
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("14", "PMD扫描失败，fail掉了");
        } else if ("fail".equals(pmdResult.get("ret")) && "3001".equals(pmdResult.get("code"))) {
            logger.error("非Java代码，无法进行PMD扫描");
            codeScanResult.setStatus(4);
            Integer updatePmd = projectPmdScanTaskService.updateProjectPmdScanTaskStatus(codeScanResult);
            if (updatePmd > 0) {
                logger.info("PMD扫描结果状态更新成功");
            } else {
                logger.error("PMD扫描结果状态更新失败");
            }
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("15", "非Java代码，无法进行PMD扫描");
        } else {
            logger.error("PMD扫描返回值异常");
            codeScanResult.setStatus(1);
            Integer updatePmd = projectPmdScanTaskService.updateProjectPmdScanTaskStatus(codeScanResult);
            if (updatePmd > 0) {
                logger.info("PMD扫描结果状态更新成功");
            } else {
                logger.error("PMD扫描结果状态更新失败");
            }
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("16", "PMD扫描返回值异常");
        }
    }
}
