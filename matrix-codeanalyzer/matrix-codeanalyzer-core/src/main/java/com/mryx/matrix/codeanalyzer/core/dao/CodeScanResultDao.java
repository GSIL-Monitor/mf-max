package com.mryx.matrix.codeanalyzer.core.dao;

import java.util.Map;

public interface CodeScanResultDao {
    Map<String, String> getCodeScanResult(Integer projectTaskId);

    Map<String, String> getMasterCodeScanResult(Integer projectTaskId);
}