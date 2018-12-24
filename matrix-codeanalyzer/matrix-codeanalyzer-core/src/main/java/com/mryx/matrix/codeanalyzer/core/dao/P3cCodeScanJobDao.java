package com.mryx.matrix.codeanalyzer.core.dao;

import com.mryx.matrix.codeanalyzer.domain.CodeScanJob;
import com.mryx.matrix.codeanalyzer.domain.CodeScanJobRecord;

import java.util.List;
import java.util.Map;

/**
 * @author lina02
 * @date 2018/12/6
 */
public interface P3cCodeScanJobDao {
    /**
     * @param codeScanJob
     * @return
     */
    Integer insertCodeScanJob(CodeScanJob codeScanJob);

    /**
     * @param codeScanJob
     * @return
     */
    Integer updateCodeScanJob(CodeScanJob codeScanJob);

    /**
     * @param codeScanJobRecord
     * @return
     */
    Integer updateCodeScanJobRecord(CodeScanJobRecord codeScanJobRecord);

    /**
     * @param codeScanJob
     * @return
     */
    Integer p3cTotal(CodeScanJob codeScanJob);

    /**
     * @param codeScanJob
     * @return
     */
    List<CodeScanJob> getCodeScanJob(CodeScanJob codeScanJob);

    /**
     * @param codeScanJobRecord
     * @return
     */
    Integer insertCodeScanJobRecord(CodeScanJobRecord codeScanJobRecord);

    /**
     * @param codeScanJob
     * @return
     */
    CodeScanJob getCodeScanJobByJobId(CodeScanJob codeScanJob);

    /**
     * @param codeScanJob
     * @return
     */
    Integer saveCodeScanJob(CodeScanJob codeScanJob);

    /**
     * @param codeScanJob
     * @return
     */
    Integer updateP3cDeletedStatus(CodeScanJob codeScanJob);

    /**
     * @param codeScanJobRecord
     * @return
     */
    List<CodeScanJobRecord> getP3cDataWeek(CodeScanJobRecord codeScanJobRecord);

    /**
     * @param codeScanJob
     * @return
     */
    Integer getJobIdByAppCode(CodeScanJob codeScanJob);

    /**
     * @param codeScanJobRecord
     * @return
     */
    Integer getRecordIdByJobId(CodeScanJobRecord codeScanJobRecord);

    /**
     * @return
     */
    List<CodeScanJobRecord> getCodeScanJobRecordRunningStatusJob();

    /**
     * @return
     */
    List<CodeScanJob> getCodeScanJobRunningStatusJob();

    /**
     * @param codeScanJob
     * @return
     */
    Integer updateCodeScanJobRuningStatus(CodeScanJob codeScanJob);

    /**
     * @param codeScanJobRecord
     * @return
     */
    Integer updateCodeScanRecordJobRuningStatus(CodeScanJobRecord codeScanJobRecord);

    /**
     * @param conditions
     * @return
     */
    List<CodeScanJob> getAllCodeScanJob(Map<String, Integer> conditions);

    /**
     * @param codeScanJob
     * @return
     */
    Integer updateAddSuccess(CodeScanJob codeScanJob);

    /**
     * @return
     */
    List<CodeScanJob> getAllAppCodeAndBranch();
}
