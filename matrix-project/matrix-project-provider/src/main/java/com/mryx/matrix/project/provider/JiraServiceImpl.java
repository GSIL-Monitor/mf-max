package com.mryx.matrix.project.provider;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.mryx.matrix.common.domain.ResultVo;
import com.mryx.matrix.project.dao.IssueDao;
import com.mryx.matrix.project.domain.JiraComponent;
import com.mryx.matrix.project.domain.JiraUser;
import com.mryx.matrix.project.enums.FieldEnum;
import com.mryx.matrix.project.enums.ProjectTypeEnum;
import com.mryx.matrix.project.service.JiraService;
import com.mryx.matrix.project.vo.IssuePickerResult;
import com.mryx.matrix.project.vo.IssueVo;
import com.mryx.matrix.project.vo.Pagination;
import com.mryx.matrix.project.vo.TransitionVo;
import net.rcarz.jiraclient.*;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

import static com.mryx.matrix.project.enums.IssueTypeEnum.*;

/**
 * @author pengcheng
 * @description
 * @email pengcheng@missfresh.cn
 * @date 2018-11-13 18:22
 **/
@Service
public class JiraServiceImpl implements JiraService {

    private static Logger logger = LoggerFactory.getLogger(JiraServiceImpl.class);

    private static final String PATH_USER_SEARCH = "/rest/api/2/user/search";

    private static final String PATH_ISSUE_PICKER = "/rest/api/2/issue/picker";

    private static final String PATH_REMOTE_LINK = "";

    @Resource
    private IssueDao issueDao;

    /**
     * JIRA用户名称
     */
    @Value("${jira_username}")
    private String jiraUserName = "粉熊机器人";

    /**
     * JIRA用户密码
     */
    @Value("${jira_password}")
    private String jiraPassword;

    /**
     * JIRA地址
     */
    @Value("${jira_uri}")
    private String jiraUri;

    @Override
    public JiraClient getJiraClient() {
        BasicCredentials credentials = new BasicCredentials(jiraUserName, jiraPassword);
        return new JiraClient(jiraUri, credentials);
    }

    @Override
    public Pagination<com.mryx.matrix.project.domain.Issue> listIssue(IssueVo issue) {
        Pagination pagination = new Pagination();
        pagination.setPageNo(issue.getPageNo());
        pagination.setPageSize(issue.getPageSize());
        try {
            String jql = "";
            String query = issue.getQuery();
            String projectType = issue.getProjectType();
            if (ProjectTypeEnum.PR.getCode().toString().equals(projectType) || ProjectTypeEnum.TR.getCode().toString().equals(projectType)) {
                jql += "( issuetype = " + REQUIREMENT.getCode() + " OR issuetype = " + STORY.getCode() + " )";
            } else if (ProjectTypeEnum.OB.getCode().toString().equals(projectType)) {
                jql += "issuetype = " + BUG.getCode();
            }
//            if (!StringUtils.isEmpty(query)) {
//                if (!StringUtils.isEmpty(jql)) {
//                    jql += " AND ";
//                }
//                jql += " (summary ~ " + query + " OR text ~ " + query + ")";
//            }

//            JiraClient jira = getJiraClient();
//            net.rcarz.jiraclient.Issue.SearchResult result = jira.searchIssues(jql);
            List<com.mryx.matrix.project.domain.Issue> list = issuePicker(jql, query);
//            if (!CollectionUtils.isEmpty(result.issues)) {
//                for (net.rcarz.jiraclient.Issue issueResult : result.issues) {
//                    com.mryx.matrix.project.domain.Issue issue1 = new com.mryx.matrix.project.domain.Issue();
//                    User assignee = issueResult.getAssignee();
//                    if (assignee != null) {
//                        issue1.setAssignee(assignee.getDisplayName());
//                    }
//                    issue1.setId(0);
//                    issue1.setJiraIssueId(issueResult.getId());
//                    issue1.setKey(issueResult.getKey());
//                    issue1.setJiraProjectId(issueResult.getProject().getId());
//                    issue1.setSummary(issueResult.getSummary());
//                    issue1.setDescription(issueResult.getDescription());
//                    issue1.setGmtCreated(ProjectUtils.string2Date((String) issueResult.getField("created")));
//                    issue1.setGmtModified(ProjectUtils.string2Date((String) issueResult.getField("update")));
//                    issue1.setIssueStatus(issueResult.getStatus().getName());
//                    Priority priority = issueResult.getPriority();
//                    if (priority != null) {
//                        issue1.setPriority(Integer.parseInt(priority.getId()));
//                    }
//                    issue1.setReporter(issueResult.getReporter().getName());
//                    list.add(issue1);
//                }
//            }
            if (list != null && list.size() > 0) {
                pagination.setStartNo(issue.getStartOfPage());
                pagination.setDataList(list);
                pagination.setTotalPage(1);
                pagination.setTotalSize(list.size());
            }
            return pagination;
        } catch (Exception e) {
            logger.error("listIssue error ", e);
            return pagination;
        }
    }

