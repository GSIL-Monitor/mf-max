package com.mryx.matrix.publish.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.mryx.matrix.common.domain.Page;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author dinglu
 * @date 2018/10/19
 */
/*发布计划发布记录*/
@Data
public class DeployPlanRecord extends Page implements Serializable {

    private static final long serialVersionUID = 264703162789272656L;
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

    /*appCode*/
    private String appCode;

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

    /*发布子状态*/
    private Integer subDeployStatus;

    /*创建时间*/
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /*更新时间*/
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    /*删除标识*/
    private Integer delFlag;

    /**
     * 回滚标志,1:回滚记录|0:发布记录
     */
    private Integer rollbackFlag;

    @Override
    public String toString() {
        return "DeployPlanRecord{" +
                "id=" + id +
                ", planId=" + planId +
                ", projectId=" + projectId +
                ", projectTaskId=" + projectTaskId +
                ", deployRecordId=" + deployRecordId +
                ", sequenece=" + sequenece +
                ", forwardId=" + forwardId +
                ", appCode='" + appCode + '\'' +
                ", appBtag='" + appBtag + '\'' +
                ", appRtag='" + appRtag + '\'' +
                ", machineCount=" + machineCount +
                ", appGroup='" + appGroup + '\'' +
                ", waitTime=" + waitTime +
                ", serviceIps='" + serviceIps + '\'' +
                ", deployStatus=" + deployStatus +
                ", subDeployStatus=" + subDeployStatus +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                ", delFlag=" + delFlag +
                ", rollbackFlag=" + rollbackFlag + ", " + super.toString() +
                '}';
    }
}
