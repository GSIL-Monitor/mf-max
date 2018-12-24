package com.mryx.matrix.publish.core.service.impl;

import java.util.List;
import java.util.Date;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import com.mryx.matrix.publish.core.dao.BetaDelpoyRecordDao;
import com.mryx.matrix.publish.core.service.BetaDelpoyRecordService;

import com.mryx.matrix.publish.domain.BetaDelpoyRecord;


/**
 * 测试环境部署记录表
 * @author zhaoxl
 * @email zhaoxl02@missfresh.cn
 * @date 2018-09-07 15:55
 **/
@Service("betaDelpoyRecordService")
public class BetaDelpoyRecordServiceImpl implements BetaDelpoyRecordService {

	private static Logger logger = LoggerFactory.getLogger(BetaDelpoyRecordServiceImpl.class);

	@Resource
	private BetaDelpoyRecordDao betaDelpoyRecordDao;

	@Override
	public BetaDelpoyRecord getById(Integer id) {
		return betaDelpoyRecordDao.getById(id);
	}

	@Override
	public int insert(BetaDelpoyRecord betaDelpoyRecord) {
		return betaDelpoyRecordDao.insert(betaDelpoyRecord);
	}

	@Override
	public int updateById(BetaDelpoyRecord betaDelpoyRecord) {
		return betaDelpoyRecordDao.updateById(betaDelpoyRecord);
	}

	@Override
	public int pageTotal(BetaDelpoyRecord betaDelpoyRecord) {
		return betaDelpoyRecordDao.pageTotal(betaDelpoyRecord);
	}

	@Override
	public List<BetaDelpoyRecord> listPage(BetaDelpoyRecord betaDelpoyRecord) {
		return betaDelpoyRecordDao.listPage(betaDelpoyRecord);
	}

	@Override
	public List<BetaDelpoyRecord> listByCondition(BetaDelpoyRecord betaDelpoyRecord){
		return betaDelpoyRecordDao.listByCondition(betaDelpoyRecord);
	}

	@Override
	public void  batchInsert(List<BetaDelpoyRecord> betaDelpoyRecordList) {
		betaDelpoyRecordDao.batchInsert(betaDelpoyRecordList);
		return ;
	}

	@Override
	public List<BetaDelpoyRecord> listByCodeStatus(String appCode) {
		return betaDelpoyRecordDao.listByCodeStatus(appCode);
	}

	@Override
	public BetaDelpoyRecord getLastId(Integer projectTaskId) {
		return betaDelpoyRecordDao.getLastId(projectTaskId);
	}


}
