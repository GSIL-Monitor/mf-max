package com.mryx;

import net.rcarz.jiraclient.*;

/**
 * @author pengcheng
 * @description
 * @email pengcheng@missfresh.cn
 * @date 2018-11-07 17:11
 **/
public class MainTest {

    public static void main(String[] args) {
        new MainTest().test();
    }

    private void test() {
        try {
            BasicCredentials creds = new BasicCredentials("liuhong", "1862nian6");
            JiraClient jira = new JiraClient("http://jira.missfresh.net", creds);
//            List<Project> projectList = jira.getProjects();
//            if (CollectionUtils.isNotEmpty(projectList)) {
//                for (Project project : projectList) {
//                    System.out.println("name: " + project.getName());
//                    System.out.println("issue type: " + project.getIssueTypes());
//                    System.out.println("assignee type:" + project.getAssigneeType());
//                    System.out.println("components:" + project.getComponents());
//                    System.out.println("id: " + project.getId());
//                    System.out.println("key: " + project.getKey());
//                    System.out.println("lead: " + project.getLead());
//                    System.out.println();
//                }
//            }

            Project project = jira.getProject("BR");
            System.out.println("name: " + project.getName());
            System.out.println("issue type: " + project.getIssueTypes());
            System.out.println("assignee type:" + project.getAssigneeType());
            System.out.println("components:" + project.getComponents());
            System.out.println("id: " + project.getId());
            System.out.println("key: " + project.getKey());
            User user = project.getLead();
            System.out.println("name: " + user.getName());
            System.out.println("display name: " + user.getDisplayName());
            System.out.println("lead: " + project.getLead());
            System.out.println();

            Issue.SearchResult sr = jira.searchIssues("project IN (BR) ");
//            while (sr.iterator().hasNext())
//                System.out.println("Result: " + sr.iterator().next());
            System.out.println(sr.issues.get(0).getIssueType());
            Issue issue = jira.getIssue("BR-1");
            System.out.println("issue labels: " + issue.getLabels());
            System.out.println("issue description: " + issue.getDescription());
            Object object = issue.getField("description");
            System.out.println(object.getClass());
            System.out.println(issue.getKey());
            System.out.println(issue.getProject());

            jira.createComponent("");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
