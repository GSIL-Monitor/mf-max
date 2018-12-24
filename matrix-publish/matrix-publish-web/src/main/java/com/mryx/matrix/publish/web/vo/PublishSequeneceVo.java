package com.mryx.matrix.publish.web.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @author dinglu
 * @date 2018/9/10
 */
public class PublishSequeneceVo implements Serializable {


    private static final long serialVersionUID = -8973898657562057941L;

    /*应用发布ID*/
    private Integer id;

    /**
     * 发布顺序
     **/
    private Integer sequenece;

    /**
     * 应用标识
     **/
    private String appCode;

    /**
     * 应用分支
     **/
    private String appBranch;

    /**
     * 要发布的btag
     **/
    private String appBtag;

    /*测试环境profile*/
    private String profile;

    /*只deploy不部署*/
    private boolean isDeploy;

    /**
     * 应用开发人
     **/
    private String appDevOwner;

    /**
     * 发布完成的rtag
     **/
    private String appRtag;

    /**
     * 应用状态
     **/
    private Integer taskStatus;

    /**
     * 应用ip
     **/
    private String serviceIps;

    /**
     * 压测IP，多个IP用英文逗号分割
     */
    private String stressIps;

    /**
     * 应用所属业务 blg，missfresh，mryt
     **/
    private String bizLine;

    /**
     * docker环境
     **/
    private String dockerEnv;

    /**
     * 是否Dock部署
     **/
    private Boolean isDockerDeploy;

    /**
     * 是否压测部署
     */
    private Boolean isStressDeploy;

    private List<PublishBatchVo> publishBatchVoList;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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

    public String getServiceIps() {
        return serviceIps;
    }

    public void setServiceIps(String serviceIps) {
        this.serviceIps = serviceIps;
    }

    public String getStressIps() {
        return stressIps;
    }

    public void setStressIps(String stressIps) {
        this.stressIps = stressIps;
    }

    public List<PublishBatchVo> getPublishBatchVoList() {
        return publishBatchVoList;
    }

    public void setPublishBatchVoList(List<PublishBatchVo> publishBatchVoList) {
        this.publishBatchVoList = publishBatchVoList;
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

    public Boolean getStressDeploy() {
        return isStressDeploy;
    }

    public void setStressDeploy(Boolean stressDeploy) {
        isStressDeploy = stressDeploy;
    }

    @Override
    public String toString() {
        return "PublishSequeneceVo{" +
                "id=" + id +
                ", sequenece=" + sequenece +
                ", appCode='" + appCode + '\'' +
                ", appBranch='" + appBranch + '\'' +
                ", appBtag='" + appBtag + '\'' +
                ", profile='" + profile + '\'' +
                ", isDeploy=" + isDeploy +
                ", appDevOwner='" + appDevOwner + '\'' +
                ", appRtag='" + appRtag + '\'' +
                ", taskStatus=" + taskStatus +
                ", serviceIps='" + serviceIps + '\'' +
                ", stressIps='" + stressIps + '\'' +
                ", bizLine='" + bizLine + '\'' +
                ", dockerEnv='" + dockerEnv + '\'' +
                ", isDockerDeploy=" + isDockerDeploy +
                ", isStressDeploy=" + isStressDeploy +
                ", publishBatchVoList=" + publishBatchVoList +
                '}';
    }
}
