package com.mryx.matrix.publish.core.service;

import com.mryx.matrix.publish.domain.ProjectTaskBatch;

import java.util.List;

/**
 * Created by dinglu on 2018/9/10.
 */
public interface ProjectTaskBatchService {

    ProjectTaskBatch getById(Integer id);

    int insert(ProjectTaskBatch projectTaskBatch);

    int updateById(ProjectTaskBatch projectTaskBatch);

    int pageTotal(ProjectTaskBatch projectTaskBatch);

    List<ProjectTaskBatch> listPage(ProjectTaskBatch projectTaskBatch);

    List<ProjectTaskBatch> listByCondition(ProjectTaskBatch projectTaskBatch);

    int batchInsert(List<ProjectTaskBatch> projectTaskBatchList);

    int save(List<ProjectTaskBatch> projectTaskBatchList);

    List<ProjectTaskBatch> getByProjectTaskId(Integer projectTaskId);

    int deleteByProjectTaskId(Integer projectTaskId);

    int updateStatusToSuspend(ProjectTaskBatch projectTaskBatch);
}
