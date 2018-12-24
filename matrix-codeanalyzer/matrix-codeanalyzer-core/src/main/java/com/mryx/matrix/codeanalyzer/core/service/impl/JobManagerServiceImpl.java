package com.mryx.matrix.codeanalyzer.core.service.impl;

import com.mryx.common.utils.StringUtils;
import com.mryx.matrix.codeanalyzer.core.service.JobManagerService;
import com.mryx.matrix.codeanalyzer.core.service.P3cCodeScanJobService;
import com.mryx.matrix.codeanalyzer.core.service.ProjectCodeScanTaskService;
import com.mryx.matrix.codeanalyzer.core.service.ProjectPmdScanTaskService;
import com.mryx.matrix.codeanalyzer.domain.CodeScanJob;
import com.mryx.matrix.codeanalyzer.domain.CodeScanResult;
import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerFactory;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service("jobManagerService")
public class JobManagerServiceImpl implements JobManagerService {
    private static Logger logger = LoggerFactory.getLogger(JobManagerServiceImpl.class);
    private static SchedulerFactory schedulerFactory = new StdSchedulerFactory();

    private Integer size=0;

    /*
     * @param jobName          任务名
     * @param jobGroupName     任务组名
     * @param triggerName      触发器名
     * @param triggerGroupName 触发器组名
     * @param jobClass         任务
     * @param cron             时间设置，参考quartz说明文档
     * @Description: 添加一个定时任务
     */
    @Override
    public boolean addJob(Class jobClass, String remote, CodeScanJob codeScanJob, String jobId) {
        try {
            Scheduler sched = schedulerFactory.getScheduler();
            String jobName = codeScanJob.getJobName();
            String jobGroupName = codeScanJob.getJobName();
            String triggerName = codeScanJob.getJobName();
            String triggerGroupName = codeScanJob.getJobName();
            if (StringUtils.isEmpty(jobName) || StringUtils.isEmpty(jobGroupName) || StringUtils.isEmpty(triggerName) || StringUtils.isEmpty(triggerGroupName)) {
                logger.error("任务名、任务组名、触发器名、触发器组名不能为空");
                return false;
            } else {
                // 任务名，任务组，任务执行类
                JobDetail jobDetail = JobBuilder.newJob(jobClass).withIdentity(jobName, jobGroupName).build();

                // 触发器
                TriggerBuilder<Trigger> triggerBuilder = TriggerBuilder.newTrigger();
                // 触发器名,触发器组
                triggerBuilder.withIdentity(triggerName, triggerGroupName);
                triggerBuilder.startNow();
                // 触发器时间设定
                triggerBuilder.withSchedule(CronScheduleBuilder.cronSchedule(codeScanJob.getTimeTrigger()));
                // 创建Trigger对象
                CronTrigger trigger = (CronTrigger) triggerBuilder.build();

                jobDetail.getJobDataMap().put("remote", remote);
                jobDetail.getJobDataMap().put("gitAddress", codeScanJob.getGitAddress());
                jobDetail.getJobDataMap().put("codeBranch", codeScanJob.getCodeBranch());
                jobDetail.getJobDataMap().put("jobId", jobId);
                jobDetail.getJobDataMap().put("codeScanJob", codeScanJob);
                // 调度容器设置JobDetail和Trigger
                sched.scheduleJob(jobDetail, trigger);

                // 启动
                if (!sched.isShutdown()) {
                    sched.start();
                    logger.info("添加定时任务成功");
                    return true;
                }
            }
        } catch (Exception e) {
            logger.error("添加定时任务失败");
            return false;
        }
        return false;
    }

    @Override
    public boolean addJob(Class jobClass, String remote, CodeScanResult codeScanResult, String jobId, ProjectPmdScanTaskService projectPmdScanTaskService) {
        try {
            Scheduler sched = schedulerFactory.getScheduler();
            // 任务名，任务组，任务执行类
            JobDetail jobDetail = JobBuilder.newJob(jobClass).withIdentity(codeScanResult.getTaskName(), codeScanResult.getProjectName()).build();

            // 触发器
            TriggerBuilder<Trigger> triggerBuilder = TriggerBuilder.newTrigger();
            // 触发器名,触发器组
            triggerBuilder.withIdentity(codeScanResult.getTaskName(), codeScanResult.getProjectName());
            triggerBuilder.startNow();
            // 触发器时间设定
            triggerBuilder.withSchedule(CronScheduleBuilder.cronSchedule(codeScanResult.getTimeTrigger()));
            // 创建Trigger对象
            CronTrigger trigger = (CronTrigger) triggerBuilder.build();

            jobDetail.getJobDataMap().put("remote", remote);
            jobDetail.getJobDataMap().put("gitAddress", codeScanResult.getGitAddress());
            jobDetail.getJobDataMap().put("codeBranch", codeScanResult.getCodeBranch());
            jobDetail.getJobDataMap().put("jobId", jobId);
            jobDetail.getJobDataMap().put("codeScanResult", codeScanResult);
            jobDetail.getJobDataMap().put("projectPmdScanTaskService", projectPmdScanTaskService);

            // 调度容器设置JobDetail和Trigger
            sched.scheduleJob(jobDetail, trigger);

            // 启动
            if (!sched.isShutdown()) {
                sched.start();
                logger.info("添加定时任务成功");
                return true;
            }
        } catch (Exception e) {
            logger.error("添加定时任务失败");
            return false;
        }
        return false;
    }

