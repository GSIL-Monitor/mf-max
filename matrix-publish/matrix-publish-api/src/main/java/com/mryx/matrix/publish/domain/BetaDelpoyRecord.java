package com.mryx.matrix.publish.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.mryx.matrix.common.domain.Page;


/**
 * 测试环境部署记录表
 *
 * @author zhaoxl
 * @email zhaoxl02@missfresh.cn
 * @date 2018-09-07 15:55
 **/
@Data
public class BetaDelpoyRecord extends Page implements Serializable {

    private static final long serialVersionUID = 5280429702310379299L;

    /**
     * 测试环境部署记录id
     **/
    private Integer id;

    /**
     * 项目ID
     **/
    private Integer projectId;

    /**
     * 应用发布工单ID
     **/
    private Integer projectTaskId;

    /**
     * 应用标识
     **/
    private String appCode;

    /**
     * 应用名称
     **/
    private String appName;

    /**
     * 应用发布分支
     **/
    private String appBranch;

    /**
     * 生成的btag
     **/
    private String appBtag;

    /*测试环境profile*/
    private String profile;

    /*只deploy不部署*/
    private Integer isDeploy;

    /**
     * 发布状态
     **/
    private Integer deployStatus;

    /*发布子状态*/
    private Integer subDeployStatus;

    /**
     * 应用ip
     **/
    private String serviceIps;

    /*发布记录日志地址*/
    private String logPath;

    /*发布者*/
    private String publishUser;

    /*中断者*/
    private String interruptUser;
    /**
     * 应用所属业务 blg，missfresh，mryt
     **/
    private String bizLine;

    /**
     * docker环境
     **/
    private String dockerEnv;

    /**
     * 是否Dock部署 0:否 1是
     **/
    private Integer isDockerDeploy;

    /**
     * 是否压测 0：否 1是
     */
    private Integer isStress;

    /**
     * 创建时间
     **/
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /**
     * 更新时间
     **/
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    /**
     * 1:正常|0:删除
     **/
    private Integer delFlag;

    /**
     * 查询方式，如果为"exact"则为精确搜索
     */
    private String mode;

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return this.id;
    }

    public void setProjectId(Integer projectId) {
        this.projectId = projectId;
    }

    public Integer getProjectId() {
        return this.projectId;
    }

    public void setProjectTaskId(Integer projectTaskId) {
        this.projectTaskId = projectTaskId;
    }

    public Integer getProjectTaskId() {
        return this.projectTaskId;
    }

    public void setAppCode(String appCode) {
        this.appCode = appCode;
    }

    public String getAppCode() {
        return this.appCode;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getAppName() {
        return this.appName;
    }

    public void setAppBranch(String appBranch) {
        this.appBranch = appBranch;
    }

    public String getAppBranch() {
        return this.appBranch;
    }

    public void setAppBtag(String appBtag) {
        this.appBtag = appBtag;
    }

    public String getAppBtag() {
        return this.appBtag;
    }

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }

    public Integer getIsDeploy() {
        return isDeploy;
    }

    public void setIsDeploy(Integer isDeploy) {
        this.isDeploy = isDeploy;
    }

    public void setDeployStatus(Integer deployStatus) {
        this.deployStatus = deployStatus;
    }

    public Integer getDeployStatus() {
        return this.deployStatus;
    }

    public Integer getSubDeployStatus() {
        return subDeployStatus;
    }

    public void setSubDeployStatus(Integer subDeployStatus) {
        this.subDeployStatus = subDeployStatus;
    }

    public void setServiceIps(String serviceIps) {
        this.serviceIps = serviceIps;
    }

    public String getServiceIps() {
        return this.serviceIps;
    }

    public String getLogPath() {
        return logPath;
    }

    public void setLogPath(String logPath) {
        this.logPath = logPath;
    }

    public String getPublishUser() {
        return publishUser;
    }

    public void setPublishUser(String publishUser) {
        this.publishUser = publishUser;
    }

    public String getInterruptUser() {
        return interruptUser;
    }

    public void setInterruptUser(String interruptUser) {
        this.interruptUser = interruptUser;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getCreateTime() {
        return this.createTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public Date getUpdateTime() {
        return this.updateTime;
    }

    public void setDelFlag(Integer delFlag) {
        this.delFlag = delFlag;
    }

    public Integer getDelFlag() {
        return this.delFlag;
    }

    public String getBizLine() {
        return bizLine;
    }

    public void setBizLine(String bizLine) {
        this.bizLine = bizLine;
    }

    public String getDockerEnv() {
        return dockerEnv;
    }

    public void setDockerEnv(String dockerEnv) {
        this.dockerEnv = dockerEnv;
    }

    public Integer getIsDockerDeploy() {
        return isDockerDeploy;
    }

    public void setIsDockerDeploy(Integer isDockerDeploy) {
        this.isDockerDeploy = isDockerDeploy;
    }

    public Integer getIsStress() {
        return isStress;
    }

    public void setIsStress(Integer isStress) {
        this.isStress = isStress;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    @Override
    public String toString() {
        return "BetaDelpoyRecord{" +
                "id=" + id +
                ", projectId=" + projectId +
                ", projectTaskId=" + projectTaskId +
                ", appCode='" + appCode + '\'' +
                ", appName='" + appName + '\'' +
                ", appBranch='" + appBranch + '\'' +
                ", appBtag='" + appBtag + '\'' +
                ", profile='" + profile + '\'' +
                ", isDeploy=" + isDeploy +
                ", deployStatus=" + deployStatus +
                ", subDeployStatus=" + subDeployStatus +
                ", serviceIps='" + serviceIps + '\'' +
                ", logPath='" + logPath + '\'' +
                ", publishUser='" + publishUser + '\'' +
                ", interruptUser='" + interruptUser + '\'' +
                ", bizLine='" + bizLine + '\'' +
                ", dockerEnv='" + dockerEnv + '\'' +
                ", isDockerDeploy=" + isDockerDeploy +
                ", isStress=" + isStress +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                ", delFlag=" + delFlag +
                ", mode='" + mode + '\'' +
                ", " + super.toString() +
                '}';
    }
}
