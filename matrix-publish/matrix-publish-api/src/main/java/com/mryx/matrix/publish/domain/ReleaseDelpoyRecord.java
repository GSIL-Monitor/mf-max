package com.mryx.matrix.publish.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.mryx.matrix.common.domain.Page;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;


/**
 * 生产环境部署记录表
 *
 * @author zhaoxl
 * @email zhaoxl02@missfresh.cn
 * @date 2018-09-07 15:55
 **/
@Data
public class ReleaseDelpoyRecord extends Page implements Serializable {

    private static final long serialVersionUID = 4869961394549106236L;

    /**
     * 生产环境部署记录id
     **/
    private Integer id;

    /**
     * 项目ID
     **/
    private Integer projectId;

    /**
     * 项目发布工单ID
     **/
    private Integer projectTaskId;

    /*依赖的上一条发布ID*/
    private Integer forwardRecordId;

    /**
     * 应用标识
     **/
    private String appCode;

    /**
     * 应用名称
     **/
    private String appName;

    /**
     * 应用分支
     **/
    private String appBranch;

    /**
     * 要发布的btag
     **/
    private String appBtag;

    /**
     * 发布完成的rtag
     **/
    private String appRtag;

    /**
     * 应用状态
     **/
    private Integer deployStatus;

    /*发布子状态*/
    private Integer subDeployStatus;

    /**
     * 应用ip
     **/
    private String serviceIps;

    /*发布者*/
    private String publishUser;

    /*日志地址*/
    private String logPath;

    /**
     * 创建时间
     **/
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /**
     * 更新时间
     **/
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    /**
     * 1:正常|0:删除
     **/
    private Integer delFlag;

    /**
     * 回滚标志,1:回滚记录|0:发布记录
     */
    private Integer rollbackFlag;

    /**
     * 检索标识，如果为exact代表精确检索
     */
    private String mode;

    @Override
    public String toString() {
        return "ReleaseDelpoyRecord{" +
                "id=" + id +
                ", projectId=" + projectId +
                ", projectTaskId=" + projectTaskId +
                ", forwardRecordId=" + forwardRecordId +
                ", appCode='" + appCode + '\'' +
                ", appName='" + appName + '\'' +
                ", appBranch='" + appBranch + '\'' +
                ", appBtag='" + appBtag + '\'' +
                ", appRtag='" + appRtag + '\'' +
                ", deployStatus=" + deployStatus +
                ", subDeployStatus=" + subDeployStatus +
                ", serviceIps='" + serviceIps + '\'' +
                ", publishUser='" + publishUser + '\'' +
                ", logPath='" + logPath + '\'' +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                ", delFlag=" + delFlag +
                ", rollbackFlag=" + rollbackFlag +
                ", mode='" + mode + '\'' +
                '}';
    }
}
