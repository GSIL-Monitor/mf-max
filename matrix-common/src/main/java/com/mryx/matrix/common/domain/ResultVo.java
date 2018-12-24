package com.mryx.matrix.common.domain;


import java.io.Serializable;

/**
 *
 * @author dinglu
 * @date 2018/9/10
 */
public class ResultVo<T> implements Serializable {

    private static final long serialVersionUID = -6768836953608258870L;

    /**
     * success/fail
     */
    private String ret;

    /**
     * 0成功 非0具体错误原因
     */
    private String code;

    /**
     * 具体错误描述or成功描述
     */
    private String message;

    /**
     * 存放业务数据
     */
    private T data;

    public static class Builder {

        @SuppressWarnings("rawtypes")
        public static ResultVo SUCC() {
            ResultVo vo = new ResultVo();
            vo.setRet("success");
            vo.setCode("0");
            return vo;
        }

        @SuppressWarnings("rawtypes")
        public static ResultVo FAIL() {
            ResultVo vo = new ResultVo();
            vo.setRet("fail");
            return vo;
        }
    }

    public ResultVo<T> initErrCodeAndMsg(String code, String message) {
//        logger.info("code="+code + " message="+message);
        this.code = code;
        this.message = message;
        return this;
    }

    public ResultVo<T> initErrCodeAndMsgAndData(String code, String message, T data) {
//        logger.info("code="+code + " message="+message);
        this.code = code;
        this.message = message;
        this.data = data;
        return this;
    }

    public ResultVo<T> initSuccDataAndMsg(String code, String message) {
//        logger.info("code="+code + " message="+message);
        this.code = code;
        this.message = message;
        return this;
    }

    public ResultVo<T> initSuccData(T data) {
//        logger.info(JSON.toJSONString(data));
        this.data = data;
        return this;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public String getRet() {
        return ret;
    }

    public void setRet(String ret) {
        this.ret = ret;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