    @Override
    public boolean addJob(Class jobClass, String remote, CodeScanJob codeScanJob, String jobId, ProjectCodeScanTaskService projectCodeScanTaskService) {
        try {
            Scheduler sched = schedulerFactory.getScheduler();
            String jobName = codeScanJob.getAppCode()+size;
            String jobGroupName = codeScanJob.getCodeBranch();
            String triggerName = codeScanJob.getAppCode()+size;
            String triggerGroupName = codeScanJob.getCodeBranch();
            ++size;
            if (StringUtils.isEmpty(jobName) || StringUtils.isEmpty(jobGroupName) || StringUtils.isEmpty(triggerName) || StringUtils.isEmpty(triggerGroupName)) {
                logger.error("任务名、任务组名、触发器名、触发器组名不能为空");
                return false;
            } else {
                // 任务名，任务组，任务执行类
                JobDetail jobDetail = JobBuilder.newJob(jobClass).withIdentity(jobName, jobGroupName).build();

                // 触发器
                TriggerBuilder<Trigger> triggerBuilder = TriggerBuilder.newTrigger();
                // 触发器名,触发器组
                triggerBuilder.withIdentity(triggerName, triggerGroupName);
                triggerBuilder.startNow();
                // 触发器时间设定
                triggerBuilder.withSchedule(CronScheduleBuilder.cronSchedule("0 0 12 * * ?"));
                logger.info("定时任务触发时间是{}", codeScanJob.getTimeTrigger());
                // 创建Trigger对象
                CronTrigger trigger = (CronTrigger) triggerBuilder.build();

                jobDetail.getJobDataMap().put("remote", remote);
                jobDetail.getJobDataMap().put("codeScanJob", codeScanJob);
                jobDetail.getJobDataMap().put("jobId", jobId);
                jobDetail.getJobDataMap().put("projectCodeScanTaskService", projectCodeScanTaskService);
                // 调度容器设置JobDetail和Trigger
                sched.scheduleJob(jobDetail, trigger);

                // 启动
                if (!sched.isShutdown()) {
                    sched.start();
                    logger.info("添加定时任务成功");
                    return true;
                }
            }
        } catch (Exception e) {
            logger.error("添加定时任务失败 {}",e);
            return false;
        }
        return false;
    }

    @Override
    public boolean addJob(String jobName, String jobGroupName, String triggerName, String triggerGroupName, Class jobClass, String cron, P3cCodeScanJobService p3cCodeScanJobService) {
        try {
            Scheduler sched = schedulerFactory.getScheduler();

            if (StringUtils.isEmpty(jobName) || StringUtils.isEmpty(jobGroupName) || StringUtils.isEmpty(triggerName) || StringUtils.isEmpty(triggerGroupName)) {
                logger.error("任务名、任务组名、触发器名、触发器组名不能为空");
                return false;
            } else {
                // 任务名，任务组，任务执行类
                JobDetail jobDetail = JobBuilder.newJob(jobClass).withIdentity(jobName, jobGroupName).build();

                // 触发器
                TriggerBuilder<Trigger> triggerBuilder = TriggerBuilder.newTrigger();
                // 触发器名,触发器组
                triggerBuilder.withIdentity(triggerName, triggerGroupName);
                triggerBuilder.startNow();
                // 触发器时间设定
                triggerBuilder.withSchedule(CronScheduleBuilder.cronSchedule(cron));
                logger.info("定时任务表达式设置正确");
                // 创建Trigger对象
                CronTrigger trigger = (CronTrigger) triggerBuilder.build();

                jobDetail.getJobDataMap().put("p3cCodeScanJobService", p3cCodeScanJobService);
                // 调度容器设置JobDetail和Trigger
                sched.scheduleJob(jobDetail, trigger);

                // 启动
                if (!sched.isShutdown()) {
                    sched.start();
                    logger.info("添加定时任务成功");
                    return true;
                }
            }
        } catch (Exception e) {
            logger.error("添加定时任务失败");
            return false;
        }
        return false;
    }

