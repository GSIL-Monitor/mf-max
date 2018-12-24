package com.mryx.matrix.process.core.dao;

import java.util.Map;

/**
 * @program: matrix-process
 * @description: 获取创建代码评审生成的链接
 * @author: jianghong
 * @create: 2018-10-10 17:40
 */
public interface CodeReviewResultDao {
    String getCodeReviewLinkUrl(Integer projectTaskId);
}
