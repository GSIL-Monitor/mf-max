package com.mryx.matrix.project.dao;

import com.mryx.matrix.project.domain.Issue;

import java.util.List;

/**
 * IssueDao
 *
 * @author supeng
 * @date 2018/11/11
 */
public interface IssueDao {

    Integer createIssue(Issue issue);

    Integer updateIssue(Issue issue);

    List<Issue> listIssue(Issue issue);

    int batchUpdateOrInsert(List<Issue> appServerList);

}
