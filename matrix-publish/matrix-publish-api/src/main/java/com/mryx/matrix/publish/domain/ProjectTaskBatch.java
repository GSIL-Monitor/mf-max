package com.mryx.matrix.publish.domain;

import com.mryx.matrix.common.domain.*;

import java.io.Serializable;
import java.util.Date;

/**
 *
 * @author dinglu
 * @date 2018/9/10
 */
public class ProjectTaskBatch extends Page implements Serializable {

    private static final long serialVersionUID = -1828590129556587470L;

    /*发布批次id*/
    private Integer id;

    /*应用发布ID*/
    private Integer projectTaskId;

    /*发布顺序*/
    private Integer sequenece;

    /*发布状态*/
    private Integer deployStatus;

    /*发布子状态*/
    private Integer subDeployStatus;

    /*机器数*/
    private Integer machineCount;

    /*应用组*/
    private String appGroup;

    /*等待时间*/
    private Integer waitTime;

    /*应用ip*/
    private String appIps;

    /*创建时间*/
    private Date createTime;

    /*更新时间*/
    private Date updateTime;

    /*删除状态*/
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

    public String getAppIps() {
        return appIps;
    }

    public void setAppIps(String appIps) {
        this.appIps = appIps;
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
        return "ProjectTaskBatch{" +
                "id=" + id +
                ", projectTaskId=" + projectTaskId +
                ", sequenece=" + sequenece +
                ", deployStatus=" + deployStatus +
                ", subDeployStatus=" + subDeployStatus +
                ", machineCount=" + machineCount +
                ", appGroup='" + appGroup + '\'' +
                ", waitTime=" + waitTime +
                ", appIps='" + appIps + '\'' +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                ", delFlag=" + delFlag +
                '}';
    }
}
