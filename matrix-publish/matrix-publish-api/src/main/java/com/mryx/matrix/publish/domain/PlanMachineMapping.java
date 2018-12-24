package com.mryx.matrix.publish.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.mryx.matrix.common.domain.Page;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 发布计划关联机器信息
 *
 * @author dinglu
 * @date 2018/10/19
 */
@Data
public class PlanMachineMapping extends Page implements Serializable {

    private static final long serialVersionUID = -6747759230194973581L;

    /**
     * 主键ID
     */
    private Integer id;

    /**
     * 发布计划ID
     */
    private Integer planId;

    /**
     * 项目ID
     */
    private Integer projectId;

    /**
     * 应用ID
     */
    private Integer projectTaskId;

    /**
     * 应用ip
     */
    private String serviceIps;

    /**
     * 机器名称
     */
    private String hostName;

    /**
     * 机器中文名称
     */
    private String hostNameCn;

    /**
     * 机器组信息
     */
    private String appGroup;

    /**
     * 机器部署tag信息
     */
    private String appTag;

    /**
     * 发布状态
     */
    private Integer deployStatus;

    /**
     * 禁用标识
     */
    private Integer disable;

    /**
     * 创建时间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /**
     * 更新时间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    /**
     * 删除标识
     */
    private Integer delFlag;

    /**
     * 回滚标识,1:回滚记录|0:发布记录
     */
    private Integer rollbackFlag;

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

    public String getServiceIps() {
        return serviceIps;
    }

    public void setServiceIps(String serviceIps) {
        this.serviceIps = serviceIps;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public String getHostNameCn() {
        return hostNameCn;
    }

    public void setHostNameCn(String hostNameCn) {
        this.hostNameCn = hostNameCn;
    }

    public String getAppGroup() {
        return appGroup;
    }

    public void setAppGroup(String appGroup) {
        this.appGroup = appGroup;
    }

    public String getAppTag() {
        return appTag;
    }

    public void setAppTag(String appTag) {
        this.appTag = appTag;
    }

    public Integer getDeployStatus() {
        return deployStatus;
    }

    public void setDeployStatus(Integer deployStatus) {
        this.deployStatus = deployStatus;
    }

    public Integer getDisable() {
        return disable;
    }

    public void setDisable(Integer disable) {
        this.disable = disable;
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
        return "PlanMachineMapping{" +
                "id=" + id +
                ", planId=" + planId +
                ", projectId=" + projectId +
                ", projectTaskId=" + projectTaskId +
                ", serviceIps='" + serviceIps + '\'' +
                ", hostName='" + hostName + '\'' +
                ", hostNameCn='" + hostNameCn + '\'' +
                ", appGroup='" + appGroup + '\'' +
                ", appTag='" + appTag + '\'' +
                ", deployStatus=" + deployStatus +
                ", disable=" + disable +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                ", delFlag=" + delFlag +
                ", rollbackFlag=" + rollbackFlag + ", " + super.toString() +
                '}';
    }
}
