package com.mryx.matrix.process.web.filter;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Maps;
import com.mryx.grampus.ccs.domain.OauthUser;
import com.mryx.matrix.common.domain.ResultVo;
import com.mryx.matrix.process.core.ccs.CcsService;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.springframework.core.annotation.Order;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.Result;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import java.util.Set;

/**
 * CCS Filter
 *
 * @author supeng
 * @date 2018/10/26
 */
@Slf4j
@Order(value = 1)
@WebFilter(filterName = "ccsTokenFilter", urlPatterns = {"/api/project/*", "/api/matrix/user/*", "/api/beta/*"})
public class CcsTokenFilter implements Filter {

    @Resource
    private CcsService ccsService;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        log.info("ccsTokenFilter init");
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        log.info("ccsTokenFilter doFilter");
//没有accessToken的请求直接返回
        String accessToken = null;
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        String url = request.getRequestURI();
        if ("GET".equals(request.getMethod())) {
            accessToken = request.getParameter("accessToken");
        } else {
            accessToken = request.getHeader("accessToken");
        }
        log.info("doFilterInternal: url=[{}], accessToken=[{}]", request.getRequestURI(), accessToken);
        if (StringUtils.isEmpty(accessToken)) {
            errorHandle(response, "token不能为空", HttpStatus.SC_UNAUTHORIZED);
            return;
        }
//将accessToken转换为userId,如果返回null则表示用户不存在,直接返回
        OauthUser u = null;
        try {
            log.info("doFilterInternal accessToken={}", accessToken);
            u = ccsService.getUserByToken(accessToken,request);
            if (u == null) {
                errorHandle(response, "用户认证CCS失败", HttpStatus.SC_UNAUTHORIZED);
                return;
            }
        } catch (Exception e) {
            log.info("doFilterInternal: e=[{}]", e);
            errorHandle(response, "用户认证失败", HttpStatus.SC_UNAUTHORIZED);
        }
//TODO 埋下全局的用户信息 可在普通控制器里直接获取到以下的参数 不需要拿id在查用户信息
        request.setAttribute("oauthId", u == null ? 0 : u.getId());
        Map<String, Object> params = Maps.newHashMap();
        if (u != null) {
            params.put("oauthId", u.getId());
            params.put("oauthName", u.getName());
            params.put("oauthAccount", u.getAccount());
            params.put("oauthMobile", u.getMobile());
            params.put("oauthEmail", u.getEmail());
        }
        AuthorRequestWrapper authorRequestWrapper = new AuthorRequestWrapper(request, params);
        filterChain.doFilter(authorRequestWrapper, response);
    }

    @Override
    public void destroy() {
        log.info("ccsTokenFilter destroy");

    }


    private void errorHandle(HttpServletResponse response, String message, int status) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        ResultVo commonVo = ResultVo.Builder.FAIL().initErrCodeAndMsg("" + status + "", message);
        try (PrintWriter out = response.getWriter()) {
            out.write(JSON.toJSONString(commonVo));
        }
    }

    // 自定义session注入类能向request添加当前登录用户的信息
    private class AuthorRequestWrapper extends HttpServletRequestWrapper {
        private Map<String, String[]> params = Maps.newHashMap();

        AuthorRequestWrapper(HttpServletRequest request) {
            super(request);
            this.params.putAll(request.getParameterMap());
            this.modifyParameterValues();
        }

        /**
         * 重载构造方法
         *
         * @param request
         * @param extendParams
         */
        AuthorRequestWrapper(HttpServletRequest request, Map<String, Object> extendParams) {
            this(request);
            //这里将扩展参数写入参数表
            addAllParameters(extendParams);
        }

        void modifyParameterValues() {//将parameter的值去除空格后重写回去
            Set<String> set = params.keySet();
            for (String key : set) {
                String[] values = params.get(key);
                values[0] = values[0].trim();
                params.put(key, values);
            }
        }

        @Override
        public String getParameter(String name) {//重写getParameter，代表参数从当前类中的map获取
            String[] values = params.get(name);
            if (values == null || values.length == 0) {
                return null;
            }
            return values[0];
        }

        @Override
        public String[] getParameterValues(String name) {
            return params.get(name);
        }

        private void addAllParameters(Map<String, Object> otherParams) {
            for (Map.Entry<String, Object> entry : otherParams.entrySet()) {
                addParameter(entry.getKey(), entry.getValue());
            }
        }

        private void addParameter(String name, Object value) {//增加参数
            if (value != null) {
                if (value instanceof String[]) {
                    params.put(name, (String[]) value);
                } else if (value instanceof String) {
                    params.put(name, new String[]{value.toString()});
                } else {
                    params.put(name, new String[]{String.valueOf(value)});
                }
            }
        }
    }
}
