package com.mryx.matrix.common.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 主机信息
 *
 * @author wangwenbo
 * @create 2018-09-20 10:17
 **/
@Data
public class ServerResourceDto implements Serializable {


    private static final long serialVersionUID = 59056010207963969L;

    //主机名称
    private String hostName;
    /**
     * 启动参数
     */
    private String vmOption;
    //主机中文名称
    private String hostNameCn;
    //主机iP
    private String hostIp;
}
