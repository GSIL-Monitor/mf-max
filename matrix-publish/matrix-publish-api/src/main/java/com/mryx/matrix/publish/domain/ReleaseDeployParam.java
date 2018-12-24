package com.mryx.matrix.publish.domain;

import com.mryx.matrix.common.dto.GroupInfoDto;
import com.mryx.matrix.common.dto.ServerResourceDto;

import java.io.Serializable;
import java.util.List;

/**
 *
 * @author dinglu
 * @date 2018/9/12
 */
public class ReleaseDeployParam extends CommonDeployParam implements Serializable{
    private static final long serialVersionUID = 3181491202421636775L;

    /*发布记录ID*/
    private Integer record;

    /*发布批次ID*/
    private Integer batch;

    /*发布计划记录ID*/
    private Integer plan;

    /*等待时间*/
    private Integer waitTime;

    private String appGroup;

    public Integer getRecord() {
        return record;
    }

    public void setRecord(Integer record) {
        this.record = record;
    }

    public Integer getBatch() {
        return batch;
    }

    public void setBatch(Integer batch) {
        this.batch = batch;
    }

    public Integer getPlan() {
        return plan;
    }

    public void setPlan(Integer plan) {
        this.plan = plan;
    }

    public Integer getWaitTime() {
        return waitTime;
    }

    public void setWaitTime(Integer waitTime) {
        this.waitTime = waitTime;
    }

    public String getAppGroup() {
        return appGroup;
    }

    public void setAppGroup(String appGroup) {
        this.appGroup = appGroup;
    }

    @Override
    public String toString() {
        return "ReleaseDeployParam{" +
                "record=" + record +
                ", batch=" + batch +
                ", plan=" + plan +
                ", waitTime=" + waitTime +
                ", appGroup='" + appGroup + '\'' + super.toString() +
                '}';
    }
}
