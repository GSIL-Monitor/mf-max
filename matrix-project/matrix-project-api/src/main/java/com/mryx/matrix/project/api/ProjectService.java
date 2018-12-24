package com.mryx.matrix.project.api;

import com.mryx.matrix.common.domain.ResultVo;
import com.mryx.matrix.project.domain.Project;

/**
 * ProjectService
 *
 * @author supeng
 * @date 2018/11/11
 */
public interface ProjectService {
    /**
     * 创建项目
     *
     * @param project
     * @return
     */
    ResultVo createProject(Project project);

    /**
     * 更新项目
     *
     * @param project
     * @return
     */
    ResultVo updateProject(Project project);

    /**
     * 项目列表
     *
     * @param project
     * @return
     */
    ResultVo listProject(Project project);
}
