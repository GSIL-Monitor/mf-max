package com.mryx.matrix.process.core.dao;

import java.util.List;

import com.mryx.matrix.process.domain.ReportDTO;


/**
 * 周报表
 * @author juqing
 * @email jvqing@missfresh.cn
 * @date 2018-09-05 15:14
 **/
public interface ReportDao {

	ReportDTO getById(Integer id);

	int insert(ReportDTO report);

	int updateById(ReportDTO report);

	int pageTotal(ReportDTO report);

	List<ReportDTO> listPage(ReportDTO report);

	List<ReportDTO> listByCondition(ReportDTO report);

}
