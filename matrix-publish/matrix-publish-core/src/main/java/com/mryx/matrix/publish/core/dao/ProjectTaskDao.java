package com.mryx.matrix.publish.core.dao;

import com.mryx.matrix.publish.domain.ProjectTask;

import java.util.List;

/**
 * Created by dinglu on 2018/9/10.
 */
public interface ProjectTaskDao {

    ProjectTask getById(Integer id);

    int insert(ProjectTask projectTask);

    int updateById(ProjectTask projectTask);

    int pageTotal(ProjectTask projectTask);

    List<ProjectTask> listPage(ProjectTask projectTask);

    List<ProjectTask> listByCondition(ProjectTask projectTask);

    int batchInsert(List<ProjectTask> projectTaskList);

    int batchDelete(List<ProjectTask> projectTaskList);

    List<ProjectTask> getProjectTasks(Integer id);

}
