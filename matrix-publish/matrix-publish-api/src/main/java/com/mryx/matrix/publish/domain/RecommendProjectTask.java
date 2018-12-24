package com.mryx.matrix.publish.domain;


import java.util.List;

/**
 * @author pengcheng
 * @description 推荐发布计划接口应用VO
 * @email pengcheng@missfresh.cn
 * @date 2018-10-24 17:21
 **/
public class RecommendProjectTask {


    /**
     * 应用ID
     */
    private Integer id;

    private Integer projectId = 0;  // 项目ID

    private String appCode; // app code

    private String appBtag; // 上线btag

    private String appRtag; // 发布rtag

    private List<GroupInfo> groupInfo; // 发布组信息

    private Integer waitTime = 0; // 批次等待时间

    private String serviceIps; // 应用发布IP

    private Integer deployStatus = 0; // 发布状态


    /**
     * 部署状态
     */
    private Integer releaseTaskStatus;

    private Integer disable = 0; // 禁用标识

    private Integer delFlag = 0; // 删除标识

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

    public String getAppCode() {
        return appCode;
    }

    public void setAppCode(String appCode) {
        this.appCode = appCode;
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

    public List<GroupInfo> getGroupInfo() {
        return groupInfo;
    }

    public void setGroupInfo(List<GroupInfo> groupInfo) {
        this.groupInfo = groupInfo;
    }

    public Integer getWaitTime() {
        return waitTime;
    }

    public void setWaitTime(Integer waitTime) {
        this.waitTime = waitTime;
    }

    public String getServiceIps() {
        return serviceIps;
    }

    public void setServiceIps(String serviceIps) {
        this.serviceIps = serviceIps;
    }

    public Integer getDeployStatus() {
        return deployStatus;
    }

    public void setDeployStatus(Integer deployStatus) {
        this.deployStatus = deployStatus;
    }

    public Integer getReleaseTaskStatus() {
        return releaseTaskStatus;
    }

    public void setReleaseTaskStatus(Integer releaseTaskStatus) {
        this.releaseTaskStatus = releaseTaskStatus;
    }

    public Integer getDisable() {
        return disable;
    }

    public void setDisable(Integer disable) {
        this.disable = disable;
    }

    public Integer getDelFlag() {
        return delFlag;
    }

    public void setDelFlag(Integer delFlag) {
        this.delFlag = delFlag;
    }

    @Override
    public String toString() {
        return "RecommendProjectTask{" +
                "id=" + id +
                ", projectId=" + projectId +
                ", appCode='" + appCode + '\'' +
                ", appBtag='" + appBtag + '\'' +
                ", appRtag='" + appRtag + '\'' +
                ", groupInfo=" + groupInfo +
                ", waitTime=" + waitTime +
                ", serviceIps='" + serviceIps + '\'' +
                ", deployStatus=" + deployStatus +
                ", releaseTaskStatus=" + releaseTaskStatus +
                ", disable=" + disable +
                ", delFlag=" + delFlag +
                '}';
    }
}
