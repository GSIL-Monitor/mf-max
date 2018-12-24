package com.mryx.matrix.codeanalyzer.core.dao;

import com.mryx.matrix.codeanalyzer.domain.CodeScanJob;
import com.mryx.matrix.codeanalyzer.domain.CodeScanJobRecord;
import com.mryx.matrix.codeanalyzer.domain.CodeScanResult;

import java.util.List;

public interface ProjectCodeScanReportDao {
    /**
     *
     * @return
     */
    List<CodeScanResult> getDataWeek(CodeScanResult codeScanResult);

    /**
     *
     * @return
     */
    List<CodeScanResult> getDataMonth(CodeScanResult codeScanResult);

    /**
     *
     * @param codeScanResult
     * @return
     */
    List<CodeScanResult> getDataInfo(CodeScanResult codeScanResult);

    /**
     *
     * @param codeScanJobRecord
     * @return
     */
    List<CodeScanJobRecord> getP3cDataWeek(CodeScanJobRecord codeScanJobRecord);
}
