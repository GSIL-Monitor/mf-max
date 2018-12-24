package com.mryx.matrix.codeanalyzer.core.utils;

import com.google.common.collect.Maps;
import com.mryx.matrix.codeanalyzer.core.service.JobManagerService;
import com.mryx.matrix.codeanalyzer.core.service.P3cCodeScanJobService;
import com.mryx.matrix.codeanalyzer.core.service.ProjectCodeScanTaskService;
import com.mryx.matrix.codeanalyzer.core.task.P3cScanJob;
import com.mryx.matrix.codeanalyzer.domain.CodeScanJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class RestartAddJobUtil {
    private static Integer addCount = 0;
    private static Integer updateCount = 0;
    @Value("${pmd_scan_remote}")
    private String pmdScanRemote;

    @Resource
    private JobManagerService jobManagerService;

    @Resource
    private P3cCodeScanJobService p3cCodeScanJobService;

    @Resource
    private ProjectCodeScanTaskService projectCodeScanTaskService;

    @PostConstruct
    public void restartAddJob() {
        Map<String, Integer> conditions = Maps.newHashMap();
        conditions.put("deleted", 0);
        List<CodeScanJob> codeScanJobs = p3cCodeScanJobService.getAllCodeScanJob(conditions);
        if (codeScanJobs.isEmpty()) {
            log.error("重启时没有可以添加的定时任务");
        } else {
            for (CodeScanJob codeScanJob : codeScanJobs) {
                Boolean addJob = jobManagerService.addJob(P3cScanJob.class, pmdScanRemote, codeScanJob, codeScanJob.getId().toString(), projectCodeScanTaskService);
                if (addJob) {
                    log.info("重启时已有的P3c定时任务添加成功");
                } else {
                    log.error("重启时已有的P3c定时任务添加失败   {}", ++addCount);
//                    codeScanJob.setAddSuccess(1);
//                    Integer updateAddSuccess = p3cCodeScanJobService.updateAddSuccess(codeScanJob);
//                    if (updateAddSuccess > 0) {
//                        log.info("重启时已有的P3c定时任务添加失败时更新addSuccess字段为1成功");
//                    } else {
//                        log.error("重启时已有的P3c定时任务添加失败时更新addSuccess字段为1失败     {}",++updateCount);
//                    }
                }
            }
        }
    }
}
