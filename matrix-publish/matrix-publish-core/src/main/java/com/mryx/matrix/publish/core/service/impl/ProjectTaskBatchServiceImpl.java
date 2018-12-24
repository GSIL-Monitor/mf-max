package com.mryx.matrix.publish.core.service.impl;

import com.mryx.matrix.publish.core.dao.ProjectTaskBatchDao;
import com.mryx.matrix.publish.core.service.ProjectTaskBatchService;
import com.mryx.matrix.publish.domain.ProjectTaskBatch;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 *
 * @author dinglu
 * @date 2018/9/10
 */
@Service("projectTaskBatchService")
public class ProjectTaskBatchServiceImpl implements ProjectTaskBatchService {

    @Resource
    ProjectTaskBatchDao projectTaskBatchDao;

    @Override
    public ProjectTaskBatch getById(Integer id) {
        return projectTaskBatchDao.getById(id);
    }

    @Override
    public int insert(ProjectTaskBatch projectTaskBatch) {
        return projectTaskBatchDao.insert(projectTaskBatch);
    }

    @Override
    public int updateById(ProjectTaskBatch projectTaskBatch) {
        return projectTaskBatchDao.updateById(projectTaskBatch);
    }

    @Override
    public int pageTotal(ProjectTaskBatch projectTaskBatch) {
        return projectTaskBatchDao.pageTotal(projectTaskBatch);
    }

    @Override
    public List<ProjectTaskBatch> listPage(ProjectTaskBatch projectTaskBatch) {
        return projectTaskBatchDao.listPage(projectTaskBatch);
    }

    @Override
    public List<ProjectTaskBatch> listByCondition(ProjectTaskBatch projectTaskBatch) {
        return projectTaskBatchDao.listByCondition(projectTaskBatch);
    }

    @Override
    public int batchInsert(List<ProjectTaskBatch> projectTaskBatchList) {

        return  projectTaskBatchDao.batchInsert(projectTaskBatchList);

    }

    @Override
    public int save(List<ProjectTaskBatch> projectTaskBatchList) {
        ProjectTaskBatch projectTaskBatch = new ProjectTaskBatch();
        projectTaskBatch.setDelFlag(1);
        projectTaskBatch.setProjectTaskId(projectTaskBatchList.get(0).getProjectTaskId());
        List<ProjectTaskBatch> projectTaskBatches = projectTaskBatchDao.listByCondition(projectTaskBatch);
        if (!projectTaskBatches.isEmpty() && projectTaskBatches.size() >0){
            projectTaskBatchDao.deleteById(projectTaskBatches);
        }
        projectTaskBatchDao.batchInsert(projectTaskBatchList);
        if (projectTaskBatchList.isEmpty() || projectTaskBatchList.size() <=0){
            return 0;
        }
        return 1;
    }

    @Override
    public List<ProjectTaskBatch> getByProjectTaskId(Integer projectTaskId) {
        return projectTaskBatchDao.getByProjectTaskId(projectTaskId);
    }

    @Override
    public int deleteByProjectTaskId(Integer projectTaskId) {
        return projectTaskBatchDao.deleteByProjectTaskId(projectTaskId);
    }

    @Override
    public int updateStatusToSuspend(ProjectTaskBatch projectTaskBatch) {
        return projectTaskBatchDao.updateStatusToSuspend(projectTaskBatch);
    }
}
