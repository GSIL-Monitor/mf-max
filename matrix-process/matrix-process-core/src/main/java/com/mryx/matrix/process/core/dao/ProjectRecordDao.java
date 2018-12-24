package com.mryx.matrix.process.core.dao;

import java.util.List;
import java.util.Date;
import java.util.Map;

import com.mryx.matrix.process.domain.ProjectRecord;



/**
 * 项目流程记录表
 * @author zhaoxl
 * @email zhaoxl02@missfresh.cn
 * @date 2018-09-04 16:21
 **/
public interface ProjectRecordDao {

	ProjectRecord getById(Integer id);

	int insert(ProjectRecord projectRecord);

	int updateById(ProjectRecord projectRecord);

	int pageTotal(ProjectRecord projectRecord);

	List<ProjectRecord> listPage(ProjectRecord projectRecord);

	List<ProjectRecord> listByCondition(ProjectRecord projectRecord);

}
