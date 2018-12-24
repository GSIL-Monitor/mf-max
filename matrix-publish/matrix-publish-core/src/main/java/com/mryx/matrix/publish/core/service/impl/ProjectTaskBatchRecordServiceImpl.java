package com.mryx.matrix.publish.core.service.impl;

import com.mryx.matrix.publish.core.dao.ProjectTaskBatchRecordDao;
import com.mryx.matrix.publish.core.service.ProjectTaskBatchRecordService;
import com.mryx.matrix.publish.domain.ProjectTaskBatchRecord;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * 发布记录Service Impl
 *
 * @author supeng
 * @date 2018/09/03
 */
@Service("publishRecordService")
public class ProjectTaskBatchRecordServiceImpl implements ProjectTaskBatchRecordService {

    @Resource
    ProjectTaskBatchRecordDao projectTaskBatchRecordDao;

    @Override
    public ProjectTaskBatchRecord getById(Integer id) {
        return projectTaskBatchRecordDao.getById(id);
    }

    @Override
    public ProjectTaskBatchRecord getByBatchId(Integer batchId){
        return  projectTaskBatchRecordDao.getByBatchId(batchId);
    }

    @Override
    public ProjectTaskBatchRecord getFirstBatchByRecord(Integer recordId) {
        return projectTaskBatchRecordDao.getFirstBatchByRecord(recordId);
    }

    @Override
    public int insert(ProjectTaskBatchRecord projectTaskBatchRecord) {
        return projectTaskBatchRecordDao.insert(projectTaskBatchRecord);
    }

    @Override
    public int update(ProjectTaskBatchRecord projectTaskBatchRecord) {
        return projectTaskBatchRecordDao.update(projectTaskBatchRecord);
    }

    @Override
    public int pageTotal(ProjectTaskBatchRecord projectTaskBatchRecord) {
        return projectTaskBatchRecordDao.pageTotal(projectTaskBatchRecord);
    }

    @Override
    public List<ProjectTaskBatchRecord> list(ProjectTaskBatchRecord projectTaskBatchRecord) {
        return projectTaskBatchRecordDao.list(projectTaskBatchRecord);
    }

    @Override
    public List<ProjectTaskBatchRecord> listByCondition(ProjectTaskBatchRecord projectTaskBatchRecord) {
        return projectTaskBatchRecordDao.listByCondition(projectTaskBatchRecord);
    }

    @Override
    public int delete(ProjectTaskBatchRecord projectTaskBatchRecord) {
        return projectTaskBatchRecordDao.delete(projectTaskBatchRecord);
    }

    @Override
    public int batchInsert(List<ProjectTaskBatchRecord> projectTaskBatchRecordList) {
        return projectTaskBatchRecordDao.batchInsert(projectTaskBatchRecordList);
    }

    @Override
    public int batchDelete(List<ProjectTaskBatchRecord> projectTaskBatchRecordList) {
        return projectTaskBatchRecordDao.batchDelete(projectTaskBatchRecordList);
    }

    @Override
    public List<ProjectTaskBatchRecord> getByRecordId(Integer recordId) {
        return projectTaskBatchRecordDao.getByRecordId(recordId);
    }

    @Override
    public int updateStatusToSuspend(ProjectTaskBatchRecord projectTaskBatchRecord) {
        return projectTaskBatchRecordDao.updateStatusToSuspend(projectTaskBatchRecord);
    }
}
