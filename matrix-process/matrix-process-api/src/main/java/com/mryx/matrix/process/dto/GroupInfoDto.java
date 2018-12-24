package com.mryx.matrix.process.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 分组信息
 *
 * @author wangwenbo
 * @create 2018-09-13 15:53
 **/
@Data
public class GroupInfoDto implements Serializable{

    /**
     * 默认空
     */
    private String groupName;

    private List<ServerResourceDto> serverInfo;


}