    /**
     * @param jobName
     * @param jobGroupName
     * @param triggerName      触发器名
     * @param triggerGroupName 触发器组名
     * @param cron             时间设置，参考quartz说明文档
     * @Description: 修改一个任务的触发时间
     */
    @Override
    public boolean modifyJobTime(String jobName,
                                 String jobGroupName, String triggerName, String triggerGroupName, String cron) {
        try {
            Scheduler sched = schedulerFactory.getScheduler();
            TriggerKey triggerKey = TriggerKey.triggerKey(triggerName, triggerGroupName);
            CronTrigger trigger = (CronTrigger) sched.getTrigger(triggerKey);
            if (trigger == null) {
                return false;
            }

            String oldTime = trigger.getCronExpression();
            if (!oldTime.equalsIgnoreCase(cron)) {
                /** 方式一 ：调用 rescheduleJob 开始 */
                // 触发器
                TriggerBuilder<Trigger> triggerBuilder = TriggerBuilder.newTrigger();
                // 触发器名,触发器组
                triggerBuilder.withIdentity(triggerName, triggerGroupName);
                triggerBuilder.startNow();
                // 触发器时间设定
                triggerBuilder.withSchedule(CronScheduleBuilder.cronSchedule(cron));
                // 创建Trigger对象
                trigger = (CronTrigger) triggerBuilder.build();
                // 方式一 ：修改一个任务的触发时间
                sched.rescheduleJob(triggerKey, trigger);
                /** 方式一 ：调用 rescheduleJob 结束 */

                /** 方式二：先删除，然后在创建一个新的Job  */
                //JobDetail jobDetail = sched.getJobDetail(JobKey.jobKey(jobName, jobGroupName));
                //Class<? extends Job> jobClass = jobDetail.getJobClass();
                //removeJob(jobName, jobGroupName, triggerName, triggerGroupName);
                //addJob(jobName, jobGroupName, triggerName, triggerGroupName, jobClass, cron);
                /** 方式二 ：先删除，然后在创建一个新的Job */
                logger.info("修改任务触发时间成功");
                return true;
            }
        } catch (Exception e) {
            logger.error("修改任务触发时间失败");
            return false;
        }
        return false;
    }

    /**
     * @param jobName
     * @param jobGroupName
     * @param triggerName
     * @param triggerGroupName
     * @Description: 移除一个任务
     */
    @Override
    public boolean removeJob(String jobName, String jobGroupName,
                             String triggerName, String triggerGroupName) {
        try {
            Scheduler sched = schedulerFactory.getScheduler();

            TriggerKey triggerKey = TriggerKey.triggerKey(triggerName, triggerGroupName);

            sched.pauseTrigger(triggerKey);// 停止触发器
            sched.unscheduleJob(triggerKey);// 移除触发器
            sched.deleteJob(JobKey.jobKey(jobName, jobGroupName));// 删除任务
            logger.info("移除任务成功");
            return true;
        } catch (Exception e) {
            logger.error("移除任务失败");
        }
        return false;
    }

    @Override
    public boolean removeJob(CodeScanJob codeScanJob) {
        try {
            Scheduler sched = schedulerFactory.getScheduler();
            String triggerName = codeScanJob.getAppCode();
            String triggerGroupName = codeScanJob.getAppCode();
            String jobName = codeScanJob.getAppCode();
            String jobGroupName = codeScanJob.getAppCode();
            if (StringUtils.isEmpty(jobName) || StringUtils.isEmpty(jobGroupName) || StringUtils.isEmpty(triggerName) || StringUtils.isEmpty(triggerGroupName)) {
                logger.info("无法移除任务");
                return false;
            } else {
                TriggerKey triggerKey = TriggerKey.triggerKey(triggerName, triggerGroupName);

                sched.pauseTrigger(triggerKey);// 停止触发器
                sched.unscheduleJob(triggerKey);// 移除触发器
                sched.deleteJob(JobKey.jobKey(jobName, jobGroupName));// 删除任务
                logger.info("移除任务成功");
                return true;
            }
        } catch (Exception e) {
            logger.error("移除任务失败");
        }
        return false;
    }

    /**
     * @Description:启动所有定时任务
     */
    @Override
    public boolean startJobs() {
        try {
            Scheduler sched = schedulerFactory.getScheduler();
            sched.start();
            logger.info("启动所有定时任务成功");
            return true;
        } catch (Exception e) {
            logger.error("启动所有定时任务失败");
        }
        return false;
    }

    /**
     * @Description:关闭所有定时任务
     */
    @Override
    public boolean shutdownJobs() {
        try {
            Scheduler sched = schedulerFactory.getScheduler();
            if (!sched.isShutdown()) {
                sched.shutdown();
                logger.info("关闭所有定时任务成功");
                return true;
            }
        } catch (Exception e) {
            logger.error("关闭所有定时任务失败");
        }
        return false;
    }
}
