package com.mryx.matrix.publish.web.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.mryx.matrix.common.dto.AppInfoDto;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author dinglu
 * @date 2018/9/18
 */
@Data
public class ReleaseDeployRecordVo implements Serializable {

    private static final long serialVersionUID = 119874178490689947L;

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

    /*发布状态描述*/
    private String deployStatusDesc;

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
     * 应用信息
     */
    private AppInfoDto appInfoDto;

    @Override
    public String toString() {
        return "ReleaseDeployRecordVo{" +
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
                ", deployStatusDesc='" + deployStatusDesc + '\'' +
                ", serviceIps='" + serviceIps + '\'' +
                ", publishUser='" + publishUser + '\'' +
                ", logPath='" + logPath + '\'' +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                ", delFlag=" + delFlag +
                ", appInfoDto=" + appInfoDto +
                '}';
    }

}
