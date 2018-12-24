package com.mryx.matrix.process.core.service.impl;

import java.util.List;

import com.mryx.matrix.process.domain.ReportDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import com.mryx.matrix.process.core.dao.ReportDao;
import com.mryx.matrix.process.core.service.ReportService;


/**
 * 周报表
 * @author juqing
 * @email jvqing@missfresh.cn
 * @date 2018-09-05 15:14
 **/
@Service("reportService")
public class ReportServiceImpl implements ReportService {

	private static Logger logger = LoggerFactory.getLogger(ReportServiceImpl.class);

	@Resource
	private ReportDao reportDao;

	@Override
	public ReportDTO getById(Integer id) {
		return reportDao.getById(id);
	}

	@Override
	public int insert(ReportDTO report) {
		return reportDao.insert(report);
	}

	@Override
	public int updateById(ReportDTO report) {
		return reportDao.updateById(report);
	}

	@Override
	public int pageTotal(ReportDTO report) {
		return reportDao.pageTotal(report);
	}

	@Override
	public List<ReportDTO> listPage(ReportDTO report) {
		return reportDao.listPage(report);
	}

	@Override
	public List<ReportDTO> listByCondition(ReportDTO report){
		return reportDao.listByCondition(report);
	}

}
