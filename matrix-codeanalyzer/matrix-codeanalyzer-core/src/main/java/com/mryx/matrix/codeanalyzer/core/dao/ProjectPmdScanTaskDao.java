package com.mryx.matrix.codeanalyzer.core.dao;


import com.mryx.matrix.codeanalyzer.domain.CodeScanResult;
import com.mryx.matrix.codeanalyzer.domain.ProjectPmdScanTask;

import java.util.List;

public interface ProjectPmdScanTaskDao {
    /**
     *
     * @param projectPmdScanTask
     * @return
     */
    Integer insertProjectPmdScanTask(ProjectPmdScanTask projectPmdScanTask);

    /**
     *
     * @param codeScanResult
     * @return
     */
    Integer updatePmdScanTask(CodeScanResult codeScanResult);

    /**
     *
     * @param projectPmdScanTask
     * @return
     */
    Integer updateProjectPmdScanTask(ProjectPmdScanTask projectPmdScanTask);

    /**
     *
     * @param appCode
     * @return
     */
    Integer getJobIdByAppCode(String appCode);

    /**
     *
     * @param projectPmdScanTask
     * @return
     */
    Integer updateProjectPmdScanTaskStatus(ProjectPmdScanTask projectPmdScanTask);

    /**
     *
     * @param projectPmdScanTask
     * @return
     */
    Integer pageTotal(ProjectPmdScanTask projectPmdScanTask);

    /**
     *
     * @param projectPmdScanTask
     * @return
     */
    List<ProjectPmdScanTask> getPmdScanTask(ProjectPmdScanTask projectPmdScanTask);

    /**
     *
     * @param codeScanResult
     * @return
     */
    Integer updatePmdScanTaskStatus(CodeScanResult codeScanResult);
}
