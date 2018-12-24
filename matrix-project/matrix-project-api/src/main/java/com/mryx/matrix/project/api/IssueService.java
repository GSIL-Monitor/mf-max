package com.mryx.matrix.project.api;

import com.mryx.matrix.common.domain.ResultVo;
import com.mryx.matrix.project.domain.Issue;
import com.mryx.matrix.project.domain.Project;

/**
 * IssueService
 *
 * @author supeng
 * @date 2018/11/11
 */
public interface IssueService {

    ResultVo createIssue(Issue issue);

    ResultVo updateIssue(Issue issue);

    ResultVo listIssue(Issue issue);

    /**
     * 关联matrix任务与Jira问题
     *
     * @return
     */
    ResultVo associate(Project project);
}
