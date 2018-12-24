package com.mryx.matrix.process.core.service.impl;

import java.util.List;
import java.util.Date;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import com.mryx.matrix.process.core.dao.ProjectRecordDao;
import com.mryx.matrix.process.core.service.ProjectRecordService;

import com.mryx.matrix.process.domain.ProjectRecord;


/**
 * 项目流程记录表
 * @author zhaoxl
 * @email zhaoxl02@missfresh.cn
 * @date 2018-09-04 16:21
 **/
@Service("projectRecordService")
public class ProjectRecordServiceImpl implements ProjectRecordService {

	private static Logger logger = LoggerFactory.getLogger(ProjectRecordServiceImpl.class);

	@Resource
	private ProjectRecordDao projectRecordDao;

	@Override
	public ProjectRecord getById(Integer id) {
		return projectRecordDao.getById(id);
	}

	@Override
	public int insert(ProjectRecord projectRecord) {
		return projectRecordDao.insert(projectRecord);
	}

	@Override
	public int updateById(ProjectRecord projectRecord) {
		return projectRecordDao.updateById(projectRecord);
	}

	@Override
	public int pageTotal(ProjectRecord projectRecord) {
		return projectRecordDao.pageTotal(projectRecord);
	}

	@Override
	public List<ProjectRecord> listPage(ProjectRecord projectRecord) {
		return projectRecordDao.listPage(projectRecord);
	}

	@Override
	public List<ProjectRecord> listByCondition(ProjectRecord projectRecord){
		return projectRecordDao.listByCondition(projectRecord);
	}

}
