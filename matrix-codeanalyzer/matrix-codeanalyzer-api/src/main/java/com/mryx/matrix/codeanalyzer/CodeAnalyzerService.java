package com.mryx.matrix.codeanalyzer;

import com.mryx.matrix.codeanalyzer.domain.CodeScanResult;

/**
 * Code Analyzer Service
 *
 * @author supeng
 * @date 2018/09/26
 */
public interface CodeAnalyzerService {
    /**
     * Code Diff
     */
    void codeDiff();

    /**
     * 是否完成CodeDiff
     */
    boolean isCodeDiff();

    /**
     * Code Review
     */
    void codeReview();

    /**
     * 是否完成CodeReview
     */
    boolean isCodeReview();

    /**
     * 代码扫描
     *
     * @return
     */
    CodeScanResult codeScan();

    /**
     * 是否代码扫描通过
     */
    boolean isCodeScan();
}