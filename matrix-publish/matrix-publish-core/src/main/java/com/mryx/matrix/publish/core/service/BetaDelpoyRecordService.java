package com.mryx.matrix.publish.core.service;

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
public interface BetaDelpoyRecordService {

	BetaDelpoyRecord getById(Integer id);

	/**
	 * @param betaDelpoyRecord
	 * @return int
	 */
	int insert(BetaDelpoyRecord betaDelpoyRecord);

	/**
	 * @param betaDelpoyRecord
	 * @return
	 */
	int updateById(BetaDelpoyRecord betaDelpoyRecord);

	/**
	 * @param betaDelpoyRecord
	 * @return
	 */
	int pageTotal(BetaDelpoyRecord betaDelpoyRecord);

	/**
	 * @param betaDelpoyRecord
	 * @return
	 */
	List<BetaDelpoyRecord> listPage(BetaDelpoyRecord betaDelpoyRecord);

	/**
	 * @param betaDelpoyRecord
	 * @return
	 */
	List<BetaDelpoyRecord> listByCondition(BetaDelpoyRecord betaDelpoyRecord);

	//TODO批量插入接口
	void batchInsert(List<BetaDelpoyRecord> betaDelpoyRecordList);

	List<BetaDelpoyRecord> listByCodeStatus(String appCode);

	BetaDelpoyRecord getLastId(Integer projectTaskId);

}
