package com.mryx.matrix.publish.core.service.impl;

import com.mryx.matrix.publish.core.dao.AppStartResultDao;
import com.mryx.matrix.publish.core.service.AppStartResultService;
import com.mryx.matrix.publish.domain.AppStartResult;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * AppStartResultService Impl
 *
 * @author supeng
 * @date 2018/11/17
 */
@Service
public class AppStartResultServiceImpl implements AppStartResultService {

    @Resource
    private AppStartResultDao appStartResultDao;

    @Override
    public Integer insert(AppStartResult appStartResult) {
        return appStartResultDao.insert(appStartResult);
    }

    @Override
    public AppStartResult selectByParameter(AppStartResult appStartResult) {
        return appStartResultDao.selectByParameter(appStartResult);
    }
}
