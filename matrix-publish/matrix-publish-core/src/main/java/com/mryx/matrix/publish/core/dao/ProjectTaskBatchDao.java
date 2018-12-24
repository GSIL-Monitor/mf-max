package com.mryx.matrix.publish.core.dao;

import com.mryx.matrix.publish.domain.BetaDelpoyRecord;
import com.mryx.matrix.publish.domain.ProjectTaskBatch;

import java.util.List;

/**
 * Created by dinglu on 2018/9/10.
 */
public interface ProjectTaskBatchDao {


    ProjectTaskBatch getById(Integer id);

    int insert(ProjectTaskBatch projectTaskBatch);

    int updateById(ProjectTaskBatch projectTaskBatch);

    int pageTotal(ProjectTaskBatch projectTaskBatch);

    List<ProjectTaskBatch> listPage(ProjectTaskBatch projectTaskBatch);

    List<ProjectTaskBatch> listByCondition(ProjectTaskBatch projectTaskBatch);

    int batchInsert(List<ProjectTaskBatch> projectTaskBatchList);

    int deleteById(List<ProjectTaskBatch> projectTaskBatches);

    List<ProjectTaskBatch> getByProjectTaskId(Integer projectTaskId);

    int deleteByProjectTaskId(Integer projectTaskId);

    int updateStatusToSuspend(ProjectTaskBatch projectTaskBatch);
}
