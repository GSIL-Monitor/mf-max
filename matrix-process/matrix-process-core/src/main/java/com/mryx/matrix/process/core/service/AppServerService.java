package com.mryx.matrix.process.core.service;

import com.mryx.matrix.process.domain.AppServer;
import com.mryx.matrix.process.dto.AppServerDTO;

import java.util.List;

/**
 * @author pengcheng
 * @email pengcheng@missfresh.cn
 * @date 2018-10-19 16:32
 **/
public interface AppServerService {

    AppServer getById(Integer id);

    int insert(AppServer appServer);

    int updateById(AppServer appServer);

    int pageTotal(AppServerDTO appServer);

    List<AppServer> listPage(AppServerDTO appServer);

    List<AppServer> listByCondition(AppServerDTO appServer);

    int batchUpdateOrInsert(List<AppServer> appServerList);

    int batchUpdateByIp(AppServer appServer);

    List<String> listAllIps();
}
