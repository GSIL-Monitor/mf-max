package com.mryx.matrix.common.dto;

import java.io.Serializable;
import java.util.List;

/**
 * 部门信息dto
 *
 * @author wangwenbo
 * @create 2018-08-31 10:52
 **/
public class AppInfoDto implements Serializable {


    private static final long serialVersionUID = 4758117244222469800L;

    /**
     * 应用代号,全局唯一
     */
    private String appCode;
    /**
     * 应用名称
     */
    private String appName;
    /**
     * git 地址
     */
    private String git;
    /**
     * 编译完成的包的路径
     */
    private String deployPath;
    /**
     * healthcheck接口
     */
    private String healthcheck;
    /**
     * 编译参数（maven等）
     */
    private String deployParameters;
    /**
     * 启动参数（jvm等）
     */
    private String vmOption;
    /**
     * 包名称
     */
    private String pkgName;

    public String getVmOption() {
        return vmOption;
    }

    public void setVmOption(String vmOption) {
        this.vmOption = vmOption;
    }

    public Integer getNexusTag() {
        return nexusTag;
    }

    public void setNexusTag(Integer nexusTag) {
        this.nexusTag = nexusTag;
    }

    /**
     * 包类型 JAR JETTY TOMCAT GO STATIC PY
     */
    private String pkgType;
    /**
     * 应用端口号
     */
    private Integer port;
    /**
     * 私服标记
     */
    private Integer nexusTag;
    /**
     * 包的部署路径
     */
    private String pkgPath;
    /**
     * Tomcat/Jetty容器路径
     */
    private String containerPath;

    private List<GroupInfoDto> groupInfo;

    private DeptDto deptDto;

    private String startShellPath;

    private String stopShellPath;

    private String startLogPath;

    private String startSuccFlag;

    /**
     * 负载类型
     */
    private String loadType;
    /**
     * springcloud APPID
     */
    private String springCloudAppID;
    /**
     * springcloud 端口
     */
    private String springCloudAppPort;

    public String getAppCode() {
        return appCode;
    }

