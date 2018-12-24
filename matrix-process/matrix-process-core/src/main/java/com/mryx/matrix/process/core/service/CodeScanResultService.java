package com.mryx.matrix.process.core.service;

import com.mryx.matrix.codeanalyzer.domain.CodeScanResult;

import java.util.Map;

public interface CodeScanResultService {
    Map<String,String> getCodeScanResult(Integer projectTaskId);
    Map<String,String> getMasterCodeScanResult(Integer projectTaskId);
    int updateCodeScanResultStatus(CodeScanResult codeScanResult);
}