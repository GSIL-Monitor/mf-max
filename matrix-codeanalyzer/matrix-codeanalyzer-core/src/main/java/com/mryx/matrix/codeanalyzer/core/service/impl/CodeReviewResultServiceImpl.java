package com.mryx.matrix.codeanalyzer.core.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import com.mryx.common.utils.HttpClientUtil;
import com.mryx.common.utils.HttpPoolClient;
import com.mryx.matrix.codeanalyzer.core.dao.CodeReviewResultDao;
import com.mryx.matrix.codeanalyzer.core.service.CodeReviewResultService;
import com.mryx.matrix.codeanalyzer.core.service.CodeService;
import com.mryx.matrix.codeanalyzer.core.utils.BranchAppCodeUtil;
import com.mryx.matrix.codeanalyzer.domain.CodeReviewResult;
import com.mryx.matrix.publish.domain.ProjectTask;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @program: matrix-codeanalyzer
 * @description: 代码结果评审表
 * @author: jianghong
 * @create: 2018-09-28 16:37
 */
@Slf4j
@Service("codeReviewResultService")
public class CodeReviewResultServiceImpl implements CodeReviewResultService {
    private static Logger logger = LoggerFactory.getLogger(CodeReviewResultServiceImpl.class);
    private static final HttpPoolClient HTTP_POOL_CLIENT = HttpClientUtil.create(250000, 250000, 5, 5, 0, 250000);


    @Resource
    private CodeReviewResultDao codeReviewResultDao;

    @Override
    public Map<String, Object> setCodeReviewCreateParameters(String gitAddress, String appBranch, String projectTaskId, List<String> codeReviewPerson) {
        Map<String, Object> parameters = Maps.newHashMap();
        parameters.put("git", gitAddress);
        parameters.put("branch", appBranch);
        parameters.put("id", projectTaskId);
        parameters.put("reviewer", codeReviewPerson);
        return parameters;
    }

    @Override
    public Map<String, Object> setCodeReviewStatusParameters(String gitAddress, String appBranch, String projectTaskId) {
        Map<String, Object> parameters = Maps.newHashMap();
        parameters.put("git", gitAddress);
        parameters.put("branch", appBranch);
        parameters.put("id", projectTaskId);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date();
        String codeReviewTime = formatter.format(date);
        parameters.put("time", codeReviewTime);
        return parameters;
    }

    @Override
    public Optional<String> getHTTPRequest(String url, Map<String, Object> json) throws RuntimeException {
        Optional<String> optional = HTTP_POOL_CLIENT.postJson(url, JSONObject.toJSONString(json));
        return optional;
    }

    @Override
    public int insertCodeReviewResult(CodeReviewResult codeReviewResult) {
        return codeReviewResultDao.insertCodeReviewResult(codeReviewResult);
    }

    @Override
    public int updateCodeReviewResult(CodeReviewResult codeReviewResult) {
        return codeReviewResultDao.updateCodeReviewResult(codeReviewResult);
    }

    @Override
    public String getCodeReviewStatus(Integer id) {
        return codeReviewResultDao.getCodeReviewStatus(id);
    }

    @Override
    public String getCodeReviewStatus(CodeService codeService, CodeReviewResultService codeReviewResultService, ProjectTask projectTask, String appProjectTasksRemote, String appDetailRemote, String codeReviewRemote) {
        String appBranch = "";
        String appCode = "";
        Map<String, String> map = BranchAppCodeUtil.getBranchAndAppCode(projectTask, appProjectTasksRemote);
        if (!map.isEmpty()) {
            appBranch = map.get("branch");
            appCode = map.get("appCode");
        }
        String  updateResult = "0";
        try {
            String gitAddress = codeService.getGitAddress(appDetailRemote, appCode).getData();
            log.info("gitAddress is {}" + gitAddress);
            CodeReviewResult codeReviewResult = new CodeReviewResult();
            codeReviewResult.setProjectTaskId(projectTask.getId());
            Map<String, Object> codeReviewStatusParameters = codeService.setParameters(gitAddress, appBranch, projectTask.getId().toString());
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
                            updateResult=appdatas1.getString("status_code");
                            codeReviewResult.setCodeReviewResult(appdatas1.getString("status_code"));
                        } else {
                            logger.error("获取代码评审状态失败");
                        }
                    } else {
                        logger.error("获取代码评审状态失败:{}",JSON.toJSONString(result));
                    }
                } else {
                    logger.error("获取代码评审状态失败:{}",JSON.toJSONString(result));
                }
            } catch (Exception e) {
                logger.error("获取代码评审状态失败");
            }
            Integer update = codeReviewResultService.updateCodeReviewResult(codeReviewResult);
            log.info("updateResult {}", update);
            if(update<=0){
                logger.error("代码评审状态更新失败");
            }
        } catch (Exception e) {
            log.error("获取代码评审状态失败", e);
        }
        return updateResult;
    }

    @Override
    public String getCodeReviewPeople(Integer id) {
        return codeReviewResultDao.getCodeReviewPeople(id);
    }
}
