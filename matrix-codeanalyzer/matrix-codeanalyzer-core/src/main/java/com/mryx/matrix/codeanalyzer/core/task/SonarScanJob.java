package com.mryx.matrix.codeanalyzer.core.task;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import com.mryx.common.utils.HttpClientUtil;
import com.mryx.common.utils.HttpPoolClient;
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

public class SonarScanJob implements Job {
    private static Logger logger = LoggerFactory.getLogger(SonarScanJob.class);
    private static final HttpPoolClient HTTP_POOL_CLIENT = HttpClientUtil.create(150000, 150000, 5, 5, 0, 150000);

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        try {
            JobDataMap dataMap = jobExecutionContext.getJobDetail().getJobDataMap();

            String remote = dataMap.getString("remote");
            String gitAddress = dataMap.getString("gitAddress");
            String codeBranch = dataMap.getString("codeBranch");
            String jobId = dataMap.getString("jobId");
            Map<String, Object> parameters = getParameters(gitAddress, codeBranch, jobId);
            logger.info("本次sonar扫描请求参数是{},{}", remote, parameters);
            Optional<String> scanResult = HTTP_POOL_CLIENT.postJson(remote, JSONObject.toJSONString(parameters));
            if (scanResult.isPresent()) {
                logger.info("代码扫描任务创建成功，已经成功调用对方接口,{} ", scanResult);
            } else {
                logger.error("代码扫描任务创建请求失败");
            }
        } catch (Exception e) {
            logger.error("代码扫描任务创建请求失败");
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
