package com.mryx.matrix.publish.web.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.mryx.common.utils.HttpPoolClient;
import com.mryx.common.utils.StringUtils;
import com.mryx.matrix.common.domain.ResultVo;
import com.mryx.matrix.common.dto.AppInfoDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @author pengcheng
 * @description
 * @email pengcheng@missfresh.cn
 * @date 2018-12-18 17:21
 **/
@Service
public class AppCenterUtil {

    private static final Logger logger = LoggerFactory.getLogger(AppCenterUtil.class);

    private static final HttpPoolClient HTTP_POOL_CLIENT = com.mryx.common.utils.HttpClientUtil.create(2000, 2000, 5, 5, 1, 500);

    @Value("${appDetail_remote}")
    private String appDetailRemote;

    public AppInfoDto getAppInfo(String appCode, String env) {
        Map<String, Object> map = new HashMap<>();
        map.put("appCode", appCode);
        map.put("envCode", env);
        Optional<String> optional = HTTP_POOL_CLIENT.postJson(appDetailRemote, JSONObject.toJSONString(map));
        String response = optional.isPresent() ? optional.get() : "";
        logger.info("调用应用中心，返回的结果是：{}", response);
        if (StringUtils.isEmpty(response)) {
            return null;
        }
        ResultVo resultVo = JSON.parseObject(response, ResultVo.class);
        return JSON.parseObject(String.valueOf(resultVo.getData()), AppInfoDto.class);
    }
}
