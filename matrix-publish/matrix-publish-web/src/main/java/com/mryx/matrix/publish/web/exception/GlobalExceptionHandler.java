package com.mryx.matrix.publish.web.exception;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 统一异常处理
 * 处理Controller层未捕获的异常
 * @author supeng
 * @date 2018/09/07
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    @ResponseBody
    Object handleException(){
        return "系统异常！";
    }

    //自定义异常
}
