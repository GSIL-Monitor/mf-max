package com.mryx.matrix.codeanalyzer.utils;

import org.apache.commons.lang.StringUtils;
import org.gitlab.api.GitlabAPI;
import org.gitlab.api.models.GitlabBranch;
import org.gitlab.api.models.GitlabProject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class GitlabUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(GitlabUtil.class);
    private static final String TOKEN = "sdmKsRD5dCKuBBtsBLY4";
    private static final String DOMAIN = "http://git.missfresh.net";
    private static final GitlabAPI GITLAB_API = GitlabAPI.connect(DOMAIN, TOKEN);
    private static final String GIT_FILE_TAIL = ".git";

    /**
     * 查看指定的分支是否存在
     *
     * @param gitAddress git地址
     * @param branchName 分支名称
     * @return
     */
    public static boolean exist(String gitAddress, String branchName) {
        GitlabProject gitlabProject = getGitlabProject(gitAddress);
        if (gitlabProject == null) {
            throw new RuntimeException("getBranch failure, GitlabProject is null!");
        }

        try {
            GitlabBranch branch = GITLAB_API.getBranch(gitlabProject, branchName);
            return branch != null;
        } catch (Exception e) {
            LOGGER.error("getBranch failure, address is " + gitAddress + ", branch is " + branchName, e);
        }

        return false;
    }

    private static GitlabProject getGitlabProject(String gitAddress) {
        if (StringUtils.isEmpty(gitAddress) || !gitAddress.endsWith(GIT_FILE_TAIL)) {
            LOGGER.warn("git地址不合法，{}", gitAddress);
            return null;
        }
        LOGGER.info("git地址是{}", gitAddress);
        String[] strings = StringUtils.split(gitAddress, ":");
        String namespaceProject = strings[strings.length - 1].replace(".git", "");
        String[] results = StringUtils.split(namespaceProject, "/");
        String namespace = results[0];
        String project = results[1];
        GitlabProject gitlabProject = new GitlabProject();

        try {
            gitlabProject = GITLAB_API.getProject(namespace, project);
            return gitlabProject;
        } catch (IOException e) {
            LOGGER.error("获取git工程失败，git地址：{}", gitAddress, e);
        }
        return gitlabProject;
    }
}