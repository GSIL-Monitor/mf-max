package com.mryx.matrix.process.core.service;

import java.util.List;
import java.util.Date;
import java.util.Map;

import com.mryx.matrix.process.domain.App;


/**
 * @author pengcheng
 * @email pengcheng@missfresh.cn
 * @date 2018-10-19 16:32
 **/
public interface AppService {

    App getById(Integer id);

    int insert(App app);

    int updateById(App app);

    int pageTotal(App app);

    List<App> listPage(App app);

    List<App> listByCondition(App app);

    int batchUpdateOrInsert(List<App> AppList);

    int updateByCondition(App app);
}
