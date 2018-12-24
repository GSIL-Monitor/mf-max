package com.mryx.matrix.codeanalyzer.core.service;

import com.mryx.matrix.codeanalyzer.domain.CodeScanResult;
import com.mryx.matrix.codeanalyzer.domain.ProjectPmdScanTask;
import com.mryx.matrix.common.domain.ResultVo;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public interface ProjectPmdScanTaskService {

    /**
     * @param projectCodeScanTask
     * @return
     */
    ResultVo<String> createPmdScanTask(ProjectPmdScanTask projectCodeScanTask);

    /**
     * @param projectPmdScanTask
     * @return
     */
    Integer insertProjectPmdScanTask(ProjectPmdScanTask projectPmdScanTask);

    /**
     * @param appCode
     * @return
     */
    Integer getJobIdByAppCode(String appCode);

    /**
     * @param codeScanResult
     * @return
     */
    Integer updateProjectPmdScanTask(CodeScanResult codeScanResult);

    /**
     * @param projectPmdScanTask
     * @return
     */
    Integer updateProjectPmdScanTask(ProjectPmdScanTask projectPmdScanTask);

    /**
     * @param projectPmdScanTask
     * @return
     */
    Integer updateProjectPmdScanTaskStatus(ProjectPmdScanTask projectPmdScanTask);
    /**
     * @param codeScanResult
     * @return
     */
    Integer updateProjectPmdScanTaskStatus(CodeScanResult codeScanResult);

    /**
     * @param projectPmdScanTask
     * @return
     */
    Integer pageTotal(ProjectPmdScanTask projectPmdScanTask);

    /**
     * @param projectPmdScanTask
     * @return
     */
    List<ProjectPmdScanTask> getPmdScanTask(ProjectPmdScanTask projectPmdScanTask);
}
