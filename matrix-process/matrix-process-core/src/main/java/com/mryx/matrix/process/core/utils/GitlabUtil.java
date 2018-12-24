package com.mryx.matrix.process.core.utils;


import com.alibaba.fastjson.JSON;
import org.apache.commons.lang.StringUtils;
import org.gitlab.api.GitlabAPI;
import org.gitlab.api.GitlabAPIException;
import org.gitlab.api.models.GitlabBranch;
import org.gitlab.api.models.GitlabMergeRequest;
import org.gitlab.api.models.GitlabProject;
import org.gitlab.api.models.GitlabTag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/***
 * @Author
 *
 */

public class GitlabUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(GitlabUtil.class);
    //grampus-ci 对应的gitlab token
    private static final String TOKEN = "sdmKsRD5dCKuBBtsBLY4";
    private static final String DOMAIN = "http://git.missfresh.net";
    private static final GitlabAPI GITLAB_API = GitlabAPI.connect(DOMAIN, TOKEN);
    private static final String GIT_FILE_TAIL = ".git";

    public static String createBranch(String gitAddress, String branchName, String ref) {
        GitlabProject gitlabProject = getGitlabProject(gitAddress);
        if (gitlabProject == null) {
            LOGGER.error("生成分支失败");
            return null;
        }
        try {
            GITLAB_API.createBranch(gitlabProject, branchName, ref);
        } catch (GitlabAPIException e) {
            String errMsg = e.getMessage();
            try {
                Map<String, String> map = JSON.toJavaObject(JSON.parseObject(errMsg), Map.class);
                if ("Branch already exists".equals(map.get("message"))) {
                    return branchName;
                }
            } catch (Exception e1) {
                LOGGER.error("生成分支失败，IO异常，分支名称 = " + branchName, e);
                return null;
            }
        } catch (IOException e) {
            LOGGER.error("生成分支失败，IO异常，分支名称 = " + branchName, e);
            return null;
        } finally {
            LOGGER.info("gitlabProject={}, branchName={}, ref={}", gitlabProject, branchName, ref);
        }
        return branchName;
    }

    public static void deleteBranch(String gitAddress, String branchName) {
        GitlabProject gitlabProject = getGitlabProject(gitAddress);
        if (gitlabProject == null) {
            LOGGER.error("删除分支失败");
            return;
        }
        try {
            GITLAB_API.deleteBranch(gitlabProject.getId(), branchName);
        } catch (IOException e) {
            LOGGER.error("删除分支失败，IO异常;{}", e);
            return;
        }
    }


    public static String createTag(String gitAddress, String tagName, String ref, String message, String releaseDescription) {
        GitlabProject gitlabProject = getGitlabProject(gitAddress);
        if (gitlabProject == null) {
            LOGGER.error("生成tag失败");
            return null;
        }
        try {
            GITLAB_API.addTag(gitlabProject, tagName, ref, message, releaseDescription);
        } catch (IOException e) {
            LOGGER.error("生成tag失败，IO异常;{}", e);
            return null;
        }
        return tagName;
    }

    public static GitlabMergeRequest createMergeRequest(String gitAddress, String sourceBranch, String targetBranch,
                                                        Integer assigneeId, String title) {
        GitlabProject gitlabProject = getGitlabProject(gitAddress);
        GitlabMergeRequest gitlabMergeRequest = null;
        if (gitlabProject == null) {
            LOGGER.error("生成MergeRequest失败");
            return null;
        }
        try {
            gitlabMergeRequest = GITLAB_API.createMergeRequest(gitlabProject.getId(), sourceBranch, targetBranch, assigneeId, title);
        } catch (IOException e) {
            LOGGER.error("生成MergeRequest失败，IO异常;{}", e);
        }
        return gitlabMergeRequest;
    }

    public static GitlabMergeRequest acceptMergeRequest(String gitAddress, Integer mergeRequestId, String mergeCommitMessage) {
        GitlabProject gitlabProject = getGitlabProject(gitAddress);
        GitlabMergeRequest gitlabMergeRequest = null;
        if (gitlabProject == null) {
            LOGGER.error("accept merge request失败");
            return null;
        }
        try {
            gitlabMergeRequest = GITLAB_API.acceptMergeRequest(gitlabProject, mergeRequestId, mergeCommitMessage);
        } catch (Exception e) {
            LOGGER.error("accept merge request失败，IO异常;{}", e);
        }
        return gitlabMergeRequest;
    }

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
            throw new RuntimeException("getBranch failure, GitlabProject is null! git address is " + gitAddress);
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

    /**
     * 验证代码的tag是否存在
     *
     * @param gitAddress git地址
     * @param tagName    tag名称
     * @return 存在返回true，否则返回false
     */
    public static boolean tagExist(String gitAddress, String tagName, boolean isRollbackTag) {
        GitlabProject gitlabProject = getGitlabProject(gitAddress);
        if (gitlabProject == null) {
            throw new RuntimeException("get git project failure, GitlabProject is null!");
        }

        if (isRollbackTag) {
            if ("master".equals(tagName)) {
                return exist(gitAddress, tagName);
            } else if (!tagName.startsWith("r-")) {
                throw new RuntimeException("回滚必须使用RTag标签，RTag标签以r-开头。");
            }
        }

        try {
            List<GitlabTag> tagList = GITLAB_API.getTags(gitlabProject);
            if (CollectionUtils.isEmpty(tagList)) {
                return false;
            }

            for (GitlabTag tag : tagList) {
                if (tag.getName().equals(tagName)) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            LOGGER.error("get tag failure, address is " + gitAddress + ", tag is " + tagName, e);
        }
        return false;
    }

    public static void main(String[] args) {
        String gitAddress = "git@git.missfresh.cn:matrix/matrix-process.git";
        String tagName = "r-20181126-204830-pengcheng@mryx";
        boolean exist = tagExist(gitAddress, tagName, true);
        System.out.println(exist);
    }
}
