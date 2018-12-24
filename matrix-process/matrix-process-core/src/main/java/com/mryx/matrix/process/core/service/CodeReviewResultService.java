package com.mryx.matrix.process.core.service;

import java.util.Map;

/**
 * @program: matrix-process
 * @description: 获取创建代码评审生成的链接
 * @author: jianghong
 * @create: 2018-10-10 17:44
 */
public interface CodeReviewResultService {
    String getCodeReviewLinkUrl(Integer projectTaskId);
}
