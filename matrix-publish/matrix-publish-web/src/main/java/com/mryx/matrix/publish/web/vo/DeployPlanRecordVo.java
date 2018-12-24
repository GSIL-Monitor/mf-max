package com.mryx.matrix.publish.web.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.mryx.matrix.publish.domain.DeployPlanRecord;
import com.mryx.matrix.publish.enums.PublishStatusEnum;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.util.Date;

/**
 *发布计划发布记录前端vo
 * @author dinglu
 * @date 2018/10/19
 */
public class DeployPlanRecordVo implements Serializable {

    private static final long serialVersionUID = 1953712139403071139L;
    /*发布计划ID*/
    private Integer id;

    /*发布计划ID*/
    private Integer planId;

    /*项目ID*/
    private Integer projectId;

    /*应用ID*/
    private Integer projectTaskId;

    /*发布记录ID*/
    private Integer deployRecordId;

    /*发布顺序*/
    private Integer sequenece;

    /*上一发布计划发布记录ID*/
    private Integer forwardId;

    /*上线btag*/
    private String appBtag;

    /*发布生成rtag*/
    private String appRtag;

    /*机器数量*/
    private Integer machineCount;

    /*发布组信息*/
    private String appGroup;

    /*批次等待时间*/
    private Integer waitTime;

    /*应用发布IP*/
    private String serviceIps;

    /*发布状态*/
    private Integer deployStatus;

    /*发布状态描述*/
    private String deployStatusDesc;

    /*发布子状态*/
    private Integer subDeployStatus;

    /*创建时间*/
    @JsonFormat(timezone = "GMT+8" ,pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /*更新时间*/
    @JsonFormat(timezone = "GMT+8" ,pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    /*删除标识*/
    private Integer delFlag;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getPlanId() {
        return planId;
    }

    public void setPlanId(Integer planId) {
        this.planId = planId;
    }

    public Integer getProjectId() {
        return projectId;
    }

    public void setProjectId(Integer projectId) {
        this.projectId = projectId;
    }

    public Integer getProjectTaskId() {
        return projectTaskId;
    }

    public void setProjectTaskId(Integer projectTaskId) {
        this.projectTaskId = projectTaskId;
    }

    public Integer getDeployRecordId() {
        return deployRecordId;
    }

    public void setDeployRecordId(Integer deployRecordId) {
        this.deployRecordId = deployRecordId;
    }

    public Integer getSequenece() {
        return sequenece;
    }

    public void setSequenece(Integer sequenece) {
        this.sequenece = sequenece;
    }

    public Integer getForwardId() {
        return forwardId;
    }

    public void setForwardId(Integer forwardId) {
        this.forwardId = forwardId;
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

    public Integer getMachineCount() {
        return machineCount;
    }

    public void setMachineCount(Integer machineCount) {
        this.machineCount = machineCount;
    }

    public String getAppGroup() {
        return appGroup;
    }

    public void setAppGroup(String appGroup) {
        this.appGroup = appGroup;
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

    @Override
    public String toString() {
        return "DeployPlanRecordVo{" +
                "id=" + id +
                ", planId=" + planId +
                ", projectId=" + projectId +
                ", projectTaskId=" + projectTaskId +
                ", deployRecordId=" + deployRecordId +
                ", sequenece=" + sequenece +
                ", forwardId=" + forwardId +
                ", appBtag='" + appBtag + '\'' +
                ", appRtag='" + appRtag + '\'' +
                ", machineCount=" + machineCount +
                ", appGroup='" + appGroup + '\'' +
                ", waitTime=" + waitTime +
                ", serviceIps='" + serviceIps + '\'' +
                ", deployStatus=" + deployStatus +
                ", deployStatusDesc='" + deployStatusDesc + '\'' +
                ", subDeployStatus=" + subDeployStatus +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                ", delFlag=" + delFlag +
                '}';
    }
}
