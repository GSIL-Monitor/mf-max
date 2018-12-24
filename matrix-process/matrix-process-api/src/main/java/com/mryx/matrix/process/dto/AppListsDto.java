package com.mryx.matrix.process.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * appCode 和 appName
 *
 * @author wangwenbo
 * @create 2018-08-31 16:03
 **/
@Data
public class AppListsDto implements Serializable {

    private static final long serialVersionUID = 6023668715251373859L;

    public AppListsDto() {
    }

    public AppListsDto(String appCode, String appName) {
        this.appCode = appCode;
        this.appName = appName;
    }

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

    private Long deptId;
    /**
     * 部门名称
     */
    private String deptName;

    //上级部门ID，一级部门为0
    private Long parentId;

    //部门名称
    private String name;
    //部门编码
    private String code;

    private List<GroupInfoDto> groupInfo;
}
