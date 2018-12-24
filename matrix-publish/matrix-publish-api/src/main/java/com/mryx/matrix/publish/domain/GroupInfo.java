package com.mryx.matrix.publish.domain;


import java.util.List;

/**
 * @author pengcheng
 * @description
 * @email pengcheng@missfresh.cn
 * @date 2018-10-24 17:45
 **/
public class GroupInfo {

    private String groupName;
    private List<ServerInfo> serverInfo;

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public List<ServerInfo> getServerInfo() {
        return serverInfo;
    }

    public void setServerInfo(List<ServerInfo> serverInfo) {
        this.serverInfo = serverInfo;
    }

    @Override
    public String toString() {
        return "GroupInfo{" +
                "groupName='" + groupName + '\'' +
                ", serverInfo=" + serverInfo +
                '}';
    }
}
