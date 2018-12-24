package com.mryx.matrix.project.provider;

import com.mryx.matrix.common.domain.ResultVo;
import com.mryx.matrix.project.api.IssueService;
import com.mryx.matrix.project.dao.IssueDao;
import com.mryx.matrix.project.domain.Issue;
import com.mryx.matrix.project.domain.Project;
import com.mryx.matrix.project.service.JiraService;
import lombok.extern.slf4j.Slf4j;
import net.rcarz.jiraclient.Field;
import net.rcarz.jiraclient.JiraClient;
import net.rcarz.jiraclient.JiraException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * IssueService Impl
 *
 * @author supeng
 * @date 2018/11/11
 */
@Slf4j
@Service
public class IssueServiceImpl implements IssueService {

    @Resource
    private IssueDao issueDao;

    @Autowired
    private JiraService jiraService;

    @Override
    public ResultVo createIssue(Issue issue) {
        try {
            String jiraProjectId = issue.getJiraProjectId();
            if (StringUtils.isEmpty(jiraProjectId) || "0".equals(jiraProjectId)) {
                throw new RuntimeException("project id is empty!");
            }

            JiraClient jira = jiraService.getJiraClient();

            net.rcarz.jiraclient.Issue.FluentCreate fluentCreate = jira.createIssue("TEST", "Bug");
            fluentCreate.field(Field.SUMMARY, "Bat signal is broken");
            fluentCreate.field(Field.DESCRIPTION, "Commissioner Gordon reports the Bat signal is broken.");
            fluentCreate.field(Field.REPORTER, "batman");
            fluentCreate.field(Field.ASSIGNEE, "robin");
            fluentCreate.execute();

            return ResultVo.Builder.SUCC();
        } catch (Exception e) {
            log.error("createIssue error ", e);
        }
        return ResultVo.Builder.FAIL().initErrCodeAndMsg("1", "创建失败");
    }

    @Override
    public ResultVo updateIssue(Issue issue) {
        try {
            return ResultVo.Builder.SUCC();
        } catch (Exception e) {
            log.error("updateIssue error ", e);
        }
        return ResultVo.Builder.FAIL().initErrCodeAndMsg("1", "更新失败");
    }

    @Override
    public ResultVo listIssue(Issue issue) {
        try {
            issue.setDelFlag(1);
            List<Issue> list = issueDao.listIssue(issue);
            if (!CollectionUtils.isEmpty(list)) {
                list.stream().forEach(issue1 -> {
                    String jiraIssueId = issue1.getJiraIssueId();
                    try {
                        net.rcarz.jiraclient.Issue jiraIssue = jiraService.getJiraClient().getIssue(jiraIssueId);
                        issue1.setSummary(jiraIssue.getSummary());
                        issue1.setKey(jiraIssue.getKey());
                    } catch (JiraException e) {
                        log.error("获取JIRA需求失败，需求ID：" + jiraIssueId, e);
                    }
                });
                return ResultVo.Builder.SUCC().initSuccData(list);
            }
        } catch (Exception e) {
            log.error("listIssue error ", e);
        }
        return ResultVo.Builder.SUCC().initSuccData(new ArrayList<>());
    }

    @Override
    public ResultVo associate(Project project) {
        Integer projectId = project.getId();
        Issue condition = new Issue();
        condition.setProjectId(projectId);
        condition.setDelFlag(1);
        List<Issue> oldIssueList = issueDao.listIssue(condition);
        List<Issue> newIssueList = project.getIssueList();
        Map<String, Issue> issueSet = new HashMap<>();
        if (!CollectionUtils.isEmpty(newIssueList)) {
            newIssueList.stream().forEach(issue -> {
                String key = issue.getKey();
                try {
                    net.rcarz.jiraclient.Issue jiraIssue = jiraService.getJiraClient().getIssue(key);
                    issue.setSummary(jiraIssue.getSummary());
                    issue.setJiraIssueId(jiraIssue.getId());
                    issue.setJiraProjectId(jiraIssue.getProject().getId());
                    issue.setIssueStatus(jiraIssue.getStatus().getName());
                    issue.setIssueType(jiraIssue.getIssueType().getName());
                } catch (JiraException e) {
                    log.error("获取JIRA需求失败，需求KEY：" + key, e);
                }
                issueSet.put(issue.getJiraIssueId(), issue);
            });
        }
        if (!CollectionUtils.isEmpty(oldIssueList)) {
            for (Issue oldIssue : oldIssueList) {
                String jiraIssueId = oldIssue.getJiraIssueId();
                Issue newIssue = issueSet.remove(jiraIssueId);
                if (newIssue == null) {
                    oldIssue.setDelFlag(0);
                }
            }
        }
        for (Map.Entry<String, Issue> entry : issueSet.entrySet()) {
            Issue issue = entry.getValue();
            issue.setProjectId(projectId);
            issue.setId(null);
            issue.setDelFlag(1);
            oldIssueList.add(issue);
        }
        if (!CollectionUtils.isEmpty(oldIssueList)) {
            issueDao.batchUpdateOrInsert(oldIssueList);
        }
        return ResultVo.Builder.SUCC();
    }
}