    @Override
    public List<JiraUser> listUser(JiraUser jiraUser) {
        JiraClient jiraClient = getJiraClient();
        RestClient restClient = jiraClient.getRestClient();

        Map<String, String> map = new HashMap();
        String username = jiraUser.getName();
        map.put("username", username);
        map.put("maxResults", "1000");

        try {
            int count = 1000;
            List<JiraUser> ret = new ArrayList<>();
            for (int startAt = 0; count == 1000; startAt += 1000) {
                map.put("startAt", String.valueOf(startAt));
                URI uri = restClient.buildURI(PATH_USER_SEARCH, map);
                Object json = restClient.get(uri);
                List<JiraUser> userList = JSON.parseArray(json.toString(), JiraUser.class);
                count = userList.size();
                ret.addAll(userList);
            }
            return ret;
        } catch (IOException e) {
            logger.error("an error reading the response occurs", e);
            throw new RuntimeException("an error reading the response occurs", e);
        } catch (RestException e) {
            logger.error("an HTTP-level error occurs", e);
            throw new RuntimeException("an HTTP-level error occurs", e);
        } catch (URISyntaxException e) {
            logger.error("the path is invalid", e);
            throw new RuntimeException("the path is invalid", e);
        }
    }

    @Override
    public List<JiraComponent> listComponents(JiraUser jiraUser) {
        JiraClient jiraClient = getJiraClient();
        try {
            Project project = jiraClient.getProject("BR");
            List<Component> components = project.getComponents();
            List<JiraComponent> jiraComponentList = new ArrayList<>();
            if (!CollectionUtils.isEmpty(components)) {
                components.stream().forEach(component -> {
                    JiraComponent jiraComponent = new JiraComponent();
                    BeanUtils.copyProperties(component, jiraComponent);
                    jiraComponentList.add(jiraComponent);
                });
            }
            return jiraComponentList;
        } catch (JiraException e) {
            logger.error("failed to obtain the project");
            throw new RuntimeException("failed to obtain the project", e);
        }
    }

