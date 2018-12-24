package com.mryx.matrix.project.controller;

import com.mryx.common.utils.HttpPoolClient;
import com.mryx.matrix.common.domain.ResultVo;
import com.mryx.matrix.project.domain.Issue;
import com.mryx.matrix.project.domain.JiraComponent;
import com.mryx.matrix.project.domain.JiraUser;
import com.mryx.matrix.project.service.JiraService;
import com.mryx.matrix.project.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

/**
 * @author pengcheng
 * @description
 * @email pengcheng@missfresh.cn
 * @date 2018-11-14 13:51
 **/
@Slf4j
@RestController
@RequestMapping(value = "/api/jira")
public class JiraController {

    private static final Logger logger = LoggerFactory.getLogger(JiraController.class);

    private final HttpPoolClient HTTP_POOL_CLIENT = com.mryx.common.utils.HttpClientUtil.create(2000, 2000, 5, 5, 1, 500);

    @Value("${dealAccesstocken_url}")
    private String dealAccesstockenUrl;

    @Autowired
    JiraService jiraService;

    @PostMapping(value = "/listUsers", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResultVo listUsers(HttpServletRequest request, @RequestBody JiraUser jiraUser) {
        try {
            List<JiraUser> jiraUserList = jiraService.listUser(jiraUser);
            JiraUserListVo data = new JiraUserListVo();
            List<JiraUserVo> jiraUserVoList = new ArrayList<>(jiraUserList.size());
            jiraUserList.stream().forEach(jiraUser1 -> {
                JiraUserVo vo = new JiraUserVo();
                BeanUtils.copyProperties(jiraUser1, vo);
                jiraUserVoList.add(vo);
            });
            data.setUserList(jiraUserVoList);
            return ResultVo.Builder.SUCC().initSuccData(data);
        } catch (Exception e) {
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("4000", "获取用户错误：" + e.getMessage());
        }
    }

    @PostMapping(value = "/listComponents", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResultVo listComponents(HttpServletRequest request, @RequestBody JiraUser jiraUser) {
        try {
            List<JiraComponent> jiraUserList = jiraService.listComponents(jiraUser);
            return ResultVo.Builder.SUCC().initSuccData(jiraUserList);
        } catch (Exception e) {
            logger.error("获取组件错误", e);
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("4000", "获取组件错误：" + e.getMessage());
        }
    }

    @PostMapping(value = "/transition", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResultVo transition(@RequestBody TransitionVo transitionVo) {
        try {
            String id = transitionVo.getId();
            // 只做已提测流转
            if (StringUtils.isEmpty(id)) {
                transitionVo.setId("71");
            }
            return jiraService.transition(transitionVo);
        } catch (Exception e) {
            logger.error("流转失败", e);
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("4000", "流转失败：" + e.getMessage());
        }
    }

    @PostMapping(value = "/listIssues", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResultVo listIssues(@RequestBody IssueVo issue) {
        try {
            String jiraProjectId = issue.getJiraProjectId();
            Pagination<Issue> issueList = jiraService.listIssue(issue);
            return ResultVo.Builder.SUCC().initSuccData(issueList);
        } catch (Exception e) {
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("4000", "获取需求错误：" + e.getMessage());
        }
    }

}
