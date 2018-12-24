package com.mryx.matrix.project.service;

import com.mryx.matrix.common.domain.ResultVo;
import com.mryx.matrix.project.domain.Issue;
import com.mryx.matrix.project.domain.JiraComponent;
import com.mryx.matrix.project.domain.JiraUser;
import com.mryx.matrix.project.vo.IssueVo;
import com.mryx.matrix.project.vo.Pagination;
import com.mryx.matrix.project.vo.TransitionVo;
import net.rcarz.jiraclient.JiraClient;

import java.util.List;

/**
 * @author pengcheng
 * @description
 * @email pengcheng@missfresh.cn
 * @date 2018-11-13 18:20
 **/
public interface JiraService {

    List<JiraUser> listUser(JiraUser jiraUser);

    List<JiraComponent> listComponents(JiraUser jiraUser);

    ResultVo transition(TransitionVo transitionVo);

    JiraClient getJiraClient();

    /**
     * 从JIRA获取需求列表
     *
     * @param issue
     * @return
     */
    Pagination<Issue> listIssue(IssueVo issue);
}
