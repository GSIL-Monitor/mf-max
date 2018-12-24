package com.mryx.matrix.process.web.vo;

import java.io.Serializable;
import java.util.Date;

public class BetaVO implements Serializable {

    /**
     * id
     **/
    private Integer id;

    /**
     * 环境名称
     **/
    private String env;

    /**
     * ip地址
     **/
    private String ip;

    /**
     * 创建人
     **/
    private String createUser;

    /**
     * 更新人
     **/
    private String updateUser;

    /**
     * 创建时间
     **/
    private Date createTime;

    /**
     * 更新时间
     **/
    private Date updateTime;

    /**
     * 0已删除 1未删除
     **/
    private Integer delFlag;


    /**
     * 项目ID
     */
    private Integer projectId;

    /**
     * 项目名称
     */
    private String projectName;

    /**
     * 占用标识，0只看被占用；1查看未占用；2查看所有
     */
    private Integer canUse;

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return this.id;
    }

    public void setEnv(String env) {
        this.env = env;
    }

    public String getEnv() {
        return this.env;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getIp() {
        return this.ip;
    }

    public void setCreateUser(String createUser) {
        this.createUser = createUser;
    }

    public String getCreateUser() {
        return this.createUser;
    }

    public void setUpdateUser(String updateUser) {
        this.updateUser = updateUser;
    }

    public String getUpdateUser() {
        return this.updateUser;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getCreateTime() {
        return this.createTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public Date getUpdateTime() {
        return this.updateTime;
    }

    public Integer getDelFlag() {
        return delFlag;
    }

    public void setDelFlag(Integer delFlag) {
        this.delFlag = delFlag;
    }

    public Integer getProjectId() {
        return projectId;
    }

    public void setProjectId(Integer projectId) {
        this.projectId = projectId;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public Integer getCanUse() {
        return canUse;
    }

    public void setCanUse(Integer canUse) {
        this.canUse = canUse;
    }

    @Override
    public String toString() {
        return "BetaVO [" +
                "id=" + id +
                ", env='" + env + '\'' +
                ", ip='" + ip + '\'' +
                ", createUser='" + createUser + '\'' +
                ", updateUser='" + updateUser + '\'' +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                ", delFlag=" + delFlag +
                ", projectId=" + projectId +
                ", projectName='" + projectName + '\'' +
                ", canUse=" + canUse +
                ']';
    }
}
