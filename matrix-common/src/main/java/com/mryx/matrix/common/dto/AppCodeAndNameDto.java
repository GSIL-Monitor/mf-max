package com.mryx.matrix.common.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * appCode 和 appName
 *
 * @author wangwenbo
 * @create 2018-08-31 16:03
 **/
@Data
public class AppCodeAndNameDto implements Serializable{

    private static final long serialVersionUID = 6023668715251373859L;

    public AppCodeAndNameDto() {
    }

    public AppCodeAndNameDto(String appCode, String appName) {
        this.appCode = appCode;
        this.appName = appName;
    }

    //应用代号,全局唯一
    private String appCode;
    //应用名称
    private String appName;

}
