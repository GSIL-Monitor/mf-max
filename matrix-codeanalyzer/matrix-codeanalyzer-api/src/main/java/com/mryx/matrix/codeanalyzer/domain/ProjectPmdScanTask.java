package com.mryx.matrix.codeanalyzer.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.mryx.matrix.common.domain.Page;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class ProjectPmdScanTask extends Page implements Serializable {
    private static final long serialVersionUID = -1287333715310829209L;

    /*主键ID*/
    private Integer id;
    /*任务ID*/
    private Integer taskId;
    /*任务名称*/
    private String taskName;
    /*被测应用*/
    private String appCode;
    /*扫描类型*/
    private Integer modeOfScan;
    /*代码分支*/
    private String codeBranch;
    /*基线版本*/
    private String baseVersion;
    /*对比版本*/
    private String compareVersion;
    /*定时任务触发时间*/
    private String timeTrigger;
    /*前端传过来的token，用此token调用接口查询创建人姓名*/
    private String accessToken;
    /*创建人姓名*/
    private String userName;
    /*代码git地址*/
    private String gitAddress;

    /*系统错误*/
    private Integer blocker;
    /*系统严重缺陷*/
    private Integer critical;
    /*系统一般缺陷*/
    private Integer major;
    /*系统建议缺陷*/
    private Integer minor;
    /*扫描结果信息*/
    private Integer info;
    /*扫描结果状态标志：1失败，0成功，2无结果*/
    private Integer status;
    /*是否为发布分支master，0不是，1是*/
    private Integer isMaster;
    /*代码扫描结果链接*/
    private String pmdScanResultUrl;
    /*blocker链接*/
    private String blockerResultUrl;
    /*critical链接*/
    private String criticalResultUrl;
    /*major链接*/
    private String majorResultUrl;
    /*minor链接*/
    private String minorResultUrl;
    /*info链接*/
    private String infoResultUrl;

    /*扫描时间*/
    private Date scanTime;

    /*创建时间*/
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /*更新时间*/
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    public ProjectPmdScanTask() {
    }

    @Override
    public String toString() {
        return "ProjectPmdScanTask{" +
                "id=" + id +
                ", taskName='" + taskName + '\'' +
                ", appCode='" + appCode + '\'' +
                ", modeOfScan=" + modeOfScan +
                ", codeBranch='" + codeBranch + '\'' +
                ", baseVersion='" + baseVersion + '\'' +
                ", compareVersion='" + compareVersion + '\'' +
                ", timeTrigger='" + timeTrigger + '\'' +
                ", accessToken='" + accessToken + '\'' +
                ", userName='" + userName + '\'' +
                ", gitAddress='" + gitAddress + '\'' +
                ", blocker=" + blocker +
                ", critical=" + critical +
                ", major=" + major +
                ", minor=" + minor +
                ", info=" + info +
                ", status=" + status +
                ", isMaster=" + isMaster +
                ", pmdScanResultUrl='" + pmdScanResultUrl + '\'' +
                ", blockerResultUrl='" + blockerResultUrl + '\'' +
                ", criticalResultUrl='" + criticalResultUrl + '\'' +
                ", majorResultUrl='" + majorResultUrl + '\'' +
                ", minorResultUrl='" + minorResultUrl + '\'' +
                ", infoResultUrl='" + infoResultUrl + '\'' +
                ", scanTime=" + scanTime +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                '}';
    }
}
