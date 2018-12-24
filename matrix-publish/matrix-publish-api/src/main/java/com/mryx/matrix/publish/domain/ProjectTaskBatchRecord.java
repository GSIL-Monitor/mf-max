package com.mryx.matrix.publish.domain;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import com.mryx.matrix.common.domain.Page;


/**
 * 发布记录
 *
 * @author supeng
 * @date 2018/09/03
 */
@Data
public class ProjectTaskBatchRecord extends Page implements Serializable{
    private static final long serialVersionUID = -3015106929724723245L;

    /*主键ID*/
    private Integer id;

    /*应用发布工单ID*/
    private Integer projectTaskId;

    /*发布批次ID*/
    private Integer projectTaskBatchId;

    /*发布记录ID*/
    private Integer deployRecordId;

    /*上一发布批次ID*/
    private Integer forwardBatchId;

    /*发布顺序*/
    private Integer sequenece;

    /*发布状态*/
    private Integer deployStatus;

    /*发布子状态*/
    private Integer subDeployStatus;

    /*机器数*/
    private Integer machineCount;

    /*等待时间*/
    private Integer waitTime;

    /*机器ips*/
    private String serviceIps;

    /*应用组*/
    private String appGroup;

    /*创建时间*/
    private Date createTime;

    /*更新时间*/
    private Date updateTime;

    /*删除标识*/
    private Integer delFlag;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getProjectTaskId() {
        return projectTaskId;
    }

    public void setProjectTaskId(Integer projectTaskId) {
        this.projectTaskId = projectTaskId;
    }

    public Integer getProjectTaskBatchId() {
        return projectTaskBatchId;
    }

    public void setProjectTaskBatchId(Integer projectTaskBatchId) {
        this.projectTaskBatchId = projectTaskBatchId;
    }

    public Integer getDeployRecordId() {
        return deployRecordId;
    }

    public void setDeployRecordId(Integer deployRecordId) {
        this.deployRecordId = deployRecordId;
    }

    public Integer getForwardBatchId() {
        return forwardBatchId;
    }

    public void setForwardBatchId(Integer forwardBatchId) {
        this.forwardBatchId = forwardBatchId;
    }

    public Integer getSequenece() {
        return sequenece;
    }

    public void setSequenece(Integer sequenece) {
        this.sequenece = sequenece;
    }

    public Integer getDeployStatus() {
        return deployStatus;
    }

    public void setDeployStatus(Integer deployStatus) {
        this.deployStatus = deployStatus;
    }

    public Integer getSubDeployStatus() {
        return subDeployStatus;
    }

    public void setSubDeployStatus(Integer subDeployStatus) {
        this.subDeployStatus = subDeployStatus;
    }

    public Integer getMachineCount() {
        return machineCount;
    }

    public void setMachineCount(Integer machineCount) {
        this.machineCount = machineCount;
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

    public String getAppGroup() {
        return appGroup;
    }

    public void setAppGroup(String appGroup) {
        this.appGroup = appGroup;
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
        return "ProjectTaskBatchRecord{" +
                "id=" + id +
                ", projectTaskId=" + projectTaskId +
                ", projectTaskBatchId=" + projectTaskBatchId +
                ", deployRecordId=" + deployRecordId +
                ", forwardBatchId=" + forwardBatchId +
                ", sequenece=" + sequenece +
                ", deployStatus=" + deployStatus +
                ", subDeployStatus=" + subDeployStatus +
                ", machineCount=" + machineCount +
                ", waitTime=" + waitTime +
                ", serviceIps='" + serviceIps + '\'' +
                ", appGroup='" + appGroup + '\'' +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                ", delFlag=" + delFlag +
                '}';
    }
}
