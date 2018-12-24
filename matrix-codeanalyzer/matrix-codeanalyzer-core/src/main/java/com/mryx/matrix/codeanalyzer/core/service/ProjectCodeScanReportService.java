package com.mryx.matrix.codeanalyzer.core.service;

import com.mryx.matrix.codeanalyzer.domain.CodeScanJob;
import com.mryx.matrix.codeanalyzer.domain.CodeScanJobRecord;
import com.mryx.matrix.codeanalyzer.domain.CodeScanResult;

import java.util.List;

public interface ProjectCodeScanReportService {

    /**
     *
     * @return
     */
    List<CodeScanResult> getDataWeek(CodeScanResult codeScanResult);

    /**
     *
     * @param codeScanJob
     * @return
     */
    List<CodeScanJobRecord> getP3cDataWeek(CodeScanJob codeScanJob);

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
}
