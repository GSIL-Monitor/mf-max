package com.mryx.matrix.process.core.service.impl;

import com.mryx.matrix.codeanalyzer.domain.CodeScanResult;
import com.mryx.matrix.process.core.dao.CodeScanResultDao;
import com.mryx.matrix.process.core.service.CodeScanResultService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Map;

@Service("codeScanResultService")
public class CodeScanResultServiceImpl implements CodeScanResultService {
    @Resource
    private CodeScanResultDao codeScanResultDao;

    @Override
    public Map<String, String> getCodeScanResult(Integer projectTaskId) {
        return codeScanResultDao.getCodeScanResult(projectTaskId);
    }

    @Override
    public Map<String, String> getMasterCodeScanResult(Integer projectTaskId) {
        return codeScanResultDao.getMasterCodeScanResult(projectTaskId);
    }

    @Override
    public int updateCodeScanResultStatus(CodeScanResult codeScanResult) {
        return codeScanResultDao.updateCodeScanResultStatus(codeScanResult);
    }
}