package com.mryx.matrix.process.web.controller;

import com.alibaba.fastjson.JSONObject;
import com.mryx.grampus.ccs.domain.OauthUser;
import com.mryx.matrix.common.domain.ResultVo;
import com.mryx.matrix.process.core.ccs.CcsService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;
import com.mryx.matrix.process.domain.User;
import com.mryx.matrix.process.core.service.UserService;
import com.mryx.matrix.process.core.service.GmsAccessTokenService;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * 登陆
 *
 * @author zxl
 * @create 2018-09-05 11:23
 **/
@RestController
@RequestMapping("/api/user")
public class LoginController {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoginController.class);
    private static final String TOKEN_TYPE = "grampus_matrix";

    /**
     * 临时方案
     */
    @Value("${ccs.matrix.appid}")
    private Integer essAppId;
    /**
     * 临时方案
     */
    @Value("${ccs.matrix.secret}")
    private String essSecret;

    @Resource
    private UserService userService;

    @Resource
    private GmsAccessTokenService gmsAccessTokenService;

    @Resource
    private CcsService ccsService;

    /**
     * 登录
     */
    @RequestMapping(value = "/login", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    public ResultVo login(@RequestBody User user) {
        LOGGER.info("userName:{} password:{}", user.getUserName(), user.getPassword());
        JSONObject result = new JSONObject();

        if (StringUtils.isEmpty(user.getUserName()) || StringUtils.isEmpty(user.getPassword())) {
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("1", "账号密码不能为空");
        }
        try {
            List<User> users = userService.listByCondition(user);
            if (CollectionUtils.isEmpty(users)) {
                return ResultVo.Builder.FAIL().initErrCodeAndMsg("1", "请检查账号密码！！");
            } else if (users.size() == 1) {
                String accessToken = gmsAccessTokenService.generateAccessToken(Long.valueOf(users.get(0).getId()), TOKEN_TYPE);
                result.put("userName", user.getUserName());
                result.put("password", user.getPassword());
                result.put("accessToken", accessToken);
                return ResultVo.Builder.SUCC().initSuccData(result);
            } else {
                return ResultVo.Builder.FAIL().initErrCodeAndMsg("1", "出现重复账号！！");
            }
        } catch (Exception e) {
            LOGGER.error("login execution error:", e);
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("1", "系统异常");
        }
    }

    @RequestMapping(value = "/dealAccesstoken", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    public ResultVo dealAccesstocken(@RequestBody Map<String, Object> requestMap) {
        String accessToken = requestMap.get("accessToken").toString();
        Map<String, Object> userMap = gmsAccessTokenService.verifyAccessTokenByUserId(accessToken);
        LOGGER.info("verifyAccessTokenByUserId.retMap={}", userMap);
        String userId = userMap.get("userId").toString();
        User user = userService.getById(Integer.valueOf(userId));
        String userName = "";
        if (user != null) {
            userName = user.getUserName();
        }
        if ("0".equals(userMap.get("userId").toString())) {
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("1", "用户未登录");
        }
        Map<String, Object> dataMap = new HashMap<String, Object>();
        dataMap.put("userName", userName);
        return ResultVo.Builder.SUCC().initSuccData(dataMap);
    }

    /**
     * 临时方案
     * 获取CCS用户信息
     *
     * @param requestMap accessToken, userAgent, ip
     * @return
     */
    @RequestMapping(value = "/getCcsUser", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    public ResultVo getCcsUser(@RequestBody Map<String, Object> requestMap) {
        LOGGER.info("getCcsUser requestMap = {}", requestMap);
        if (requestMap == null || requestMap.isEmpty()
                || requestMap.get("accessToken") == null || "".equals(requestMap.get("accessToken"))
                || requestMap.get("userAgent") == null || "".equals(requestMap.get("userAgent"))
                || requestMap.get("ip") == null || "".equals(requestMap.get("ip"))) {
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("1", "参数错误！");
        }
        OauthUser oauthUser = ccsService.getUserByToken(requestMap.get("accessToken").toString(), requestMap.get("userAgent").toString(), requestMap.get("ip").toString());
        if (oauthUser == null) {
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("1", "获取CCS用户信息失败！");
        }
        return ResultVo.Builder.SUCC().initSuccData(oauthUser);
    }


}
