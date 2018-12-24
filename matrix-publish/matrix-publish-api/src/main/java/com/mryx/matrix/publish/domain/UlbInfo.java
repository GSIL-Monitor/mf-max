package com.mryx.matrix.publish.domain;

import com.mryx.matrix.common.domain.Page;
import lombok.Data;

import java.util.Date;

/**
 * UlbInfo
 *
 * @author supeng
 * @date 2018/11/04
 */
@Data
public class UlbInfo extends Page {
    /**
     * ID
     */
    private Integer id;
    /**
     * 名称
     */
    private String ulbName;
    /**
     * ULB内网IP
     */
    private String privateIp;
    /**
     * ULB外网IP
     */
    private String eip;
    /**
     *  Backend IP
     */
    private String ip;
    /**
     * Backend 端口
     */
    private Integer backendPort;
    /**
     * ULB ID
     */
    private String ulbId;
    /**
     * Backend ID
     */
    private String backendId;
    /**
     * 创建时间
     */
    private Date createTime;
    /**
     * 更新时间
     */
    private Date updateTime;
}
