package com.mryx.matrix.publish.web.vo;

import java.io.Serializable;

/**
 *
 * @author dinglu
 * @date 2018/9/17
 */
public class PublishBatchVo implements Serializable {

    private static final long serialVersionUID = 8545391204878330373L;

    /*发布批次ID*/
    private Integer id;

    /**发布顺序**/
    private Integer sequenece;

    /*发布机器数量*/
    private Integer machineCount;

    /*批次间等待时间*/
    private Integer waitTime;

    /*批次发布机器*/
    private String serviceIps;

    /*发布组信息*/
    private String appGroup;

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

    @Override
    public String toString() {
        return "PublishBatchVo{" +
                "id=" + id +
                ", sequenece=" + sequenece +
                ", machineCount=" + machineCount +
                ", waitTime=" + waitTime +
                ", serviceIps='" + serviceIps + '\'' +
                ", appGroup='" + appGroup + '\'' +
                '}';
    }
}
