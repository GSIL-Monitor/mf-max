package com.mryx.matrix.publish.dto;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.mryx.matrix.common.domain.Page;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * Agent 上报数据
 *
 * @author supeng
 * @date 2018/09/16
 */
@Data
public class AgentDTO extends Page implements Serializable {

    public AgentDTO() {
    }

    @Override
    public String toString() {
        return "AgentDTO{" +
                "appCode='" + appCode + '\'' +
                ", appName='" + appName + '\'' +
                ", deptId=" + deptId +
                ", deptName='" + deptName + '\'' +
                ", agentStatus='" + agentStatus + '\'' +
                ", ip='" + ip + '\'' +
                ", java='" + java + '\'' +
                ", cpu='" + cpu + '\'' +
                ", load='" + load + '\'' +
                ", mem='" + mem + '\'' +
                ", disk='" + disk + '\'' +
                ", os='" + os + '\'' +
                ", agentCreateTime='" + agentCreateTime + '\'' +
                ", agentStartTime='" + agentStartTime + '\'' +
                ", agentUpdateTime=" + agentUpdateTime +
                ", " + super.toString() +
                '}';
    }

    public AgentDTO(String appCode, String appName, Long deptId, String deptName, String agentStatus, String ip, String java, String cpu, String load, String mem, String disk, String os, Date updateTime) {
        this.appCode = appCode;
        this.appName = appName;
        this.deptId = deptId;
        this.deptName = deptName;
        this.agentStatus = agentStatus;
        this.ip = ip;
        this.java = java;
        this.cpu = cpu;
        this.load = load;
        this.mem = mem;
        this.disk = disk;
        this.os = os;
    }

    /**
     * 应用编码
     */
    private String appCode;
    /**
     * 应用名称
     */
    private String appName;
    /**
     * 部门ID
     */
    private Long deptId;
    /**
     * 部门名称
     */
    private String deptName;
    /**
     * Agent状态
     */
    private String agentStatus;
    /**
     * 机器IP
     */
    private String ip;
    /**
     * Java环境
     */
    private String java;
    /**
     * CPU信息
     */
    private String cpu;
    /**
     * load信息
     */
    private String load;
    /**
     * 内存信息
     */
    private String mem;
    /**
     * 磁盘信息
     */
    private String disk;
    /**
     * 系统信息
     */
    private String os;

    /**
     * agent创建时间
     */
    private String agentCreateTime;

    /**
     * agent启动时间
     */
    private String agentStartTime;

    /**
     * 最新上报时间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date agentUpdateTime;

}
