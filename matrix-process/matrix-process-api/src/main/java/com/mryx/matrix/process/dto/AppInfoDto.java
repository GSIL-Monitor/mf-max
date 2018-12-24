package com.mryx.matrix.process.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 部门信息dto
 *
 * @author wangwenbo
 * @create 2018-08-31 10:52
 **/
@Data
public class AppInfoDto implements Serializable {


    private static final long serialVersionUID = 4758117244222469800L;

    //应用代号,全局唯一
    private String appCode;
    //应用名称
    private String appName;
    //git 地址
    private String git;
    //部署路径
    private String deployPath;
    //healthcheck地址
    private String healthcheck;
    //部署参数
    private String deployParameters;
    //包名称
    private String pkgName;
    //包类型 jar war go h5 py
    private String pkgType;
    //部署端口号
    private Integer port;

    private List<GroupInfoDto> groupInfo;

    /**
     * 虚拟机参数
     */
    private String vmOption;


}