    @Override
    public ResultVo transition(TransitionVo transitionVo) {
        try {
            JiraClient jiraClient = getJiraClient();
            RestClient restClient = jiraClient.getRestClient();
            StringBuilder builder = new StringBuilder();
            Integer projectId = transitionVo.getProjectId();
            com.mryx.matrix.project.domain.Issue condition = new com.mryx.matrix.project.domain.Issue();
            condition.setProjectId(projectId);
            condition.setDelFlag(1);
            List<com.mryx.matrix.project.domain.Issue> issueList = issueDao.listIssue(condition);
            List<Issue> jiraIssueList = new ArrayList<>();
            if (!CollectionUtils.isEmpty(issueList)) {
                String review = transitionVo.getReviewer();
                String techDocument = transitionVo.getTechDocument();
                for (com.mryx.matrix.project.domain.Issue matrix : issueList) {
                    String key = matrix.getJiraIssueId();
                    Issue jiraIssue = jiraClient.getIssue(key);
                    jiraIssueList.add(jiraIssue);
//                    Status status = jiraIssue.getStatus();
//                    if (!"开发中".equals(status.getName())) {
//                        if (builder.length() > 0) {
//                            builder.append("\r");
//                        }
//                        builder.append("需求 ").append(jiraIssue.getKey()).append(" 状态处于 ").append(status.getName()).append(" 中，");
//                        builder.append("无法流转状态为已提测。");
//                        continue;
//                    }

                    try {
                        Map<String, String> object = new HashMap<>();
                        object.put("title", "matrix project " + projectId);
                        object.put("url", "http://max.missfresh.net/project/progress?id=" + projectId);
                        String path = "/rest/api/2/issue/" + jiraIssue.getKey() + "/remotelink";
                        JSONObject string = JSONObject.parseObject(JSON.toJSONString(object));
                        JSONObject m = new JSONObject();
                        m.put("object", string);
                        URI uri = restClient.buildURI(path);
                        String p = JSON.toJSONString(m);
                        issueLinks(uri.toURL().toString(), p);
                        Issue.FluentTransition fluentTransition = jiraIssue.transition();
                        // Review负责人
                        fluentTransition.field(FieldEnum.ReviewUser.getCode(), review);
                        // 技术文档
                        fluentTransition.field(FieldEnum.TechDocument.getCode(), techDocument);
                        //
                        //            // 计划评审完成时间
                        //            fluentTransition.field("customfield_10112", "2019-01-01");
                        //            // 计划排期时间
                        //            fluentTransition.field("customfield_10114", "2019-01-01");
                        //            // 计划提测时间
                        //            fluentTransition.field("customfield_10116", "2019-01-01");
                        //            // 计划上线时间
                        //            fluentTransition.field("customfield_10118", "2019-01-01");
                        //

                        // 经办人
                        fluentTransition.field(Field.ASSIGNEE, transitionVo.getAssignee());

                        // 已提测
                        fluentTransition.execute(Integer.parseInt(transitionVo.getId()));

                        if (!StringUtils.isEmpty(transitionVo.getComment())) {
                            jiraIssue.addComment(transitionVo.getComment());
                        }
                    } catch (Exception e) {
                        logger.error("流转需求状态失败", e);
                    }
                }
            }
            if (builder.length() > 0) {
                return ResultVo.Builder.FAIL().initErrCodeAndMsg("2000", builder.toString());
            }
            return ResultVo.Builder.SUCC();
        } catch (JiraException e) {
            logger.error("流转需求状态失败", e);
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("1000", "流转状态失败: " + e.getMessage());
        }
    }

    private void issueLinks(String uri, String content) {
        HttpClient client = HttpClientBuilder.create().build();
        HttpResponse response = null;
        try {
            HttpPost post = new HttpPost(uri);
            authenticate(post);
            StringEntity entity = new StringEntity(content);
            post.setEntity(entity);
            post.addHeader("content-type", "application/json");
            entity.setContentType("application/json");
            entity.setContentEncoding("utf-8");
            response = client.execute(post);
        } catch (IOException e) {
            logger.error("", e);
        } catch (AuthenticationException e) {
            logger.error("", e);
        }
        int statusCode = response.getStatusLine().getStatusCode();
        logger.info("issue link status: " + statusCode);
    }

    private void authenticate(HttpPost req) throws AuthenticationException {
        Credentials creds = new UsernamePasswordCredentials(jiraUserName, jiraPassword);
        req.addHeader(new BasicScheme().authenticate(creds, req, null));
    }

    private List<com.mryx.matrix.project.domain.Issue> issuePicker(String jql, String query) {
        JiraClient jiraClient = getJiraClient();
        RestClient restClient = jiraClient.getRestClient();

        Map<String, String> map = new HashMap<>();
        map.put("currentJQL", jql);
        map.put("query", query);

        List<com.mryx.matrix.project.domain.Issue> list = new ArrayList<>();
        Set<String> keys = new HashSet<>();
        try {
            URI uri = restClient.buildURI(PATH_ISSUE_PICKER, map);
            net.sf.json.JSON json = restClient.get(uri);
            JSONObject jsonObject = JSONObject.parseObject(json.toString());
            JSONArray array = jsonObject.getJSONArray("sections");

            for (Object object : array) {
                JSONObject section = (JSONObject) object;
                if ("hs".equals(section.get("id"))) {
                    continue;
                }
                List<IssuePickerResult> result = JSONArray.parseArray(section.getJSONArray("issues").toJSONString(), IssuePickerResult.class);
                if (!CollectionUtils.isEmpty(result)) {
                    for (IssuePickerResult item : result) {
                        String key = item.getKey();
                        if (keys.add(key)) {
                            com.mryx.matrix.project.domain.Issue issue = new com.mryx.matrix.project.domain.Issue();
                            issue.setKey(key);
                            issue.setSummary(item.getSummaryText());
                            list.add(issue);
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("查询Issue失败", e);
        }
        return list;
    }
}
