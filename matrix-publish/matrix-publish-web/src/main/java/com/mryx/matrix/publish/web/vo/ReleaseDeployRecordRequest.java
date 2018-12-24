package com.mryx.matrix.publish.web.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.mryx.matrix.common.domain.Page;

import java.io.Serializable;
import java.util.Date;

/**
 * @author dinglu
 * @date 2018/9/18
 */
public class ReleaseDeployRecordRequest extends Page implements Serializable {

    private static final long serialVersionUID = 8534202133974294918L;

    /**
     * 生产环境部署记录id
     **/
    private Integer id;

    /**
     * 项目ID
     **/
    private String projectId;

    /**
     * 项目发布工单ID
     **/
    private String projectTaskId;

    /*依赖的上一条发布ID*/
    private String forwardRecordId;

    /**
     * 应用标识
     **/
    private String appCode;

    /**
     * 应用名称
     **/
    private String appName;

    /**
     * 应用分支
     **/
    private String appBranch;

    /**
     * 要发布的btag
     **/
    private String appBtag;

    /**
     * 发布完成的rtag
     **/
    private String appRtag;

    /**
     * 应用状态
     **/
    private Integer deployStatus;

    /*发布状态描述*/
    private String deployStatusDesc;

    /**
     * 应用ip
     **/
    private String serviceIps;

    /*发布者*/
    private String publishUser;

    /*日志地址*/
    private String logPath;

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
     * 检索标识，如果为exact代表精确检索
     */
    private String mode;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getProjectTaskId() {
        return projectTaskId;
    }

    public void setProjectTaskId(String projectTaskId) {
        this.projectTaskId = projectTaskId;
    }

    public String getForwardRecordId() {
        return forwardRecordId;
    }

    public void setForwardRecordId(String forwardRecordId) {
        this.forwardRecordId = forwardRecordId;
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

    public String getAppRtag() {
        return appRtag;
    }

    public void setAppRtag(String appRtag) {
        this.appRtag = appRtag;
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

    public String getServiceIps() {
        return serviceIps;
    }

    public void setServiceIps(String serviceIps) {
        this.serviceIps = serviceIps;
    }

    public String getPublishUser() {
        return publishUser;
    }

    public void setPublishUser(String publishUser) {
        this.publishUser = publishUser;
    }

    public String getLogPath() {
        return logPath;
    }

    public void setLogPath(String logPath) {
        this.logPath = logPath;
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
        return "ReleaseDeployRecordRequest{" +
                "id=" + id +
                ", projectId='" + projectId + '\'' +
                ", projectTaskId='" + projectTaskId + '\'' +
                ", forwardRecordId='" + forwardRecordId + '\'' +
                ", appCode='" + appCode + '\'' +
                ", appName='" + appName + '\'' +
                ", appBranch='" + appBranch + '\'' +
                ", appBtag='" + appBtag + '\'' +
                ", appRtag='" + appRtag + '\'' +
                ", deployStatus=" + deployStatus +
                ", deployStatusDesc='" + deployStatusDesc + '\'' +
                ", serviceIps='" + serviceIps + '\'' +
                ", publishUser='" + publishUser + '\'' +
                ", logPath='" + logPath + '\'' +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                ", delFlag=" + delFlag +
                ", mode='" + mode + '\'' +
                '}';
    }
}
