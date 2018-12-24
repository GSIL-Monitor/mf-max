package com.mryx.matrix.codeanalyzer.core.service;

import com.mryx.matrix.codeanalyzer.domain.CodeScanResult;
import com.mryx.matrix.codeanalyzer.dto.CodeScanResultDto;
import com.mryx.matrix.common.domain.ResultVo;
import com.mryx.matrix.publish.domain.ProjectTask;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Code Service
 *
 * @author supeng
 * @date 2018/09/25
 */
public interface CodeService {

    /**
     * 将gitAddress、appBranch、projectTaskId转换成map
     *
     * @param gitAddress
     * @param codeBranch
     * @param projectTaskId
     * @return
     */
    Map<String, Object> setParameters(String gitAddress, String codeBranch, String projectTaskId);

    /**
     * 请求其他服务接口
     *
     * @param url
     * @param json
     * @return
     */
    Optional<String> codeScanRequest(String url, Map<String, Object> json);

    /**
     * 代码扫描结果入库
     *
     * @param codeScanResult
     * @return
     */
    int insertCodeScanResult(CodeScanResult codeScanResult);

    /**
     * 根据appCode获取gitAddress
     *
     * @param url
     * @param appCode
     * @return
     */
    ResultVo<String> getGitAddress(String url, String appCode);

    /**
     * 更新CodeScan的状态
     * TODO 需要一个记录的标识作为参数（更新条件）
     *
     * @param codeScanResultDto
     * @return
     */
    ResultVo updateCodeScanStatus(CodeScanResultDto codeScanResultDto);

    /**
     * 根据ProjectTask的id查询其主键id
     *
     * @param projectTaskId
     * @return
     */
    Integer getIdByProjectTaskId(Integer projectTaskId);

    /**
     * 获取当前扫描分支与上次发布分支的对比结果
     *
     * @param projectTask
     * @return
     */
    Map<String, String> compareResult(ProjectTask projectTask);

    /**
     * 当前分支发布成功后，该分支的is_master字段设置为1，表明其是上线发布过的
     *
     * @param codeScanResult
     * @return
     */
    Integer updateMaster(CodeScanResult codeScanResult);

    /**
     * 重新运行时，代码扫描结果更新
     *
     * @param codeScanResult
     * @return
     */
    Integer updateCodeScanResult(CodeScanResult codeScanResult);

    /**
     * 根据projectTaskId和isMaster查询主键id
     *
     * @param para
     * @return
     */
    Integer getIdByProjectTaskIdAndIsMaster(Map<String,Integer> para);
}
