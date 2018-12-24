package com.mryx.matrix.codeanalyzer.core.service;

import com.mryx.matrix.codeanalyzer.domain.CodeScanJob;
import com.mryx.matrix.codeanalyzer.domain.CodeScanJobRecord;
import com.mryx.matrix.codeanalyzer.domain.CodeScanResult;
import com.mryx.matrix.common.domain.ResultVo;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public interface ProjectCodeScanTaskService {

    ResultVo<String> createCodeScanJob(CodeScanJob codeScanJob);

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
     * 创建代码扫描任务
     *
     * @param codeScanResult
     * @return
     */
    ResultVo<String> createCodeScanTask(CodeScanResult codeScanResult);

    /**
     * @param codeScanJob
     * @return
     */
    ResultVo<String> manualRunP3cJob(CodeScanJob codeScanJob);

    /**
     * @param codeScanResult
     * @return
     */
    List<CodeScanResult> getCodeScanTask(CodeScanResult codeScanResult);

    /**
     * @param codeScanJob
     * @return
     */
    List<CodeScanJob> getCodeScanJob(CodeScanJob codeScanJob);

    /**
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
     * 获取IP
     *
     * @param httpServletRequest
     * @return
     */
    String getHttpIp(HttpServletRequest httpServletRequest);

    /**
     * @param accessToken
     * @param userAgent
     * @param ip
     * @return
     */
    String getUser(String accessToken, String userAgent, String ip);

    /**
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
    Integer insertP3cRecord(CodeScanJobRecord codeScanJobRecord);

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
    ResultVo<String> deleteP3cCodeScanJob(CodeScanJob codeScanJob);

    /**
     * @param codeScanJob
     * @return
     */
    Integer updateP3cDeletedStatus(CodeScanJob codeScanJob);
}
