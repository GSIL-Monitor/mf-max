package com.mryx.matrix.publish.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * Node
 *
 * @author supeng
 * @date 2018/10/28
 */
@Data
public class NodeDTO implements Serializable {
    /**
     * IP
     */
    private String ip;
    /**
     * 后端服务端口
     */
    private Integer backendPort;
    /**
     * 处理类型 LbType.name()
     */
    private String type;
    /**
     * 操作 LbAction.name()
     */
    private String action;
}
