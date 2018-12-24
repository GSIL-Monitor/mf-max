package com.mryx.matrix.publish.core.dao;

import java.util.List;

import com.mryx.matrix.publish.domain.ReleaseDelpoyRecord;


/**
 * 生产环境部署记录表
 *
 * @author zhaoxl
 * @email zhaoxl02@missfresh.cn
 * @date 2018-09-07 15:55
 **/
public interface ReleaseDelpoyRecordDao {

    ReleaseDelpoyRecord getById(Integer id);

    ReleaseDelpoyRecord getByRecordId(Integer recordId);

    ReleaseDelpoyRecord getByProjectId(Integer projectId);

    int insert(ReleaseDelpoyRecord releaseDelpoyRecord);

    int updateById(ReleaseDelpoyRecord releaseDelpoyRecord);

    int pageTotal(ReleaseDelpoyRecord releaseDelpoyRecord);

    List<ReleaseDelpoyRecord> listPage(ReleaseDelpoyRecord releaseDelpoyRecord);

    List<ReleaseDelpoyRecord> listByCondition(ReleaseDelpoyRecord releaseDelpoyRecord);

    int batchInsert(List<ReleaseDelpoyRecord> releaseDelpoyRecordList);

    int deleteById(ReleaseDelpoyRecord releaseDelpoyRecord);

    int batchDelete(List<ReleaseDelpoyRecord> releaseDelpoyRecordList);

    List<ReleaseDelpoyRecord> listByCodeStatus(String appCode);

    ReleaseDelpoyRecord getByLastId(Integer projectTaskId);

    List<ReleaseDelpoyRecord> listByProjectStatus(ReleaseDelpoyRecord releaseDelpoyRecord);
}
