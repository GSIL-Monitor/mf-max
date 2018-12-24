package com.mryx.matrix.codeanalyzer.core.service;

import com.mryx.matrix.codeanalyzer.domain.CodeScanJob;
import com.mryx.matrix.codeanalyzer.domain.CodeScanJobRecord;
import com.mryx.matrix.codeanalyzer.dto.AppDto;
import com.mryx.matrix.codeanalyzer.dto.CodeScanJobDto;
import com.mryx.matrix.codeanalyzer.dto.CodeScanJobRecordDto;
import com.mryx.matrix.common.domain.ResultVo;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

public interface P3cCodeScanJobService {
    /**
     * @param codeScanJob
     * @param httpServletRequest
     * @return
     */
    String getUser(CodeScanJob codeScanJob, HttpServletRequest httpServletRequest);

    /**
     * @param codeScanJob
     * @return
     */
    ResultVo<String> createCodeScanJob(CodeScanJob codeScanJob);

    /**
     * @param codeScanJobRecordDto
     * @return
     */
    ResultVo<String> updateScanResult(CodeScanJobRecordDto codeScanJobRecordDto);

    /**
     * @param codeScanJob
     * @return
     */
    Integer p3cTotal(CodeScanJob codeScanJob);

    /**
     * @param codeScanJob
     * @return
     */
    List<CodeScanJob> getCodeScanJob(CodeScanJob codeScanJob);

    /**
     * @param codeScanJob
     * @return
     */
    ResultVo<String> manualRunP3cJob(CodeScanJob codeScanJob);

    /**
     * @param codeScanJob
     * @return
     */
    CodeScanJob getCodeScanJobByJobId(CodeScanJob codeScanJob);

    /**
     * @param codeScanJob
     * @return
     */
    ResultVo<String> saveCodeScanJob(CodeScanJob codeScanJob);

    /**
     * @param codeScanJob
     * @return
     */
    ResultVo<String> deleteP3cCodeScanJob(CodeScanJob codeScanJob);

    /**
     * @param codeScanJob
     * @return
     */
    List<CodeScanJobRecord> getP3cDataWeek(CodeScanJob codeScanJob);

    /**
     * @param codeScanJob
     * @return
     */
    CodeScanJobDto getP3cData(CodeScanJob codeScanJob);

    /**
     * @param codeScanJob
     * @param userName
     * @return
     */
    ResultVo<String> saveCodeScanJobInfo(CodeScanJob codeScanJob, String userName);

    /**
     * @return
     */
    ResultVo<String> updateCodeScanJobRuningStatus();

    /**
     * @return
     */
    ResultVo<String> updateCodeScanJobRecordRuningStatus();

    /**
     * @param conditions
     * @return
     */
    List<CodeScanJob> getAllCodeScanJob(Map<String, Integer> conditions);

    /**
     * @param codeScanJob
     * @return
     */
    Integer updateAddSuccess(CodeScanJob codeScanJob);

    /**
     * @return
     */
    List<AppDto> getAllAppCode();
}
