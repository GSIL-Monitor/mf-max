package com.mryx.matrix.project.dao;

import com.mryx.matrix.project.domain.Project;

import java.util.List;

/**
 * ProjectDao
 *
 * @author supeng
 * @date 2018/11/11
 */
public interface ProjectDao {
    /**
     * 创建项目
     *
     * @param project
     * @return
     */
    Integer createProject(Project project);

    /**
     * 更新项目
     *
     * @param project
     * @return
     */
    Integer updateProject(Project project);

    /**
     * 项目列表
     *
     * @param project
     * @return
     */
    List<Project> listProject(Project project);
}
