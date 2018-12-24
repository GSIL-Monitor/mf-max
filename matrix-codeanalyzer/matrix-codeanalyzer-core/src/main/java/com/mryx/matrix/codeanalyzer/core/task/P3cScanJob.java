package com.mryx.matrix.codeanalyzer.core.task;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import com.mryx.common.utils.HttpClientUtil;
import com.mryx.common.utils.HttpPoolClient;
import com.mryx.matrix.codeanalyzer.core.service.ProjectCodeScanTaskService;
import com.mryx.matrix.codeanalyzer.domain.CodeScanJob;
import com.mryx.matrix.codeanalyzer.domain.CodeScanJobRecord;
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

public class P3cScanJob implements Job {
    private static Logger logger = LoggerFactory.getLogger(P3cScanJob.class);
    private static final HttpPoolClient HTTP_POOL_CLIENT = HttpClientUtil.create(150000, 150000, 5, 5, 0, 150000);

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        try {
            JobDataMap dataMap = jobExecutionContext.getJobDetail().getJobDataMap();

            String remote = dataMap.getString("remote");
            String jobId = dataMap.getString("jobId");
            ProjectCodeScanTaskService projectCodeScanTaskService = (ProjectCodeScanTaskService) dataMap.get("projectCodeScanTaskService");
            CodeScanJob codeScanJob = (CodeScanJob) dataMap.get("codeScanJob");
            String gitAddress = codeScanJob.getGitAddress();
            String codeBranch = codeScanJob.getCodeBranch();
            String runUserName = codeScanJob.getRunUserName();
            CodeScanJobRecord codeScanJobRecord = new CodeScanJobRecord();
            codeScanJobRecord.setJobId(Integer.valueOf(jobId));
            codeScanJobRecord.setRunUserName(runUserName);
            codeScanJobRecord.setManualOrAutomatic(1);
            codeScanJobRecord.setCodeScanTime(new Date());
            codeScanJobRecord.setTypeOfScan(2);
            codeScanJobRecord.setJobStatus(5);
            synchronized (P3cScanJob.class) {
                Integer insertRecord = projectCodeScanTaskService.insertP3cRecord(codeScanJobRecord);
                if (insertRecord > 0) {
                    logger.info("jobID is {}", jobId);
                    Integer recordId = projectCodeScanTaskService.getRecordIdByJobId(codeScanJobRecord);
                    if (recordId > 0) {
                        logger.info("recordId is {}", recordId);
                        codeScanJob.setId(Integer.valueOf(jobId));
                        String sendId = jobId + "a" + recordId;
                        Map<String, Object> parameters = getParameters(gitAddress, codeBranch, sendId);
                        logger.info("本次p3c扫描请求参数是{},{}", remote, parameters);
                        Optional<String> scanResult = HTTP_POOL_CLIENT.postJson(remote, JSONObject.toJSONString(parameters));
                        if (scanResult.isPresent()) {
                            logger.info("p3c扫描任务创建成功，已经成功调用对方接口,{} ", scanResult);
                        } else {
                            logger.error("p3c扫描任务创建请求失败");
                        }
                    } else {
                        logger.error("获取任务记录ID失败");
                    }
                } else {
                    logger.error("任务记录入库失败");
                }
            }
        } catch (Exception e) {
            logger.error("p3c扫描任务创建请求失败{}", e);
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
}
