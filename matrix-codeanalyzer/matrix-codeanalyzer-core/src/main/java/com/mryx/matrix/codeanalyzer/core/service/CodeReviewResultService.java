package com.mryx.matrix.codeanalyzer.core.service;

import com.mryx.matrix.codeanalyzer.domain.CodeReviewResult;
import com.mryx.matrix.publish.domain.ProjectTask;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @program: matrix-codeanalyzer
 * @description: 代码评审结果表
 * @author: jianghong
 * @create: 2018-09-28 16:34
 */
public interface CodeReviewResultService {
    String getCodeReviewPeople(Integer id);

    Map<String, Object> setCodeReviewCreateParameters(String gitAddress, String appBranch, String projectTaskId, List<String> codeReviewPerson);

    Map<String, Object> setCodeReviewStatusParameters(String gitAddress, String appBranch, String projectTaskId);

    Optional getHTTPRequest(String url, Map<String, Object> json) throws RuntimeException;

    int insertCodeReviewResult(CodeReviewResult codeReviewResult);

    int updateCodeReviewResult(CodeReviewResult codeReviewResult);

    String getCodeReviewStatus(Integer id);

    String getCodeReviewStatus(CodeService codeService, CodeReviewResultService codeReviewResultService, ProjectTask projectTask, String appProjectTasksRemote, String appDetailRemote, String codeReviewRemote);
}