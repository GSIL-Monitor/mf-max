package com.mryx.matrix.process.core.aop;

import com.mryx.matrix.common.domain.ResultVo;
import com.mryx.matrix.process.core.ccs.CcsService;
import com.mryx.matrix.process.core.annotation.CcsPermission;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;

/**
 * CCS AOP
 *
 * @author supeng
 * @date 2018/10/26
 */
@Aspect
@Component
@Order(1)
public class PermissionAop {

    @Resource
    private CcsService ccsService;

    @Pointcut(value = "@annotation(com.mryx.matrix.process.core.annotation.CcsPermission)")
    private void cutPermission() {

    }

    @Around("cutPermission()")
    public Object doPermission(ProceedingJoinPoint point) throws Throwable {
        MethodSignature ms = (MethodSignature) point.getSignature();
        Method method = ms.getMethod();
        CcsPermission permission = method.getAnnotation(CcsPermission.class);
        Object[] permissions = permission.value();
        if (permissions == null || permissions.length == 0) {
            return point.proceed();
        } else {
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
            Object oauthId = request.getAttribute("oauthId");
            boolean result = ccsService.hasPermission((Integer) oauthId, permissions[0].toString());
            if (result) {
                return point.proceed();
            } else {
                return ResultVo.Builder.FAIL().initErrCodeAndMsg("1", "您无此权限!");
            }
        }

    }

}
