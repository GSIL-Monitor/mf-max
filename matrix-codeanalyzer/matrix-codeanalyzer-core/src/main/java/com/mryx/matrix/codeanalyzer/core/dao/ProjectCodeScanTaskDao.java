package com.mryx.matrix.codeanalyzer.core.dao;

import com.mryx.matrix.codeanalyzer.domain.CodeScanJob;
import com.mryx.matrix.codeanalyzer.domain.CodeScanJobRecord;
import com.mryx.matrix.codeanalyzer.domain.CodeScanResult;

import java.util.List;

public interface ProjectCodeScanTaskDao {

    /**
     * @return
     */
    List<CodeScanResult> getCodeScanInfo();

    /**
     * 代码扫描任务入库
     *
     * @param codeScanJob
     * @return
     */
    Integer insertCodeScanJob(CodeScanJob codeScanJob);

    /**
     * @param codeScanResult
     * @return
     */
    Integer insertProjectCodeScanTask(CodeScanResult codeScanResult);

    /**
     * 查询代码扫描任务结果
     *
     * @return
     */
    List<CodeScanResult> getCodeScanTask(CodeScanResult codeScanResult);

    /**
     * @param codeScanJob
     * @return
     */
    List<CodeScanJob> getCodeScanJob(CodeScanJob codeScanJob);

    /**
     * 分页总记录数
     *
     * @param codeScanResult
     * @return
     */
    Integer pageTotal(CodeScanResult codeScanResult);

    /**
     * @param codeScanJob
     * @return
     */
    Integer p3cTotal(CodeScanJob codeScanJob);

    /**
     * 重新运行时更新codeScanResult
     *
     * @param codeScanResult
     * @return
     */
    Integer updateProjectCodeScanTask(CodeScanResult codeScanResult);

    /**
     * @param codeScanResult
     * @return
     */
    Integer getIdByAppCode(CodeScanResult codeScanResult);

    /**
     * @param codeScanJob
     * @return
     */
    Integer getJobIdByAppCode(CodeScanJob codeScanJob);

    /**
     * @param codeScanJobRecord
     * @return
     */
    Integer insertCodeScanJobRecord(CodeScanJobRecord codeScanJobRecord);

    /**
     * @param codeScanJobRecord
     * @return
     */
    Integer getRecordIdByJobId(CodeScanJobRecord codeScanJobRecord);

    /**
     * @param codeScanJob
     * @return
     */
    Integer updateCodeScanJob(CodeScanJob codeScanJob);

    /**
     * @param codeScanJob
     * @return
     */
    Integer saveCodeScanJob(CodeScanJob codeScanJob);

    /**
     * @param codeScanJobRecord
     * @return
     */
    Integer updateCodeScanJobRecord(CodeScanJobRecord codeScanJobRecord);

    /**
     * @param codeScanJob
     * @return
     */
    CodeScanJob getCodeScanJobByJobId(CodeScanJob codeScanJob);

    /**
     * @param codeScanJob
     * @return
     */
    Integer updateP3cDeletedStatus(CodeScanJob codeScanJob);
}
