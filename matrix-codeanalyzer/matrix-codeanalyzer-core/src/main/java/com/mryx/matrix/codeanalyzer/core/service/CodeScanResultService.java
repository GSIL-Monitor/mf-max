package com.mryx.matrix.codeanalyzer.core.service;

import java.util.Map;

public interface CodeScanResultService {
    Map<String, String> getCodeScanResult(Integer projectTaskId);

    Map<String, String> getMasterCodeScanResult(Integer projectTaskId);
}