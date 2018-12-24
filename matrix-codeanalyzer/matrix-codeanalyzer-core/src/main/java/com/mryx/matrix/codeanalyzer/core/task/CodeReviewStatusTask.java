package com.mryx.matrix.codeanalyzer.core.task;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.mryx.common.utils.HttpClientUtil;
import com.mryx.common.utils.HttpPoolClient;
import com.mryx.matrix.codeanalyzer.core.service.CodeReviewResultService;
import com.mryx.matrix.codeanalyzer.core.service.CodeService;
import com.mryx.matrix.codeanalyzer.core.utils.BranchAppCodeUtil;
import com.mryx.matrix.codeanalyzer.domain.CodeReviewResult;
import com.mryx.matrix.publish.domain.ProjectTask;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Optional;

/**
 * @program: matrix-codeanalyzer
 * @description: Code Review Status任务
 * @author: jianghong
 * @create: 2018-09-30 11:59
 */

@Slf4j
public class CodeReviewStatusTask implements Runnable {

    private static final HttpPoolClient HTTP_POOL_CLIENT = HttpClientUtil.create(2000, 2000, 5, 5, 1, 500);

    private CodeService codeService;
    private ProjectTask projectTask;
    private CodeReviewResultService codeReviewResultService;
    private String appProjectTasksRemote;
    private String appDetailRemote;
    private String codeReviewRemote;

    public CodeReviewStatusTask(final CodeService codeService, final ProjectTask projectTask, final CodeReviewResultService codeReviewResultService, final String appProjectTasksRemote, final String appDetailRemote, final String codeReviewRemote) {
        this.codeService = codeService;
        this.projectTask = projectTask;
        this.codeReviewResultService = codeReviewResultService;
        this.appProjectTasksRemote = appProjectTasksRemote;
        this.appDetailRemote = appDetailRemote;
        this.codeReviewRemote = codeReviewRemote;
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
        try {
            String gitAddress = codeService.getGitAddress(appDetailRemote, appCode).getData();
            log.info("gitAddress is {}" + gitAddress);
            CodeReviewResult codeReviewResult = new CodeReviewResult();
            codeReviewResult.setProjectTaskId(projectTask.getId());
            Map<String, Object> codeReviewStatusParameters = codeReviewResultService.setCodeReviewStatusParameters(gitAddress, appBranch, projectTask.getId().toString());
            try {
                Optional result = codeReviewResultService.getHTTPRequest(codeReviewRemote, codeReviewStatusParameters);
                log.info("调用代码评审状态接口的返回值为：" + result.toString());
                if (result.isPresent()) {
                    JSONObject apps1 = (JSONObject) JSONObject.parse(result.get().toString());
                    if ("0".equals(apps1.get("code"))) {
                        if (apps1.get("data") != null) {
                            //只更新最新一次的应用的代码评审状态
                            JSONObject appdatas1 = JSONObject.parseObject(apps1.get("data").toString());
                            log.info("代码评审返回结果状态为：" + appdatas1.getString("status_code"));
                            codeReviewResult.setCodeReviewResult(appdatas1.getString("status_code"));
                        } else {
                            codeReviewResult.setCodeReviewResult("获取代码评审状态失败");
                        }
                    } else {
                        log.error("调用失败: {}", codeReviewRemote + JSON.toJSONString(result));
                        codeReviewResult.setCodeReviewResult("获取代码评审状态失败");
                    }
                } else {
                    codeReviewResult.setCodeReviewResult("获取代码评审状态失败");
                }
            } catch (Exception e) {
                log.error("codeReviewRemote error", e);
                codeReviewResult.setCodeReviewResult("获取代码评审状态失败");
            }
            int updateResult = codeReviewResultService.updateCodeReviewResult(codeReviewResult);
            log.info(codeReviewResult.getCodeReviewResult());
            log.info("updateResult {}", updateResult);
        } catch (Exception e) {
            log.error("appDetailRemote error", e);
        }
    }
}