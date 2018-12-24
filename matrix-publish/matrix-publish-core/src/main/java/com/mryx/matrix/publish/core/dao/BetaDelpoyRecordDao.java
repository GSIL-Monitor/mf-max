package com.mryx.matrix.publish.core.dao;

import java.util.List;
import java.util.Date;
import java.util.Map;

import com.mryx.matrix.publish.domain.BetaDelpoyRecord;



/**
 * 测试环境部署记录表
 * @author zhaoxl
 * @email zhaoxl02@missfresh.cn
 * @date 2018-09-07 15:55
 **/
public interface BetaDelpoyRecordDao {

	BetaDelpoyRecord getById(Integer id);

	int insert(BetaDelpoyRecord betaDelpoyRecord);

	int updateById(BetaDelpoyRecord betaDelpoyRecord);

	int pageTotal(BetaDelpoyRecord betaDelpoyRecord);

	List<BetaDelpoyRecord> listPage(BetaDelpoyRecord betaDelpoyRecord);

	List<BetaDelpoyRecord> listByCondition(BetaDelpoyRecord betaDelpoyRecord);

	int batchInsert(List<BetaDelpoyRecord> betaDelpoyRecordList);

	List<BetaDelpoyRecord> listByCodeStatus(String appCode);

	BetaDelpoyRecord getLastId(Integer projectTaskId);
}
