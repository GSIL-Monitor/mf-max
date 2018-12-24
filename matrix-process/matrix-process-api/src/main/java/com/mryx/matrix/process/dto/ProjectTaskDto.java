package com.mryx.matrix.process.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.mryx.matrix.process.domain.BasePage;
import com.mryx.matrix.publish.domain.ProjectTaskBatch;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;


/**
 * 应用发布工单表
 *
 * @author zhaoxl
 * @email zhaoxl02@missfresh.cn
 * @date 2018-09-04 16:21
 **/
@Data
public class ProjectTaskDto extends BasePage implements Serializable {

    private static final long serialVersionUID = 6816553181892541948L;

    /**
     * 应用id
     **/
    private Integer id;

    /**
     * 项目ID
     **/
    private Integer projectId;

    /**
     * 发布顺序
     **/
    private Integer sequenece;

    /**
     * 应用标识
     **/
    private String appCode;

    /**
     * 应用分支
     **/
    private String appBranch;

    /**
     * 要发布的btag
     **/
    private String appBtag;

    /**
     * 应用开发人
     **/
    private String appDevOwner;

    /**
     * 发布完成的rtag
     **/
    private String appRtag;

    /**
     * 应用状态
     **/
    private Integer taskStatus;

    /**
     * 应用ip
     **/
    private String serviceIps;

    /**
     * 创建时间
     **/
    private Date createTime;

    /**
     * 更新时间
     **/
    private Date updateTime;

    /**
     * 1:正常|0:删除
     **/
    private Integer delFlag;

    /*测试环境profile*/
    private String profile;

    /*只deploy不部署*/
    private boolean isDeploy;


    /*release应用发布状态*/
    private Integer releaseTaskStatus;

    private String appBranchSuffix;

    private List<GroupInfoDto> groupInfo;

    /*发布批次*/
    @JsonProperty("publishBatchVoList")
    private List<ProjectTaskBatch> projectTaskBatchList;

    /*代码扫描结果数据*/
    private String codeScanResultDesc;
    private CodeScanResultDataDto codeScanResultPrevKpi;
    private CodeScanResultDataDto codeScanResultCurrKpi;

    private String appGitAddress;

    /*代码扫描结果链接*/
    private String codeScanResultUrl;

    /*代码评审结果链接*/
    private String codeReviewResultUrl;

    /**
     * 压测服务器列表
     */
    private List<String> stressIpList;

    /**
     * 从应用中心获取的信息
     */
    private AppInfoDto appInfoDto;

    @Override
    public String toString() {
        return "ProjectTaskDto{" +
                "id=" + id +
                ", projectId=" + projectId +
                ", sequenece=" + sequenece +
                ", appCode='" + appCode + '\'' +
                ", appBranch='" + appBranch + '\'' +
                ", appBtag='" + appBtag + '\'' +
                ", appDevOwner='" + appDevOwner + '\'' +
                ", appRtag='" + appRtag + '\'' +
                ", taskStatus=" + taskStatus +
                ", serviceIps='" + serviceIps + '\'' +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                ", delFlag=" + delFlag +
                ", profile='" + profile + '\'' +
                ", isDeploy=" + isDeploy +
                ", releaseTaskStatus=" + releaseTaskStatus +
                ", appBranchSuffix='" + appBranchSuffix + '\'' +
                ", groupInfo=" + groupInfo +
                ", projectTaskBatchList=" + projectTaskBatchList +
                ", codeScanResultDesc='" + codeScanResultDesc + '\'' +
                ", codeScanResultPrevKpi=" + codeScanResultPrevKpi +
                ", codeScanResultCurrKpi=" + codeScanResultCurrKpi +
                ", appGitAddress='" + appGitAddress + '\'' +
                ", codeScanResultUrl='" + codeScanResultUrl + '\'' +
                ", codeReviewResultUrl='" + codeReviewResultUrl + '\'' +
                ", stressIpList=" + stressIpList +
                ", appInfoDto=" + appInfoDto +
                '}';
    }

}
