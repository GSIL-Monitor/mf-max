package com.mryx.matrix.publish.domain;

/**
 * @author pengcheng
 * @description
 * @email pengcheng@missfresh.cn
 * @date 2018-10-24 17:25
 **/
public class ServerInfo {


    private String hostIp;
    private String hostName;
    private String hostNameCn;

    public String getHostIp() {
        return hostIp;
    }

    public void setHostIp(String hostIp) {
        this.hostIp = hostIp;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public String getHostNameCn() {
        return hostNameCn;
    }

    public void setHostNameCn(String hostNameCn) {
        this.hostNameCn = hostNameCn;
    }

    @Override
    public String toString() {
        return "ServerInfo{" +
                "hostIp='" + hostIp + '\'' +
                ", hostName='" + hostName + '\'' +
                ", hostNameCn='" + hostNameCn + '\'' +
                '}';
    }
}
