package com.mryx.matrix.codeanalyzer.core.service.impl;

import com.mryx.matrix.codeanalyzer.core.dao.ProjectCodeScanReportDao;
import com.mryx.matrix.codeanalyzer.core.service.ProjectCodeScanReportService;
import com.mryx.matrix.codeanalyzer.domain.CodeScanJob;
import com.mryx.matrix.codeanalyzer.domain.CodeScanJobRecord;
import com.mryx.matrix.codeanalyzer.domain.CodeScanResult;
import com.mryx.matrix.common.domain.ResultVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@Service("projectCodeScanReportService")
public class ProjectCodeScanReportServiceImpl implements ProjectCodeScanReportService {

    @Resource
    ProjectCodeScanReportDao projectCodeScanReportDao;

    @Override
    public List<CodeScanResult> getDataWeek(CodeScanResult codeScanResult) {
        return projectCodeScanReportDao.getDataWeek(codeScanResult);
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
                List<CodeScanJobRecord> codeScanJobRecords = projectCodeScanReportDao.getP3cDataWeek(codeScanJobRecord);
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
    public List<CodeScanResult> getDataMonth(CodeScanResult codeScanResult) {
        return projectCodeScanReportDao.getDataMonth(codeScanResult);
    }

    @Override
    public List<CodeScanResult> getDataInfo(CodeScanResult codeScanResult) {
        return projectCodeScanReportDao.getDataInfo(codeScanResult);
    }
}
