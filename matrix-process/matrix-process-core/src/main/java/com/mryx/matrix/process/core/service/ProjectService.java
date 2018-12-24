package com.mryx.matrix.process.core.service;

import java.util.List;
import java.util.Date;
import java.util.Map;

import com.mryx.matrix.common.domain.ResultVo;
import com.mryx.matrix.process.domain.Project;
import com.mryx.matrix.process.dto.*;


/**
 * 应用发布工单表
 *
 * @author zhaoxl
 * @email zhaoxl02@missfresh.cn
 * @date 2018-09-04 16:21
 **/
public interface ProjectService {

    ProjectDTO getById(Integer id);

    int insert(Project project);

    int updateById(Project project);

    int pageTotal(Project project);

    List<Project> listPage(Project project);

    Integer countProjectDTOTotal(ProjectDTO projectdto);

    List<ProjectDTO> listProjectDTOPage(ProjectDTO projectdto);

    List<Project> listByCondition(Project project);

    List<AppListsDto> dealAppcodeInfo(Long id, String appCode);

    AppInfoDto getAppDetailInfo(Map<String, Object> mapInfo);

    Project getProjectById(Integer id);

    ResultVo createCodeScan(Project project);

    ResultVo createCodeReview(Project project);
}
