package com.mryx.matrix.publish.core.service;

import com.mryx.matrix.publish.domain.AppStartResult;

/**
 * AppStartResultService
 *
 * @author supeng
 * @date 2018/11/17
 */
public interface AppStartResultService {
    /**
     * 插入数据
     *
     * @param appStartResult
     * @return
     */
    Integer insert(AppStartResult appStartResult);

    /**
     * 查询数据库
     *
     * @param appStartResult
     * @return
     */
    AppStartResult selectByParameter(AppStartResult appStartResult);
}
