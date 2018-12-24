package com.mryx.matrix.publish.core.service.impl;

import java.util.List;
import java.util.Date;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import com.mryx.matrix.publish.core.dao.ReleaseDelpoyRecordDao;
import com.mryx.matrix.publish.core.service.ReleaseDelpoyRecordService;

import com.mryx.matrix.publish.domain.ReleaseDelpoyRecord;


/**
 * 生产环境部署记录表
 * @author zhaoxl
 * @email zhaoxl02@missfresh.cn
 * @date 2018-09-07 15:55
 **/
@Service("releaseDelpoyRecordService")
public class ReleaseDelpoyRecordServiceImpl implements ReleaseDelpoyRecordService {

	private static Logger logger = LoggerFactory.getLogger(ReleaseDelpoyRecordServiceImpl.class);

	@Resource
	private ReleaseDelpoyRecordDao releaseDelpoyRecordDao;

	@Override
	public ReleaseDelpoyRecord getById(Integer id) {
		return releaseDelpoyRecordDao.getById(id);
	}

	@Override
	public ReleaseDelpoyRecord getByRecordId(Integer recordId) {
		return releaseDelpoyRecordDao.getByRecordId(recordId);
	}

	@Override
	public ReleaseDelpoyRecord getByProjectId(Integer projectId){
		return releaseDelpoyRecordDao.getByProjectId(projectId);
	}

	@Override
	public int insert(ReleaseDelpoyRecord releaseDelpoyRecord) {
		return releaseDelpoyRecordDao.insert(releaseDelpoyRecord);
	}

	@Override
	public int updateById(ReleaseDelpoyRecord releaseDelpoyRecord) {
		return releaseDelpoyRecordDao.updateById(releaseDelpoyRecord);
	}

	@Override
	public int pageTotal(ReleaseDelpoyRecord releaseDelpoyRecord) {
		return releaseDelpoyRecordDao.pageTotal(releaseDelpoyRecord);
	}

	@Override
	public List<ReleaseDelpoyRecord> listPage(ReleaseDelpoyRecord releaseDelpoyRecord) {
		return releaseDelpoyRecordDao.listPage(releaseDelpoyRecord);
	}

	@Override
	public List<ReleaseDelpoyRecord> listByCondition(ReleaseDelpoyRecord releaseDelpoyRecord){
		return releaseDelpoyRecordDao.listByCondition(releaseDelpoyRecord);
	}

	@Override
	public int batchInsert(List<ReleaseDelpoyRecord> releaseDelpoyRecordList) {
		return releaseDelpoyRecordDao.batchInsert(releaseDelpoyRecordList);
	}

	@Override
	public int deleteById(ReleaseDelpoyRecord releaseDelpoyRecord) {
		return releaseDelpoyRecordDao.deleteById(releaseDelpoyRecord);
	}

	@Override
	public int batchDelete(List<ReleaseDelpoyRecord> releaseDelpoyRecordList) {
		return releaseDelpoyRecordDao.batchDelete(releaseDelpoyRecordList);
	}

	@Override
	public List<ReleaseDelpoyRecord> listByCodeStatus(String appCode) {
		return releaseDelpoyRecordDao.listByCodeStatus(appCode);
	}

	@Override
	public ReleaseDelpoyRecord getByLastId(Integer projectTaskId) {
		return releaseDelpoyRecordDao.getByLastId(projectTaskId);
	}

	@Override
	public List<ReleaseDelpoyRecord> listByProjectStatus(ReleaseDelpoyRecord releaseDelpoyRecord){
		return releaseDelpoyRecordDao.listByProjectStatus(releaseDelpoyRecord);
	}

}
