package com.mryx.matrix.process.core.service.impl;

import java.util.List;
import java.util.Date;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import com.mryx.matrix.process.core.dao.AppDao;
import com.mryx.matrix.process.core.service.AppService;

import com.mryx.matrix.process.domain.App;


/**
 * @author pengcheng
 * @email pengcheng@missfresh.cn
 * @date 2018-10-19 16:32
 **/
@Service("appService")
public class AppServiceImpl implements AppService {

    private static Logger logger = LoggerFactory.getLogger(AppServiceImpl.class);

    @Resource
    private AppDao appDao;

    @Override
    public App getById(Integer id) {
        return appDao.getById(id);
    }

    @Override
    public int insert(App app) {
        return appDao.insert(app);
    }

    @Override
    public int updateById(App app) {
        return appDao.updateById(app);
    }

    @Override
    public int pageTotal(App app) {
        return appDao.pageTotal(app);
    }

    @Override
    public List<App> listPage(App app) {
        return appDao.listPage(app);
    }

    @Override
    public List<App> listByCondition(App app) {
        return appDao.listByCondition(app);
    }

    @Override
    public int batchUpdateOrInsert(List<App> appList) {
        return appDao.batchUpdateOrInsert(appList);
    }

    @Override
    public int updateByCondition(App app) {
        return appDao.updateByCondition(app);
    }


}
