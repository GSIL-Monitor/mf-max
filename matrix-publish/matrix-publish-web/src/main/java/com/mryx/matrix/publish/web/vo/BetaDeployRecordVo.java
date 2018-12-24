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
public class BetaDeployRecordVo implements Serializable {

    private static final long serialVersionUID = 367947375468751913L;

    /**
     * 测试环境部署记录id
     **/
    private Integer id;

    /**
     * 项目ID
     **/
    private Integer projectId;

    /**
     * 项目名称
     **/
    private String projectName;

    /**
     * 应用发布工单ID
     **/
    private Integer projectTaskId;

    /**
     * 应用标识
     **/
    private String appCode;

    /**
     * 应用名称
     **/
    private String appName;

    /**
     * 应用发布分支
     **/
    private String appBranch;

    /**
     * 生成的btag
     **/
    private String appBtag;

    /*测试环境profile*/
    private String profile;

    /*只deploy不部署*/
    private Boolean isDeploy;

    /**
     * 发布状态
     **/
    private Integer deployStatus;

    /*发布状态描述*/
    private String deployStatusDesc;

    /*发布子状态*/
    private Integer subDeployStatus;

    /**
     * 应用ip
     **/
    private String serviceIps;

    /*发布记录日志地址*/
    private String logPath;

    /*发布者*/
    private String publishUser;

    /*中断者*/
    private String interruptUser;

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
        return "BetaDeployRecordVo{" +
                "id=" + id +
                ", projectId=" + projectId +
                ", projectName='" + projectName + '\'' +
                ", projectTaskId=" + projectTaskId +
                ", appCode='" + appCode + '\'' +
                ", appName='" + appName + '\'' +
                ", appBranch='" + appBranch + '\'' +
                ", appBtag='" + appBtag + '\'' +
                ", profile='" + profile + '\'' +
                ", isDeploy=" + isDeploy +
                ", deployStatus=" + deployStatus +
                ", deployStatusDesc='" + deployStatusDesc + '\'' +
                ", subDeployStatus=" + subDeployStatus +
                ", serviceIps='" + serviceIps + '\'' +
                ", logPath='" + logPath + '\'' +
                ", publishUser='" + publishUser + '\'' +
                ", interruptUser='" + interruptUser + '\'' +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                ", delFlag=" + delFlag +
                ", appInfoDto=" + appInfoDto +
                '}';
    }
}
