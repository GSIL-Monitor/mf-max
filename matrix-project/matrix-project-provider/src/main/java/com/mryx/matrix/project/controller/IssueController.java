package com.mryx.matrix.project.controller;

import com.mryx.matrix.common.domain.ResultVo;
import com.mryx.matrix.project.api.IssueService;
import com.mryx.matrix.project.domain.Issue;
import com.mryx.matrix.project.domain.Project;
import com.mryx.matrix.project.service.JiraService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Issue Controller
 *
 * @author supeng
 * @date 2018/11/12
 */
@Slf4j
@RestController
@RequestMapping(value = "/api/jira/issue")
public class IssueController {

    @Autowired
    private IssueService issueService;

    private JiraService jiraService;

    @PostMapping(value = "/createIssue", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResultVo createIssue(@RequestBody Issue issue) {
        return issueService.createIssue(issue);
    }

    @PostMapping(value = "/updateIssue", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResultVo updateIssue(@RequestBody Issue issue) {
        return issueService.updateIssue(issue);
    }

    @PostMapping(value = "/listIssue", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResultVo listIssue(@RequestBody Issue issue) {
        return issueService.listIssue(issue);
    }

    @PostMapping(value = "/associate", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResultVo associate(@RequestBody Project project) {
        return issueService.associate(project);
    }
}
