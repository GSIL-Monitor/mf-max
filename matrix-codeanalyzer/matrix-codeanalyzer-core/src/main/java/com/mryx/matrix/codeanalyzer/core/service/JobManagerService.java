package com.mryx.matrix.codeanalyzer.core.service;

import com.mryx.matrix.codeanalyzer.domain.CodeScanJob;
import com.mryx.matrix.codeanalyzer.domain.CodeScanResult;
import org.quartz.*;

public interface JobManagerService {
    /*
     * @param jobName          任务名
     * @param jobGroupName     任务组名
     * @param triggerName      触发器名
     * @param triggerGroupName 触发器组名
     * @param jobClass         任务
     * @param cron             时间设置，参考quartz说明文档
     * @Description: 添加一个定时任务
     */
    public boolean addJob(Class jobClass, String remote, CodeScanJob codeScanJob, String jobId);

    public boolean addJob(Class jobClass, String remote, CodeScanResult codeScanResult, String jobId, ProjectPmdScanTaskService projectPmdScanTaskService);

    public boolean addJob(Class jobClass, String remote, CodeScanJob codeScanJob, String jobId, ProjectCodeScanTaskService projectCodeScanTaskService);

    public boolean addJob(String jobName, String jobGroupName,
                              String triggerName, String triggerGroupName, Class jobClass, String cron,P3cCodeScanJobService p3cCodeScanJobService);

    /**
     * @param jobName
     * @param jobGroupName
     * @param triggerName      触发器名
     * @param triggerGroupName 触发器组名
     * @param cron             时间设置，参考quartz说明文档
     * @Description: 修改一个任务的触发时间
     */
    public boolean modifyJobTime(String jobName,
                                 String jobGroupName, String triggerName, String triggerGroupName, String cron);

    /**
     * @param jobName
     * @param jobGroupName
     * @param triggerName
     * @param triggerGroupName
     * @Description: 移除一个任务
     */
    public boolean removeJob(String jobName, String jobGroupName,
                             String triggerName, String triggerGroupName);

    public boolean removeJob(CodeScanJob codeScanJob);

    /**
     * @Description:启动所有定时任务
     */
    public boolean startJobs();

    /**
     * @Description:关闭所有定时任务
     */
    public boolean shutdownJobs();
}
