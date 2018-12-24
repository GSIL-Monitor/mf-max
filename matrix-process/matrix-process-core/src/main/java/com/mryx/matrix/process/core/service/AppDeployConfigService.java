package com.mryx.matrix.process.core.service;

import java.util.List;
import java.util.Date;
import java.util.Map;

import com.mryx.matrix.process.domain.AppDeployConfig;



/**
 * 
 * @author pengcheng
 * @email pengcheng@missfresh.cn
 * @date 2018-10-19 16:32
 **/
public interface AppDeployConfigService {

	AppDeployConfig getById(Integer id);

	int insert(AppDeployConfig appDeployConfig);

	int updateById(AppDeployConfig appDeployConfig);

	int pageTotal(AppDeployConfig appDeployConfig);

	List<AppDeployConfig> listPage(AppDeployConfig appDeployConfig);

	List<AppDeployConfig> listByCondition(AppDeployConfig appDeployConfig);

    int batchUpdateOrInsert(List<AppDeployConfig> appDeployConfigList);
}
