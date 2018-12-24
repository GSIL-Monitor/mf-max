package com.mryx.matrix.publish.web.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.mryx.matrix.publish.domain.DeployPlan;
import com.mryx.matrix.publish.domain.PlanMachineMapping;
import com.mryx.matrix.publish.enums.PublishStatusEnum;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 发布计划前端vo
 *
 * @author dinglu
 * @date 2018/10/19
 */
public class DeployPlanVo implements Serializable {

    private static final long serialVersionUID = 1515421477817700547L;

    /**
     * 发布计划ID
     */
    private Integer id;

    /**
     * 项目ID
     */
    private Integer projectId;

    /**
     * 应用ID
     */
    private Integer projectTaskId;

    /**
     * app code
     */
    private String appCode;

    /**
     * 发布顺序
     */
    private Integer sequenece;

    /**
     * 上线btag
     */
    private String appBtag;

    /**
     * 发布生成rtag
     */
    private String appRtag;

    /**
     * 机器数量
     */
    private Integer machineCount;

    /**
     * 发布组信息
     */
    private String appGroup;

    /**
     * 批次等待时间
     */
    private Integer waitTime;

    /**
     * 应用发布IP
     */
    private String serviceIps;

    /**
     * 发布状态
     */
    private Integer deployStatus;

    /**
     * 发布状态描述
     */
    private String deployStatusDesc;

    /**
     * 发布子状态
     */
    private Integer subDeployStatus;

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
     * 关联机器信息
     */
    private List<PlanMachineMappingVo> planMachineMappings;

    /**
     * 回滚标志,1:回滚记录|0:发布记录
     */
    private Integer rollbackFlag;

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

    public Integer getProjectTaskId() {
        return projectTaskId;
    }

    public void setProjectTaskId(Integer projectTaskId) {
        this.projectTaskId = projectTaskId;
    }

    public String getAppCode() {
        return appCode;
    }

    public void setAppCode(String appCode) {
        this.appCode = appCode;
    }

    public Integer getSequenece() {
        return sequenece;
    }

    public void setSequenece(Integer sequenece) {
        this.sequenece = sequenece;
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

    public List<PlanMachineMappingVo> getPlanMachineMappings() {
        return planMachineMappings;
    }

    public void setPlanMachineMappings(List<PlanMachineMappingVo> planMachineMappings) {
        this.planMachineMappings = planMachineMappings;
    }

    public Integer getRollbackFlag() {
        return rollbackFlag;
    }

    public void setRollbackFlag(Integer rollbackFlag) {
        this.rollbackFlag = rollbackFlag;
    }

    @Override
    public String toString() {
        return "DeployPlanVo{" +
                "id=" + id +
                ", projectId=" + projectId +
                ", projectTaskId=" + projectTaskId +
                ", appCode='" + appCode + '\'' +
                ", sequenece=" + sequenece +
                ", appBtag='" + appBtag + '\'' +
                ", appRtag='" + appRtag + '\'' +
                ", machineCount=" + machineCount +
                ", appGroup='" + appGroup + '\'' +
                ", waitTime=" + waitTime +
                ", serviceIps='" + serviceIps + '\'' +
                ", deployStatus=" + deployStatus +
                ", deployStatusDesc='" + deployStatusDesc + '\'' +
                ", subDeployStatus=" + subDeployStatus +
                ", disable=" + disable +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                ", delFlag=" + delFlag +
                ", planMachineMappings=" + planMachineMappings +
                ", rollbackFlag=" + rollbackFlag +
                '}';
    }
}
