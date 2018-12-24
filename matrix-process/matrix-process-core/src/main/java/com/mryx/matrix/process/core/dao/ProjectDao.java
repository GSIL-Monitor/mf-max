package com.mryx.matrix.process.core.dao;

import java.util.List;
import java.util.Date;
import java.util.Map;

import com.mryx.matrix.process.domain.Project;
import com.mryx.matrix.process.dto.ProjectDTO;


/**
 * 应用发布工单表
 * @author zhaoxl
 * @email zhaoxl02@missfresh.cn
 * @date 2018-09-04 16:21
 **/
public interface ProjectDao {

	Project getById(Integer id);

	int insert(Project project);

	int updateById(Project project);

	int pageTotal(Project project);

	List<Project> listPage(Project project);

	List<Project> listByCondition(Project project);

	Integer countProjectDTOTotal(ProjectDTO projectdto);

	List<ProjectDTO> listProjectDTOPage(ProjectDTO projectdto);


}
