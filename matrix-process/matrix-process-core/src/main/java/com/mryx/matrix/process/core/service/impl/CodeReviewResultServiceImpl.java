package com.mryx.matrix.process.core.service.impl;

import com.mryx.matrix.process.core.dao.CodeReviewResultDao;
import com.mryx.matrix.process.core.service.CodeReviewResultService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Map;

/**
 * @program: matrix-process
 * @description: 获取创建代码评审生成的链接
 * @author: jianghong
 * @create: 2018-10-10 17:43
 */

@Service("codeReviewResultService")
public class CodeReviewResultServiceImpl implements CodeReviewResultService {

    @Resource
    private CodeReviewResultDao codeReviewResultDao;

    @Override
    public String getCodeReviewLinkUrl(Integer projectTaskId){
        return codeReviewResultDao.getCodeReviewLinkUrl(projectTaskId);
    }
}
