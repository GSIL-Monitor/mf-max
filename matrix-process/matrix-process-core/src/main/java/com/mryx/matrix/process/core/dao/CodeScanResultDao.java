package com.mryx.matrix.process.core.dao;

import com.mryx.matrix.codeanalyzer.domain.CodeScanResult;

import java.util.Map;

public interface CodeScanResultDao {
    Map<String, String> getCodeScanResult(Integer projectTaskId);
    Map<String,String> getMasterCodeScanResult(Integer projectTaskId);
    int updateCodeScanResultStatus(CodeScanResult codeScanResult);
}