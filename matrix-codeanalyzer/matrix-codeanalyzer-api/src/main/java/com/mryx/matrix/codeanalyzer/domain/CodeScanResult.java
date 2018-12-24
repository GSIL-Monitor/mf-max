package com.mryx.matrix.codeanalyzer.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.mryx.matrix.common.domain.Page;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.Date;

/**
 * 代码扫描结果
 *
 * @author supeng
 * @date 2018/09/26
 */
@Data
public class CodeScanResult extends Page implements Serializable {
    private static final long serialVersionUID = 7307345507074842804L;
    private static final Logger logger = LoggerFactory.getLogger(CodeScanResult.class);

    /*代码扫描结果ID*/
    private Integer id;
    /*关联的应用ID*/
    private Integer projectTaskId;
    /*手动、自动执行代码扫描，0代表手动，1代表自动*/
    private Integer manualOrAutomatic;
    /*代码git地址*/
    private String gitAddress;
    /*代码分支*/
    private String codeBranch;
    /*代码扫描时间*/
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date codeScanTime;
    /*系统错误*/
    private Integer blocker;
    /*系统严重缺陷*/
    private Integer critical;
    /*系统一般缺陷*/
    private Integer major;
    /*系统建议缺陷*/
    private Integer minor;
    /*代码扫描结果信息*/
    private Integer info;
    /*代码扫描结果状态标志：1失败，0成功，2无结果*/
    private Integer status;
    /*是否为发布分支master，0不是，1是*/
    private Integer isMaster;
    /*代码扫描结果链接*/
    private String codeScanResultUrl;
    /*创建时间*/
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /*更新时间*/
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

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

    /*任务ID*/
    private Integer taskId;
    /*任务名称*/
    private String taskName;
    /*被测应用*/
    private String appCode;
    /*扫描类型*/
    private Integer typeOfScan;
    /*扫描模式*/
    private Integer modeOfScan;
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
    /*项目名字*/
    private String projectName;
}
