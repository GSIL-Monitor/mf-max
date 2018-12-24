package com.mryx.matrix.codeanalyzer.core.dao;

import com.mryx.matrix.codeanalyzer.domain.CodeScanResult;
import com.mryx.matrix.publish.domain.ProjectTask;

import java.util.List;
import java.util.Map;

/**
 * Demo Dao
 *
 * @author supeng
 * @date 2018/09/25
 */
public interface CodeDao {
    /**
     * 代码扫描结果入库
     *
     * @param codeScanResult
     * @return
     */
    int insertCodeScanResult(CodeScanResult codeScanResult);

    /**
     * 更新Code Scan记录的状态
     *
     * @param codeScanResult
     * @return
     */
    int updateCodeScanStatus(CodeScanResult codeScanResult);

    /**
     * 根据ProjectTask的id查询其主键id
     *
     * @param projectTaskId
     * @return
     */
    Integer getIdByProjectTaskId(Integer projectTaskId);

    /**
     * 重新运行时，代码扫描结果更新
     *
     * @param codeScanResult
     * @return
     */
    Integer updateCodeScanResult(CodeScanResult codeScanResult);

    /**
     * 当前分支发布成功后，该分支的is_master字段设置为1，表明其是上线发布过的
     *
     * @param codeScanResult
     * @return
     */
    Integer updateMaster(CodeScanResult codeScanResult);

    /**
     * 根据projectTaskId和isMaster查询主键id
     *
     * @param para
     * @return
     */
    Integer getIdByProjectTaskIdAndIsMaster(Map<String,Integer> para);
}