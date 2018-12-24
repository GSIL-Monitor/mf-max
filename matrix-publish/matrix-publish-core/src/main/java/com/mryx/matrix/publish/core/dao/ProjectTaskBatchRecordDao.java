package com.mryx.matrix.publish.core.dao;

import com.mryx.matrix.publish.domain.ProjectTaskBatchRecord;

import java.util.List;

/**
 * 发布记录Dao
 *
 * @author supeng
 * @date 2018/09/03
 */
public interface ProjectTaskBatchRecordDao {

    ProjectTaskBatchRecord getById(Integer id);

    ProjectTaskBatchRecord getByBatchId(Integer batchId);

    ProjectTaskBatchRecord getFirstBatchByRecord(Integer recordId);

    int insert(ProjectTaskBatchRecord projectTaskBatchRecord);

    int update(ProjectTaskBatchRecord projectTaskBatchRecord);

    int pageTotal(ProjectTaskBatchRecord projectTaskBatchRecord);

    List<ProjectTaskBatchRecord> list(ProjectTaskBatchRecord projectTaskBatchRecord);

    List<ProjectTaskBatchRecord> listByCondition(ProjectTaskBatchRecord projectTaskBatchRecord);

    int delete(ProjectTaskBatchRecord projectTaskBatchRecord);

    int batchInsert(List<ProjectTaskBatchRecord> projectTaskBatchRecordList);

    int batchDelete(List<ProjectTaskBatchRecord> projectTaskBatchRecordList);

    List<ProjectTaskBatchRecord> getByRecordId(Integer recordId);

    int updateStatusToSuspend(ProjectTaskBatchRecord projectTaskBatchRecord);

}
