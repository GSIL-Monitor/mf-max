package com.mryx.matrix.common.enums;

/**
 * 返回值编码
 *
 * @author supeng
 * @date 2018/11/12
 */
public enum ResultType {
    SUCCESS("0", "成功！"),
    FAILURE("1", "失败！"),
    PARAMETER_ERROR("2", "参数错误！"),
    SYSTEM_EXCEPTION("3", "系统异常！");

    /**
     * 返回编码
     */
    private String code;
    /**
     * 返回信息
     */
    private String message;

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

    ResultType(String code, String message) {
        this.code = code;
        this.message = message;
    }
}
