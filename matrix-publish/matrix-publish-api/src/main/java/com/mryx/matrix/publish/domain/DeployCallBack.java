package com.mryx.matrix.publish.domain;

import com.fasterxml.jackson.databind.deser.Deserializers;

import com.mryx.matrix.common.domain.Base;

import java.io.Serializable;

/**
 *
 * @author dinglu
 * @date 2018/9/12
 */
public class DeployCallBack extends Base implements Serializable{

    private static final long serialVersionUID = -5204895211350049739L;

    /*发布记录ID*/
    private String recordId;

    /*发布状态*/
    private String deployStatus;

    /*发布生成tag*/
    private String tag;

    /*发布批次ID*/
    private String batchId;

    /*发布计划ID*/
    private String planId;

    /*发布机器ip*/
    private String ip;

    public String getRecordId() {
        return recordId;
    }

    public void setRecordId(String recordId) {
        this.recordId = recordId;
    }

    public String getDeployStatus() {
        return deployStatus;
    }

    public void setDeployStatus(String deployStatus) {
        this.deployStatus = deployStatus;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getBatchId() {
        return batchId;
    }

    public void setBatchId(String batchId) {
        this.batchId = batchId;
    }

    public String getPlanId() {
        return planId;
    }

    public void setPlanId(String planId) {
        this.planId = planId;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    @Override
    public String toString() {
        return "DeployCallBack{" +
                "recordId='" + recordId + '\'' +
                ", deployStatus='" + deployStatus + '\'' +
                ", tag='" + tag + '\'' +
                ", batchId='" + batchId + '\'' +
                ", planId='" + planId + '\'' +
                ", ip='" + ip + '\'' +
                '}';
    }
}
