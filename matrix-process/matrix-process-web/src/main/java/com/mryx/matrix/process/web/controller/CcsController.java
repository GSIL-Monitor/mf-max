package com.mryx.matrix.process.web.controller;

import com.mryx.matrix.common.domain.ResultVo;
import com.mryx.matrix.process.core.ccs.CcsService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * CcsController
 *
 * @author supeng
 * @date 2018/10/26
 */
@Slf4j
@RestController
@RequestMapping("/api/matrix/user")
public class CcsController {

    @Autowired
    private CcsService ccsService;

    @RequestMapping("/login")
    public Object login(Integer oauthId, String oauthName) {
        log.info("login oauthId = {},oauthName = {}", oauthId, oauthName);
        if (oauthId != null && oauthId > 0) {
            Map<String, Object> ret = new HashMap<>();
            ret.put("id", oauthId);
            ret.put("name", oauthName);
            return ResultVo.Builder.SUCC().initSuccData(ret);
        }
        return ResultVo.Builder.FAIL().initErrCodeAndMsg("1", "校验登录信息失败，请重新登录。");
    }

    @RequestMapping("/my")
    public Object my(Integer oauthId, @RequestBody Map req, HttpServletRequest request) {
        log.info("my req= {},accessToken = {}", req, request.getHeader("accessToken"));
        Long timestamp = MapUtils.getLong(req, "timestamp");
        if (timestamp == null || timestamp <= 0) {
            timestamp = System.currentTimeMillis();
        }
        String accessToken = "";
        if (req.get("accessToken") != null && !Objects.equals("", req.get("accessToken").toString())) {
            accessToken = req.get("accessToken").toString();
        } else if (request.getHeader("accessToken") != null && !Objects.equals("", request.getHeader("accessToken"))) {
            accessToken = request.getHeader("accessToken");
        }
        Map<String, Object> map = ccsService.getMenuByOauthId(oauthId, timestamp, accessToken);
        if (MapUtils.isNotEmpty(map)) {
            return ResultVo.Builder.SUCC().initSuccData(map);
        }
        return ResultVo.Builder.FAIL().initErrCodeAndMsg("1", "获取菜单权限失败，请检查权限或联系管理员授权。");
    }

}
