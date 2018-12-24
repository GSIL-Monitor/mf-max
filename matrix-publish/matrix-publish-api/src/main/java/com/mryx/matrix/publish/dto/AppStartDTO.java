package com.mryx.matrix.publish.dto;

import lombok.Data;

/**
 * AppStartDTO
 *
 * @author supeng
 * @date 2018/11/17
 */
@Data
public class AppStartDTO {
    /**
     * 构建类型 beta release ...
     */
    private String buildType;
    /**
     * 包类型 jar go jetty tomcat static ...
     */
    private String packageType;
    /**
     * 包名
     */
    private String packageName;
    /**
     * 包路径
     */
    private String packagePath;
    /**
     * 容器路径
     */
    private String containerPath;
    /**
     * 包下载Url
     */
    private String packageUrl;
    /**
     * md5值
     */
    private String md5Name;
    /**
     * 执行动作 restart start stop ...
     */
    private String action;
    /**
     * 启动参数
     */
    private String startOps;
    /**
     * 发布记录ID
     */
    private String recordId;
    /**
     * 应用机器IP
     */
    private String ip;
    /**
     * 探活接口
     */
    private String healthCheckUrl;
    /**
     * wms服务名称
     */
    private String wmsJsService;
    /**
     * 服务停止脚本全路径
     */
    private String stopShellPath;

    /**
     * 服务启动脚本全路径
     */
    private String startShellPath;

    /**
     * docker启动命令
     */
    private String dockerStartCmd;

    /**
     * 启动日志路径
     */
    private String appLogPath;

    /**
     * 启动成功标识
     */
    private String appLogKeyWord;

    /**
     * 更新方式，all为全部更新
     */
    private String mode;
}
