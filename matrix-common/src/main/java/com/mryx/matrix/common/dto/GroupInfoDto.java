package com.mryx.matrix.common.dto;

import java.io.Serializable;
import java.util.List;

/**
 * 分组信息
 *
 * @author wangwenbo
 * @create 2018-09-13 15:53
 **/
public class GroupInfoDto implements Serializable{

    /**
     * 默认空
     */
    private String groupName;

    private List<ServerResourceDto> serverInfo;

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public List<ServerResourceDto> getServerInfo() {
        return serverInfo;
    }

    public void setServerInfo(List<ServerResourceDto> serverInfo) {
        this.serverInfo = serverInfo;
    }
}