    public void setAppCode(String appCode) {
        this.appCode = appCode;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getGit() {
        return git;
    }

    public void setGit(String git) {
        this.git = git;
    }

    public String getDeployPath() {
        return deployPath;
    }

    public void setDeployPath(String deployPath) {
        this.deployPath = deployPath;
    }

    public String getHealthcheck() {
        return healthcheck;
    }

    public void setHealthcheck(String healthcheck) {
        this.healthcheck = healthcheck;
    }

    public String getDeployParameters() {
        return deployParameters;
    }

    public void setDeployParameters(String deployParameters) {
        this.deployParameters = deployParameters;
    }

    public String getPkgName() {
        return pkgName;
    }

    public void setPkgName(String pkgName) {
        this.pkgName = pkgName;
    }

    public String getPkgType() {
        return pkgType;
    }

    public void setPkgType(String pkgType) {
        this.pkgType = pkgType;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public List<GroupInfoDto> getGroupInfo() {
        return groupInfo;
    }

    public void setGroupInfo(List<GroupInfoDto> groupInfo) {
        this.groupInfo = groupInfo;
    }

    public String getPkgPath() {
        return pkgPath;
    }

    public void setPkgPath(String pkgPath) {
        this.pkgPath = pkgPath;
    }

    public String getContainerPath() {
        return containerPath;
    }

    public void setContainerPath(String containerPath) {
        this.containerPath = containerPath;
    }

    public DeptDto getDeptDto() {
        return deptDto;
    }

    public void setDeptDto(DeptDto deptDto) {
        this.deptDto = deptDto;
    }

    public String getStartShellPath() {
        return startShellPath;
    }

    public void setStartShellPath(String startShellPath) {
        this.startShellPath = startShellPath;
    }

    public String getStopShellPath() {
        return stopShellPath;
    }

    public void setStopShellPath(String stopShellPath) {
        this.stopShellPath = stopShellPath;
    }

    public String getStartLogPath() {
        return startLogPath;
    }

    public void setStartLogPath(String startLogPath) {
        this.startLogPath = startLogPath;
    }

    public String getStartSuccFlag() {
        return startSuccFlag;
    }

    public void setStartSuccFlag(String startSuccFlag) {
        this.startSuccFlag = startSuccFlag;
    }

    public String getLoadType() {
        return loadType;
    }

    public void setLoadType(String loadType) {
        this.loadType = loadType;
    }

    public String getSpringCloudAppID() {
        return springCloudAppID;
    }

    public void setSpringCloudAppID(String springCloudAppID) {
        this.springCloudAppID = springCloudAppID;
    }

    public String getSpringCloudAppPort() {
        return springCloudAppPort;
    }

    public void setSpringCloudAppPort(String springCloudAppPort) {
        this.springCloudAppPort = springCloudAppPort;
    }

    /**
     * default
     */
    public AppInfoDto() {
    }

    /**
     * all
     *
     * @param appCode
     * @param appName
     * @param git
     * @param deployPath
     * @param healthcheck
     * @param deployParameters
     * @param vmOption
     * @param pkgName
     * @param pkgType
     * @param port
     * @param nexusTag
     * @param pkgPath
     * @param containerPath
     * @param groupInfo
     * @param deptDto
     */
    public AppInfoDto(String appCode, String appName, String git, String deployPath, String healthcheck, String deployParameters, String vmOption, String pkgName, String pkgType, Integer port, Integer nexusTag, String pkgPath, String containerPath, List<GroupInfoDto> groupInfo, DeptDto deptDto) {
        this.appCode = appCode;
        this.appName = appName;
        this.git = git;
        this.deployPath = deployPath;
        this.healthcheck = healthcheck;
        this.deployParameters = deployParameters;
        this.vmOption = vmOption;
        this.pkgName = pkgName;
        this.pkgType = pkgType;
        this.port = port;
        this.nexusTag = nexusTag;
        this.pkgPath = pkgPath;
        this.containerPath = containerPath;
        this.groupInfo = groupInfo;
        this.deptDto = deptDto;
    }

    @Override
    public String toString() {
        return "AppInfoDto{" +
                "appCode='" + appCode + '\'' +
                ", appName='" + appName + '\'' +
                ", git='" + git + '\'' +
                ", deployPath='" + deployPath + '\'' +
                ", healthcheck='" + healthcheck + '\'' +
                ", deployParameters='" + deployParameters + '\'' +
                ", vmOption='" + vmOption + '\'' +
                ", pkgName='" + pkgName + '\'' +
                ", pkgType='" + pkgType + '\'' +
                ", port=" + port +
                ", nexusTag=" + nexusTag +
                ", pkgPath='" + pkgPath + '\'' +
                ", containerPath='" + containerPath + '\'' +
                ", groupInfo=" + groupInfo +
                ", deptDto=" + deptDto +
                ", startShellPath='" + startShellPath + '\'' +
                ", stopShellPath='" + stopShellPath + '\'' +
                ", startLogPath='" + startLogPath + '\'' +
                ", startSuccFlag='" + startSuccFlag + '\'' +
                ", loadType='" + loadType + '\'' +
                ", springCloudAppID='" + springCloudAppID + '\'' +
                ", springCloudAppPort='" + springCloudAppPort + '\'' +
                '}';
    }

    /**
     * 兼容
     *
     * @param appCode
     * @param appName
     * @param git
     * @param deployPath
     * @param healthcheck
     * @param deployParameters
     * @param vmOption
     * @param pkgName
     * @param pkgType
     * @param port
     * @param nexusTag
     * @param groupInfo
     */
    public AppInfoDto(String appCode, String appName, String git, String deployPath, String healthcheck, String deployParameters, String vmOption, String pkgName, String pkgType, Integer port, Integer nexusTag, List<GroupInfoDto> groupInfo) {
        this.appCode = appCode;
        this.appName = appName;
        this.git = git;
        this.deployPath = deployPath;
        this.healthcheck = healthcheck;
        this.deployParameters = deployParameters;
        this.vmOption = vmOption;
        this.pkgName = pkgName;
        this.pkgType = pkgType;
        this.port = port;
        this.nexusTag = nexusTag;
        this.groupInfo = groupInfo;
    }

}
