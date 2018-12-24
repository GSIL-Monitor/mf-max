package com.mryx.matrix.publish.web.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.mryx.matrix.publish.domain.AppServer;
import lombok.Data;

import java.util.Date;

/**
 * @author pengcheng
 * @description
 * @email pengcheng@missfresh.cn
 * @date 2018-12-04 19:07
 **/
@Data
public class AppServerVo extends AppServer {

    /**
     * 服务器IP
     */
    private String ip = super.getHostIp();

    /**
     * java 版本信息
     */
    private String java;

    /**
     * CPU性能信息
     */
    private String cpu;

    /**
     * CPU负载信息
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
     * 操作系统信息
     */
    private String os;

    /**
     * agent状态描述
     */
    private String agentStatusDesc;

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

    @Override
    public String toString() {
        return "AppServerVo{" +
                "ip='" + ip + '\'' +
                ", java='" + java + '\'' +
                ", cpu='" + cpu + '\'' +
                ", load='" + load + '\'' +
                ", mem='" + mem + '\'' +
                ", disk='" + disk + '\'' +
                ", os='" + os + '\'' +
                ", agentStatusDesc='" + agentStatusDesc + '\'' +
                ", agentCreateTime='" + agentCreateTime + '\'' +
                ", agentStartTime='" + agentStartTime + '\'' +
                ", " + super.toString() +
                '}';
    }
}
