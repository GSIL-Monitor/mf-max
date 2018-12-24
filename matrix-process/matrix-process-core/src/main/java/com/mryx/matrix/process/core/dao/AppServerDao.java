package com.mryx.matrix.process.core.dao;

import java.util.List;
import java.util.Date;
import java.util.Map;

import com.mryx.matrix.process.domain.AppServer;
import com.mryx.matrix.process.dto.AppServerDTO;


/**
 * @author pengcheng
 * @email pengcheng@missfresh.cn
 * @date 2018-10-19 16:32
 **/
public interface AppServerDao {

    AppServer getById(Integer id);

    int insert(AppServer appServer);

    int updateById(AppServer appServer);

    int pageTotal(AppServerDTO appServer);

    List<AppServer> listPage(AppServerDTO appServer);

    List<AppServer> listByCondition(AppServerDTO appServer);

    int batchUpdateOrInsert(List<AppServer> appServerList);

    int batchUpdateByIp(AppServer appServer);

    /**
     * 获取所有IP地址
     *
     * @return IP地址列表
     */
    List<String> listAllIps();
}
