package com.mryx.matrix.publish.core.service.impl;

import com.mryx.matrix.publish.core.dao.ProjectTaskDao;
import com.mryx.matrix.publish.core.service.ProjectTaskService;
import com.mryx.matrix.publish.domain.ProjectTask;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 *
 * @author dinglu
 * @date 2018/9/10
 */

@Service("projectTaskService")
public class ProjectTaskServiceImpl implements ProjectTaskService{

    @Resource
    ProjectTaskDao projectTaskDao;

    @Override
    public ProjectTask getById(Integer id) {
        return projectTaskDao.getById(id);
    }

    @Override
    public int insert(ProjectTask projectTask) {
        return projectTaskDao.insert(projectTask);
    }

    @Override
    public int updateById(ProjectTask projectTask) {
        return projectTaskDao.updateById(projectTask);
    }

    @Override
    public int pageTotal(ProjectTask projectTask) {
        return projectTaskDao.pageTotal(projectTask);
    }

    @Override
    public List<ProjectTask> listPage(ProjectTask projectTask) {
        return projectTaskDao.listPage(projectTask);
    }

    @Override
    public List<ProjectTask> listByCondition(ProjectTask projectTask) {
        return projectTaskDao.listByCondition(projectTask);
    }

    @Override
    public int batchInsert(List<ProjectTask> projectTaskList) {
        return projectTaskDao.batchInsert(projectTaskList);
    }

    @Override
    public int batchDelete(List<ProjectTask> projectTaskList) {
        return projectTaskDao.batchDelete(projectTaskList);
    }

    @Override
    public List<ProjectTask> getProjectTasks(Integer id){
        return projectTaskDao.getProjectTasks(id);
    }
}
