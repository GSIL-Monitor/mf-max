package com.mryx.matrix.publish.core.service.impl;

import com.alibaba.fastjson.JSON;
import com.google.common.base.Strings;
import com.google.common.collect.Collections2;
import com.mryx.matrix.publish.core.service.ProjectTaskBatchRecordService;
import com.mryx.matrix.publish.core.service.PublishService;
import com.mryx.matrix.publish.core.service.ReleaseDelpoyRecordService;
import com.mryx.matrix.publish.core.utils.FileUtil;
import com.mryx.matrix.publish.core.utils.ShellUtil;
import com.mryx.matrix.publish.domain.BetaDeployParam;
import com.mryx.matrix.publish.domain.ProjectTaskBatchRecord;
import com.mryx.matrix.publish.domain.ReleaseDelpoyRecord;
import com.mryx.matrix.publish.domain.ReleaseDeployParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * @author dinglu
 * @date 2018/9/12
 */
@Service("publishService")
public class PublishServiceImpl implements PublishService {

    private static final Logger logger = LoggerFactory.getLogger(PublishServiceImpl.class);


    private static final String DUBBO_ADMIN_URL = "";

    @Resource
    ProjectTaskBatchRecordService projectTaskBatchRecordService;

    @Resource
    ReleaseDelpoyRecordService releaseDelpoyRecordService;

    @Value("${buildcript}")
    private String buildcript;

    @Override
    public String betaPublish(BetaDeployParam betaDeployParam, String outputToLogFilePwd) {
        //String deployShellName = betaDeployParam.getScript();
        //TODO 修改路径
        String deployShellName = buildcript;
        String betaDeployShellParam = "\'" + JSON.toJSONString(betaDeployParam) + "\'";

        try {

            String shellCmd = deployShellName + " " + betaDeployShellParam + " 2>&1 | tee -a " + outputToLogFilePwd;
            logger.info("执行脚本：{},日志路径：{}", shellCmd, outputToLogFilePwd);
            //执行脚本
            ShellUtil.exec(shellCmd);

            return outputToLogFilePwd;

        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public String releasePublish(ReleaseDeployParam releaseDeployParam, String outputToLogFilePwd) {
        String deployShellName = buildcript;
        if (outputToLogFilePwd == null || outputToLogFilePwd.trim().length() <= 0) {
            String deployShellFilePwd = "/data/matrix/logs/release/";
            Date now = new Date();
            //可以方便地修改日期格式
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HHmmss");
            String datetimeOfNow = dateFormat.format(now);
            outputToLogFilePwd = deployShellFilePwd + releaseDeployParam.getRecord() + "-" + datetimeOfNow + "-out.log";
            ReleaseDelpoyRecord releaseDelpoyRecord = new ReleaseDelpoyRecord();
            releaseDelpoyRecord.setId(releaseDeployParam.getRecord());
            releaseDelpoyRecord.setLogPath(outputToLogFilePwd);
            releaseDelpoyRecordService.updateById(releaseDelpoyRecord);
        }
        String releaseDeployShellParam = "\'" + JSON.toJSONString(releaseDeployParam) + "\'";
        try {
            logger.info("==============!!!!!!!!!!!!!!");
            logger.info(releaseDeployParam.toString());
            logger.info("==============!!!!!!!!!!!!!!");
            String shellCmd = deployShellName + " " + releaseDeployShellParam + " 2>&1 | tee -a " + outputToLogFilePwd;
            //执行脚本
            ShellUtil.exec(shellCmd);

            return outputToLogFilePwd;

        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public String log(Integer id, String fileAddr, boolean init) {
        if (init) {
            return FileUtil.initLog(fileAddr);
        }
        return FileUtil.realTimeReadFile(fileAddr);
    }

    @Override
    public boolean removeDubbo(List<String> ips) {
        if (ips == null || ips.isEmpty()) {
            return false;
        }
        for (String ip : ips) {
            if (Strings.isNullOrEmpty(ip)) {
                continue;
            }
            //TODO 摘dubbo
        }
        return false;
    }
}
