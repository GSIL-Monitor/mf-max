package com.mryx.matrix.process.core.ccs;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.mryx.common.utils.HttpClientUtil;
import com.mryx.common.utils.HttpPoolClient;
import com.mryx.grampus.ccs.domain.OauthUser;
import com.mryx.grampus.ccs.result.CommonVo;
import com.mryx.grampus.ccs.util.Env;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Ccs Service
 *
 * @author supeng
 * @date 2018/10/26
 */
@Slf4j
@Component
public class CcsService {

    @Value("${ccs.validatetoken.url}")
    private String validateToKenUrl;

    @Value("${ccs.getmenubyoauthid.url}")
    private String getMenuByOauthIdUrl;

    @Value("${ccs.haspermission.url}")
    private String hasPermissionUrl;

    @Value("${ccs.matrix.appid}")
    private Integer essAppId;

    @Value("${ccs.matrix.secret}")
    private String essSecret;

    private static final HttpPoolClient HTTP_POOL_CLIENT = HttpClientUtil.create(3000, 3000, 20, 20, 1, 500);

    /**
     * 调用CCS系统鉴权返回用户核心字段
     *
     * @param accessToken
     * @return userId, account, name, mobile, email
     */
    public OauthUser getUserByToken(String accessToken, HttpServletRequest request) {
        Env env = Env.resolve();
//        String userAgent = request.getHeader("User-Agent");
//        String ip = request.getHeader("X-Forwarded-For");
//        if (ip == null || "".equals(ip) || "unKnown".equals(ip)) {
//            ip = request.getRequestURI();
//        }

        String userAgent = request.getHeader("user-agent");
        String ip = request.getHeader("X-Forwarded-For");
        if (!StringUtils.isEmpty(ip) && !"unKnown".equalsIgnoreCase(ip)) {
            //多次反向代理后会有多个ip值，第一个ip才是真实ip
            int index = ip.indexOf(",");
            if (index != -1) {
                ip = ip.substring(0, index);
            }
        } else {
            ip = request.getHeader("X-Real-IP");
            if (StringUtils.isEmpty(ip) || "unKnown".equalsIgnoreCase(ip)) {
                ip = request.getRemoteAddr();
            }
        }

        CommonVo userObject = validateToKen(essAppId, essSecret, accessToken, env, userAgent, ip);
        log.debug("validateCcsToKen={},systemId={},appSecret={},userObject={}", accessToken, essAppId, essSecret, JSON.toJSONString(userObject));
        Map<String, Object> userMap;
        OauthUser user = null;
        if ("0".equals(userObject.getCode())) {
            userMap = (Map<String, Object>) userObject.getData();
            user = new OauthUser();
            user.setId(MapUtils.getInteger(userMap, "userId", 0));
            if (user.getId().equals(0)) {
                log.info("{}", userMap);
            }
            user.setAccount(MapUtils.getString(userMap, "account"));
            user.setName(MapUtils.getString(userMap, "name"));
            user.setMobile(MapUtils.getString(userMap, "mobile"));
            user.setEmail(MapUtils.getString(userMap, "email"));
        } else {
            log.info("validateCcsToKen fail token={} ret={}", accessToken, userObject);
        }
        return user;
    }


    public OauthUser getUserByToken(String accessToken, String userAgent,String ip) {
        Env env = Env.resolve();
        CommonVo userObject = validateToKen(essAppId, essSecret, accessToken, env, userAgent, ip);
        log.debug("validateCcsToKen={},systemId={},appSecret={},userObject={}", accessToken, essAppId, essSecret, JSON.toJSONString(userObject));
        Map<String, Object> userMap;
        OauthUser user = null;
        if ("0".equals(userObject.getCode())) {
            userMap = (Map<String, Object>) userObject.getData();
            user = new OauthUser();
            user.setId(MapUtils.getInteger(userMap, "userId", 0));
            if (user.getId().equals(0)) {
                log.info("{}", userMap);
            }
            user.setAccount(MapUtils.getString(userMap, "account"));
            user.setName(MapUtils.getString(userMap, "name"));
            user.setMobile(MapUtils.getString(userMap, "mobile"));
            user.setEmail(MapUtils.getString(userMap, "email"));
        } else {
            log.info("validateCcsToKen fail token={} ret={}", accessToken, userObject);
        }
        return user;
    }

