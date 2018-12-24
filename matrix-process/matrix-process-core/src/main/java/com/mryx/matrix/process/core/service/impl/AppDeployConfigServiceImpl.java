package com.mryx.matrix.process.core.service.impl;

import java.util.List;
import java.util.Date;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import com.mryx.matrix.process.core.dao.AppDeployConfigDao;
import com.mryx.matrix.process.core.service.AppDeployConfigService;

import com.mryx.matrix.process.domain.AppDeployConfig;


/**
 * 
 * @author pengcheng
 * @email pengcheng@missfresh.cn
 * @date 2018-10-19 16:32
 **/
@Service("appDeployConfigService")
public class AppDeployConfigServiceImpl implements AppDeployConfigService {

	private static Logger logger = LoggerFactory.getLogger(AppDeployConfigServiceImpl.class);

	@Resource
	private AppDeployConfigDao appDeployConfigDao;

	@Override
	public AppDeployConfig getById(Integer id) {
		return appDeployConfigDao.getById(id);
	}

	@Override
	public int insert(AppDeployConfig appDeployConfig) {
		return appDeployConfigDao.insert(appDeployConfig);
	}

	@Override
	public int updateById(AppDeployConfig appDeployConfig) {
		return appDeployConfigDao.updateById(appDeployConfig);
	}

	@Override
	public int pageTotal(AppDeployConfig appDeployConfig) {
		return appDeployConfigDao.pageTotal(appDeployConfig);
	}

	@Override
	public List<AppDeployConfig> listPage(AppDeployConfig appDeployConfig) {
		return appDeployConfigDao.listPage(appDeployConfig);
	}

	@Override
	public List<AppDeployConfig> listByCondition(AppDeployConfig appDeployConfig){
		return appDeployConfigDao.listByCondition(appDeployConfig);
	}

    @Override
    public int batchUpdateOrInsert(List<AppDeployConfig> appDeployConfigList) {
        return appDeployConfigDao.batchUpdateOrInsert(appDeployConfigList);
    }

}
