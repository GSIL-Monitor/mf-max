package com.mryx.matrix.codeanalyzer.core.utils;

import com.mryx.matrix.codeanalyzer.core.service.JobManagerService;
import com.mryx.matrix.codeanalyzer.core.service.P3cCodeScanJobService;
import com.mryx.matrix.codeanalyzer.core.task.UpdateStatusJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

@Slf4j
@Component
public class UpdateTimeoutStatusUtil {
    public final static String JOB_NAME = "updateRunStatusJobName";
    public final static String TRIGGER_NAME = "updateRunStatusTriggerName";
    public final static String JOB_GROUP_NAME = "updateRunStatusJobJobGroupName";
    public final static String TRIGGER_GROUP_NAME = "updateRunStatusJobTriggerGroupName";

    @Resource
    private JobManagerService jobManagerService;

    @Resource
    private P3cCodeScanJobService p3cCodeScanJobService;

    @PostConstruct
    public void udpateRunStatus() {
        jobManagerService.addJob(JOB_NAME, JOB_GROUP_NAME, TRIGGER_NAME, TRIGGER_GROUP_NAME, UpdateStatusJob.class, "0 */30 * * * ?", p3cCodeScanJobService);
    }
}