    private CommonVo validateToKen(Integer appId, String secret, String token, Env env, String userAgent, String ip) {
        log.info("validateToKen {},{},{},{},{}",appId,secret,token,userAgent,ip);
        HttpRequestBase requestBase = new HttpPost();
        try {
            requestBase.setURI(new URI(validateToKenUrl));
        } catch (URISyntaxException e) {
            log.error("validateToKen error", e);
            return CommonVo.Builder.FAIL().initErrCodeAndMsg("1", "请求URI错误！");
        }
        requestBase.setHeader("appId", String.valueOf(appId));
        requestBase.setHeader("secret", secret);
        requestBase.setHeader("accessToken", token);
        requestBase.setHeader("Content-Type", "application/json;charset=UTF-8");
        requestBase.setHeader("User-Agent", userAgent);
        requestBase.setHeader("X-Forwarded-For", ip);

        Map<String, Object> map = new HashMap<>();
        map.put("appId", appId);
        map.put("secret", secret);
        map.put("accessToken", token);

//        StringRequestEntity e = new StringRequestEntity(((JSONArray) json).toJSONString(), "application/json", "UTF-8");
        ((HttpPost) requestBase).setEntity(new StringEntity(JSONObject.toJSONString(map), "UTF-8"));
        Optional<String> optionalS = HTTP_POOL_CLIENT.request(requestBase);
        if (optionalS.isPresent()) {
            try {
                CommonVo result = JSONObject.parseObject(optionalS.get(), CommonVo.class);
                if (result != null) {
                    return result;
                }
            } catch (Exception e) {
                log.error("validateToKen error", e);
            }
        }
        return CommonVo.Builder.FAIL().initErrCodeAndMsg("1", "校验token失败！");
    }

//    /**
//     * 根据ccs_id查询用户
//     *
//     * @param oauthId
//     * @return
//     */
//    public OauthUser getByOauthId(Integer oauthId) {
//        return ccsUserService.getByOauthId(oauthId);
//    }

    /**
     * 获取用户菜单权限
     *
     * @param oauthId
     * @param timestamp
     * @return 菜单树 version:String,dataList:List<OauthMenuDataModel>,permissionList:Map<String,String>
     */
    public Map<String, Object> getMenuByOauthId(Integer oauthId, Long timestamp, String accessToken) {
        return getMenuByOauthId(essAppId, essSecret, oauthId, timestamp, accessToken);
    }

    private Map<String, Object> getMenuByOauthId(Integer appId, String secret, Integer oauthId, Long timeStamp, String accessToken) {
        log.info("getMenuByOauthId appId = {},secret = {},oauthId = {},timeStamp = {},accessToken = {}", appId, secret, oauthId, timeStamp, accessToken);
        HttpRequestBase requestBase = new HttpPost();
        try {
            requestBase.setURI(new URI(getMenuByOauthIdUrl));
        } catch (URISyntaxException e) {
            log.error("validateToKen error", e);
            return null;
        }

        requestBase.setHeader("appId", String.valueOf(appId));
        requestBase.setHeader("secret", secret);
        requestBase.setHeader("accessToken", accessToken);
        requestBase.setHeader("Content-Type", "application/json;charset=UTF-8");


        Map<String, Object> map = new HashMap<>();
        map.put("appId", appId);
        map.put("secret", secret);
        map.put("oauthId", oauthId);
        map.put("timeStamp", timeStamp);

        ((HttpPost) requestBase).setEntity(new StringEntity(JSONObject.toJSONString(map), "UTF-8"));
        Optional<String> optionalS = HTTP_POOL_CLIENT.request(requestBase);
        if (optionalS.isPresent()) {
            try {
                log.info("getMenuByOauthId optionalS = {}", optionalS.get());
                CommonVo result = JSONObject.parseObject(optionalS.get(), CommonVo.class);
                if ("0".equals(result.getCode())) {
                    return (Map<String, Object>) result.getData();
                }
            } catch (Exception e) {
                log.error("getMenuByOauthId error", e);
            }
        }
        return null;
    }

    /**
     * 校验用户有没有某项操作权限
     *
     * @param oauthId
     * @param permissionKey
     * @return
     */
    public boolean hasPermission(Integer oauthId, String permissionKey) {
        return hasPermission(essAppId, essSecret, oauthId, permissionKey);
    }

    private boolean hasPermission(Integer appId, String secret, Integer oauthId, String permissionKey) {
        Map<String, Object> map = new HashMap<>();
        map.put("appId", appId);
        map.put("secret", secret);
        map.put("oauthId", oauthId);
        map.put("permissionKey", permissionKey);

        Optional<String> optionalS = HTTP_POOL_CLIENT.postJson(hasPermissionUrl, JSON.toJSONString(map));
        if (optionalS.isPresent()) {
            try {
                CommonVo result = JSONObject.parseObject(optionalS.get(), CommonVo.class);
                if ("0".equals(result.getCode()) && Boolean.parseBoolean(String.valueOf(result.getData()))) {
                    return true;
                }
            } catch (Exception e) {
                log.error("getMenuByOauthId error", e);
            }
        }
        return false;

    }

}
