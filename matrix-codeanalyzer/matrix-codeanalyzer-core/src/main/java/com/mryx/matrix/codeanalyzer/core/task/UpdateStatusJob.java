package com.mryx.matrix.codeanalyzer.core.task;

import com.mryx.matrix.codeanalyzer.core.service.P3cCodeScanJobService;
import com.mryx.matrix.common.domain.ResultVo;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UpdateStatusJob implements Job {
    private static Logger logger = LoggerFactory.getLogger(UpdateStatusJob.class);

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        JobDataMap dataMap = jobExecutionContext.getJobDetail().getJobDataMap();
        P3cCodeScanJobService p3cCodeScanJobService = (P3cCodeScanJobService) dataMap.get("p3cCodeScanJobService");
        ResultVo<String> updateJobResult = p3cCodeScanJobService.updateCodeScanJobRuningStatus();
        if ("0".equals(updateJobResult.getCode())) {
            logger.info(updateJobResult.getMessage());
        } else {
            logger.error(updateJobResult.getMessage());
        }
        ResultVo<String> updateRecordResult = p3cCodeScanJobService.updateCodeScanJobRecordRuningStatus();
        if ("0".equals(updateRecordResult.getCode())) {
            logger.info(updateRecordResult.getMessage());
        } else {
            logger.error(updateRecordResult.getMessage());
        }
    }
}
