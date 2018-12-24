package com.mryx.matrix.publish.web.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.mryx.matrix.common.domain.Base;
import com.mryx.matrix.publish.domain.ProjectTaskBatch;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 *
 * @author dinglu
 * @date 2018/9/21
 */
public class ProjectTaskVo extends Base implements Serializable {

    private static final long serialVersionUID = 4650038334318760498L;

    /*应用发布ID*/
    private Integer id;

    /*项目ID*/
    private Integer projectId;

    /*发布顺序*/
    private Integer sequenece;

    /*应用标识*/
    private String appCode;

    /*应用分支*/
    private String appBranch;

    /*发布btag*/
    private String appBtag;

    /*测试环境profile*/
    private String profile;

    /*只deploy不部署*/
    private boolean isDeploy;

    /*应用开发人*/
    private String appDevOwner;

    /*发布生成rtag*/
    private String appRtag;

    /*应用状态*/
    private Integer taskStatus;

    /*发布状态描述*/
    private String taskStatusDesc;

    /*beta发布子状态*/
    private Integer subTaskStatus;

    /*生产发布状态*/
    private Integer releaseTaskStatus;

    /*生产发布子状态*/
    private Integer subReleaseTaskStatus;

    /*生产发布状态描述*/
    private String releaseTaskStatusDesc;

    /*应用ip*/
    private String serviceIps;

    /**应用所属业务 blg，missfresh，mryt**/
    private String bizLine;

    /**docker环境**/
    private String dockerEnv;

    /**是否Dock部署**/
    private Boolean isDockerDeploy;

    /*创建时间*/
    @JsonFormat(timezone = "GMT+8" ,pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /*更新时间*/
    @JsonFormat(timezone = "GMT+8" ,pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    /*删除状态*/
    private Integer delFlag;

    /*发布批次*/
    private List<ProjectTaskBatch> projectTaskBatchList;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getProjectId() {
        return projectId;
    }

    public void setProjectId(Integer projectId) {
        this.projectId = projectId;
    }

    public Integer getSequenece() {
        return sequenece;
    }

    public void setSequenece(Integer sequenece) {
        this.sequenece = sequenece;
    }

    public String getAppCode() {
        return appCode;
    }

    public void setAppCode(String appCode) {
        this.appCode = appCode;
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

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }

    public boolean getIsDeploy() {
        return isDeploy;
    }

    public void setIsDeploy(boolean deploy) {
        isDeploy = deploy;
    }

    public String getAppDevOwner() {
        return appDevOwner;
    }

    public void setAppDevOwner(String appDevOwner) {
        this.appDevOwner = appDevOwner;
    }

    public String getAppRtag() {
        return appRtag;
    }

    public void setAppRtag(String appRtag) {
        this.appRtag = appRtag;
    }

    public Integer getTaskStatus() {
        return taskStatus;
    }

    public void setTaskStatus(Integer taskStatus) {
        this.taskStatus = taskStatus;
    }

    public String getTaskStatusDesc() {
        return taskStatusDesc;
    }

    public void setTaskStatusDesc(String taskStatusDesc) {
        this.taskStatusDesc = taskStatusDesc;
    }

    public Integer getSubTaskStatus() {
        return subTaskStatus;
    }

    public void setSubTaskStatus(Integer subTaskStatus) {
        this.subTaskStatus = subTaskStatus;
    }

    public Integer getReleaseTaskStatus() {
        return releaseTaskStatus;
    }

    public void setReleaseTaskStatus(Integer releaseTaskStatus) {
        this.releaseTaskStatus = releaseTaskStatus;
    }

    public Integer getSubReleaseTaskStatus() {
        return subReleaseTaskStatus;
    }

    public void setSubReleaseTaskStatus(Integer subReleaseTaskStatus) {
        this.subReleaseTaskStatus = subReleaseTaskStatus;
    }

    public String getReleaseTaskStatusDesc() {
        return releaseTaskStatusDesc;
    }

    public void setReleaseTaskStatusDesc(String releaseTaskStatusDesc) {
        this.releaseTaskStatusDesc = releaseTaskStatusDesc;
    }

    public String getServiceIps() {
        return serviceIps;
    }

    public void setServiceIps(String serviceIps) {
        this.serviceIps = serviceIps;
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

    public List<ProjectTaskBatch> getProjectTaskBatchList() {
        return projectTaskBatchList;
    }

    public void setProjectTaskBatchList(List<ProjectTaskBatch> projectTaskBatchList) {
        this.projectTaskBatchList = projectTaskBatchList;
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

    public Boolean getIsDockerDeploy() {
        return isDockerDeploy;
    }

    public void setIsDockerDeploy(Boolean isDockerDeploy) {
        this.isDockerDeploy = isDockerDeploy;
    }

    @Override
    public String toString() {
        return "ProjectTaskVo{" +
                "id=" + id +
                ", projectId=" + projectId +
                ", sequenece=" + sequenece +
                ", appCode='" + appCode + '\'' +
                ", appBranch='" + appBranch + '\'' +
                ", appBtag='" + appBtag + '\'' +
                ", profile='" + profile + '\'' +
                ", isDeploy=" + isDeploy +
                ", appDevOwner='" + appDevOwner + '\'' +
                ", appRtag='" + appRtag + '\'' +
                ", taskStatus=" + taskStatus +
                ", taskStatusDesc='" + taskStatusDesc + '\'' +
                ", subTaskStatus=" + subTaskStatus +
                ", releaseTaskStatus=" + releaseTaskStatus +
                ", subReleaseTaskStatus=" + subReleaseTaskStatus +
                ", releaseTaskStatusDesc='" + releaseTaskStatusDesc + '\'' +
                ", serviceIps='" + serviceIps + '\'' +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                ", delFlag=" + delFlag +
                ", projectTaskBatchList=" + projectTaskBatchList +
                ", isDockerDeploy=" + isDockerDeploy +
                ", dockerEnv=" + dockerEnv +
                ", bizLine=" + bizLine +
                '}';
    }
}
