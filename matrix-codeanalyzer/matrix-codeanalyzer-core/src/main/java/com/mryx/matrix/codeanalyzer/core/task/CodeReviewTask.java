package com.mryx.matrix.codeanalyzer.core.task;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.mryx.common.utils.HttpClientUtil;
import com.mryx.common.utils.HttpPoolClient;
import com.mryx.matrix.codeanalyzer.core.service.CodeReviewResultService;
import com.mryx.matrix.codeanalyzer.core.service.CodeService;
import com.mryx.matrix.codeanalyzer.core.utils.BranchAppCodeUtil;
import com.mryx.matrix.codeanalyzer.domain.CodeReviewResult;
import com.mryx.matrix.codeanalyzer.utils.ChineseCharacterUtil;
import com.mryx.matrix.publish.domain.ProjectTask;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

/**
 * Code Review任务
 *
 * @author supeng
 * @date 2018/09/30
 */
@Slf4j
public class CodeReviewTask implements Runnable {

    private static final HttpPoolClient HTTP_POOL_CLIENT = HttpClientUtil.create(2000, 2000, 5, 5, 1, 500);

    private CodeService codeService;
    private ProjectTask projectTask;
    private CodeReviewResultService codeReviewResultService;
    private String appProjectTasksRemote;
    private String appDetailRemote;
    private String codeReviewCreateRemote;

    public CodeReviewTask(final CodeService codeService, final ProjectTask projectTask, final CodeReviewResultService codeReviewResultService, final String appProjectTasksRemote, final String appDetailRemote, final String codeReviewCreateRemote) {
        this.codeService = codeService;
        this.projectTask = projectTask;
        this.codeReviewResultService = codeReviewResultService;
        this.appProjectTasksRemote = appProjectTasksRemote;
        this.appDetailRemote = appDetailRemote;
        this.codeReviewCreateRemote = codeReviewCreateRemote;
    }

    @Override
    public void run() {
        String appBranch = "";
        String appCode = "";
        Map<String, String> map = BranchAppCodeUtil.getBranchAndAppCode(projectTask, appProjectTasksRemote);
        if (!map.isEmpty()) {
            appBranch = map.get("branch");
            appCode = map.get("appCode");
        }
        log.info("branch is " + appBranch + " appCode is " + appCode);
        try {
            String gitAddress = codeService.getGitAddress(appDetailRemote, appCode).getData();
            log.info("gitAddress is {}" + gitAddress);
            CodeReviewResult codeReviewResult = new CodeReviewResult();
            log.info("" + projectTask.getProjectId());
            String devOwner = codeReviewResultService.getCodeReviewPeople(projectTask.getProjectId());
            log.info("评审人为:" + devOwner);
            //将中文转换成拼音
            List<String> codeReviewPerson = Arrays.asList(devOwner.split(","));
            List<String> codeReviewPersonPinyin = new ArrayList<>();
            for (String person : codeReviewPerson) {
                String personPin = ChineseCharacterUtil.getLowerCase(person, true);
                log.info("转换后的评审人为:{}", personPin);
                codeReviewPersonPinyin.add(personPin);
            }
            codeReviewResult.setProjectTaskId(projectTask.getId());
            codeReviewResult.setCodeBranch(appBranch);
            codeReviewResult.setCodeReviewPerson(devOwner);
            codeReviewResult.setCodeReviewResult("0");
            Map<String, Object> codeReviewCreateParameters = codeReviewResultService.setCodeReviewCreateParameters(gitAddress, appBranch, projectTask.getId().toString(), codeReviewPersonPinyin);
            try {
                Optional reviewResult = codeReviewResultService.getHTTPRequest(codeReviewCreateRemote, codeReviewCreateParameters);
                log.info("调用创建代码评审接口的返回值为: " + reviewResult.toString());
                if (reviewResult.isPresent()) {
                    JSONObject result = JSONObject.parseObject(reviewResult.get().toString());
                    if ("0".equals(result.get("code").toString())) {
                        if (result.get("data") != null) {
                            JSONObject data = JSONObject.parseObject(result.get("data").toString());
                            codeReviewResult.setLinkURL(data.getString("url"));
                        } else {
                            codeReviewResult.setLinkURL("创建评审失败！");
                        }
                    } else if ("1".equals(result.get("code").toString())) {
                        if (result.get("message").toString().contains("No changes found")) {
                            codeReviewResult.setLinkURL("代码没有改动，拒绝创建评审！");
                            log.warn("代码没有改动，拒绝创建评审！");
                        } else if (result.get("message").toString().contains("Reviewers")) {
                            for (String person : codeReviewPerson) {
                                String personPin = ChineseCharacterUtil.getLowerCase(person, false);
                                log.info("转换后的评审人为:{}", personPin);
                                codeReviewPersonPinyin.clear();
                                codeReviewPersonPinyin.add(personPin);
                            }
                            codeReviewCreateParameters = codeReviewResultService.setCodeReviewCreateParameters(gitAddress, appBranch, projectTask.getId().toString(), codeReviewPersonPinyin);
                            try {
                                Optional reviewResult2 = codeReviewResultService.getHTTPRequest(codeReviewCreateRemote, codeReviewCreateParameters);
                                log.info("调用创建代码评审接口的返回值为: " + reviewResult.toString());
                                if (reviewResult.isPresent()) {
                                    JSONObject result2 = JSONObject.parseObject(reviewResult.get().toString());
                                    if ("0".equals(result.get("code").toString())) {
                                        if (result.get("data") != null) {
                                            JSONObject data = JSONObject.parseObject(result.get("data").toString());
                                            codeReviewResult.setLinkURL(data.getString("url"));
                                        } else {
                                            codeReviewResult.setLinkURL("创建评审失败！");
                                        }
                                    } else if ("1".equals(result.get("code").toString())) {
                                        if (result.get("message").toString().contains("No changes found")) {
                                            codeReviewResult.setLinkURL("代码没有改动，拒绝创建评审！");
                                            log.warn("代码没有改动，拒绝创建评审！");
                                        } else if (result.get("message").toString().contains("Reviewers")) {
                                            codeReviewResult.setLinkURL("创建评审失败！代码评审人不存在！！！");
                                        }
                                    }
                                } else {
                                    codeReviewResult.setLinkURL("创建评审失败！");
                                }
                            } catch (Exception e) {
                                log.error("CodeReviewCreateRemote error ", e);
                                codeReviewResult.setLinkURL("创建评审失败！");
                            }
                        }
                    }
                } else {
                    codeReviewResult.setLinkURL("创建评审失败！");
                }
            } catch (Exception e) {
                log.error("CodeReviewCreateRemote error ", e);
                codeReviewResult.setLinkURL("创建评审失败！");
            }
            int insertResult = codeReviewResultService.insertCodeReviewResult(codeReviewResult);
            log.info("insertResult {}", insertResult);
        } catch (Exception e) {
            log.error("appDetailRemote error", e);
        }
    }
}