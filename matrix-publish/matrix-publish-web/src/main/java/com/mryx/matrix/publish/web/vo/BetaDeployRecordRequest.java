package com.mryx.matrix.publish.web.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.mryx.matrix.common.domain.Page;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 *
 * @author dinglu
 * @date 2018/11/6
 */
public class BetaDeployRecordRequest extends Page implements Serializable{

    private static final long serialVersionUID = 5865897741550647186L;

    /**测试环境部署记录id**/
    private String id;

    /**项目ID**/
    private String projectId;

    /**项目名称**/
    private String projectName;

    /**应用发布工单ID**/
    private String projectTaskId;

    /**应用标识**/
    private String appCode;

    /**应用名称**/
    private String appName;

    /**应用发布分支**/
    private String appBranch;

    /**生成的btag**/
    private String appBtag;

    /**发布状态**/
    private Integer deployStatus;

    /*发布状态描述*/
    private String deployStatusDesc;

    /*发布子状态*/
    private Integer subDeployStatus;

    /**应用ip**/
    private String serviceIps;

    /*发布记录日志地址*/
    private String logPath;

    /*发布者*/
    private String publishUser;

    /*中断者*/
    private String interruptUser;

    /**创建时间**/
    @JsonFormat(timezone = "GMT+8" ,pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /**更新时间**/
    @JsonFormat(timezone = "GMT+8" ,pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    /**1:正常|0:删除**/
    private Integer delFlag;

    /**
     * 查询模式，如果为extra标识精确查询
     */
    private String mode;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public void setProjectTaskId(String projectTaskId) {
        this.projectTaskId = projectTaskId;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getProjectTaskId() {
        return projectTaskId;
    }

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

    public String getAppBranch() {
        return appBranch;
    }

    public void setAppBranch(String appBranch) {
        this.appBranch = appBranch;
    }

    public String getAppBtag() {
        return appBtag;
    }

    public void setAppBtag(String appBtag) {
        this.appBtag = appBtag;
    }

    public Integer getDeployStatus() {
        return deployStatus;
    }

    public void setDeployStatus(Integer deployStatus) {
        this.deployStatus = deployStatus;
    }

    public String getDeployStatusDesc() {
        return deployStatusDesc;
    }

    public void setDeployStatusDesc(String deployStatusDesc) {
        this.deployStatusDesc = deployStatusDesc;
    }

    public Integer getSubDeployStatus() {
        return subDeployStatus;
    }

    public void setSubDeployStatus(Integer subDeployStatus) {
        this.subDeployStatus = subDeployStatus;
    }

    public String getServiceIps() {
        return serviceIps;
    }

    public void setServiceIps(String serviceIps) {
        this.serviceIps = serviceIps;
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

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public Integer getDelFlag() {
        return delFlag;
    }

    public void setDelFlag(Integer delFlag) {
        this.delFlag = delFlag;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    @Override
    public String toString() {
        return "BetaDeployRecordRequest{" +
                "id='" + id + '\'' +
                ", projectId='" + projectId + '\'' +
                ", projectName='" + projectName + '\'' +
                ", projectTaskId='" + projectTaskId + '\'' +
                ", appCode='" + appCode + '\'' +
                ", appName='" + appName + '\'' +
                ", appBranch='" + appBranch + '\'' +
                ", appBtag='" + appBtag + '\'' +
                ", deployStatus=" + deployStatus +
                ", deployStatusDesc='" + deployStatusDesc + '\'' +
                ", subDeployStatus=" + subDeployStatus +
                ", serviceIps='" + serviceIps + '\'' +
                ", logPath='" + logPath + '\'' +
                ", publishUser='" + publishUser + '\'' +
                ", interruptUser='" + interruptUser + '\'' +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                ", delFlag=" + delFlag +
                ", mode='" + mode + '\'' +
                '}';
    }
}

