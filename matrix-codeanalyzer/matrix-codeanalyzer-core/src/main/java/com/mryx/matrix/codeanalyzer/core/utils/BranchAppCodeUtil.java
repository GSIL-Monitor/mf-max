package com.mryx.matrix.codeanalyzer.core.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import com.mryx.common.utils.HttpClientUtil;
import com.mryx.common.utils.HttpPoolClient;
import com.mryx.matrix.common.domain.ResultVo;
import com.mryx.matrix.publish.domain.ProjectTask;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Optional;

@Slf4j
public class BranchAppCodeUtil {
    private static final HttpPoolClient HTTP_POOL_CLIENT = HttpClientUtil.create(2000, 2000, 5, 5, 1, 500);

    public static Map<String, String> getBranchAndAppCode(ProjectTask projectTask, String remote) {
        String appBranch = "";
        String appCode = "";
        try {
            ProjectTask param = new ProjectTask();
            param.setId(projectTask.getId());
            Optional<String> result = HTTP_POOL_CLIENT.postJson(remote, JSON.toJSONString(param));
            log.info("param = {},result = {}", param, result);
            if (result.isPresent()) {
                ResultVo resultVo = JSONObject.parseObject(result.get(), ResultVo.class);
                log.info("resultVo = {}", JSON.toJSONString(resultVo));
                if (resultVo != null && "0".equals(resultVo.getCode())) {
                    JSONObject jsonObject = JSONObject.parseObject(String.valueOf(resultVo.getData()));
                    if (jsonObject != null) {
                        appBranch = jsonObject.getString("appBranch");
                        appCode = jsonObject.getString("appCode");
                    } else {
                        log.info("projectTask is null");
                        return null;
                    }
                } else {
                    log.info("get branch&appCode failure");
                    return null;
                }
            } else {
                log.info("http get branch&appCode failure");
                return null;
            }
        } catch (Exception e) {
            log.error("get branch&appCode error ", e);
        }
        log.info("branch is " + appBranch + " appCode is " + appCode);
        Map<String, String> map = Maps.newHashMap();
        map.put("branch", appBranch);
        map.put("appCode", appCode);
        return map;
    }
}