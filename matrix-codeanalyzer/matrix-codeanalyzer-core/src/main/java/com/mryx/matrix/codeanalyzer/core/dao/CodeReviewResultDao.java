package com.mryx.matrix.codeanalyzer.core.dao;

import com.mryx.matrix.codeanalyzer.domain.CodeReviewResult;

/**
 * @program: matrix-codeanalyzer
 * @description: 代码评审结果表
 * @author: jianghong
 * @create: 2018-09-28 15:05
 */
public interface CodeReviewResultDao {
    String getCodeReviewPeople(Integer id);

    int insertCodeReviewResult(CodeReviewResult codeReviewResult);

    int updateCodeReviewResult(CodeReviewResult codeReviewResult);

    String getCodeReviewStatus(Integer id);

}