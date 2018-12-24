package com.mryx.matrix.process.core.service.impl;

import com.mryx.matrix.process.core.dao.AppServerDao;
import com.mryx.matrix.process.core.service.AppServerService;
import com.mryx.matrix.process.domain.AppServer;
import com.mryx.matrix.process.dto.AppServerDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;


/**
 * @author pengcheng
 * @email pengcheng@missfresh.cn
 * @date 2018-10-19 16:32
 **/
@Service("appServerService")
public class AppServerServiceImpl implements AppServerService {

    private static Logger logger = LoggerFactory.getLogger(AppServerServiceImpl.class);

    @Resource
    private AppServerDao appServerDao;

    @Override
    public AppServer getById(Integer id) {
        return appServerDao.getById(id);
    }

    @Override
    public int insert(AppServer appServer) {
        return appServerDao.insert(appServer);
    }

    @Override
    public int updateById(AppServer appServer) {
        return appServerDao.updateById(appServer);
    }

    @Override
    public int pageTotal(AppServerDTO appServer) {
        return appServerDao.pageTotal(appServer);
    }

    @Override
    public List<AppServer> listPage(AppServerDTO appServer) {
        return appServerDao.listPage(appServer);
    }

    @Override
    public List<AppServer> listByCondition(AppServerDTO appServer) {
        return appServerDao.listByCondition(appServer);
    }

    @Override
    public int batchUpdateOrInsert(List<AppServer> appServerList) {
        return appServerDao.batchUpdateOrInsert(appServerList);
    }

    @Override
    public int batchUpdateByIp(AppServer appServer) {
        return appServerDao.batchUpdateByIp(appServer);
    }

    @Override
    public List<String> listAllIps() {
        return appServerDao.listAllIps();
    }

}
