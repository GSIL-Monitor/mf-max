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
public class CodeScanJob extends Page implements Serializable {
    private static final long serialVersionUID = 175507074842804L;
    private static final Logger logger = LoggerFactory.getLogger(CodeScanJob.class);

    /*代码扫描任务ID*/
    private Integer id;
    /*任务名称*/
    private String jobName;
    /*代码git地址*/
    private String gitAddress;
    /*代码分支*/
    private String codeBranch;
    /*被测应用*/
    private String appCode;
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
    private Integer jobStatus;
    /*代码扫描任务结果链接*/
    private String codeScanResultUrl;
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
    /*基线版本*/
    private String baseVersion;
    /*对比版本*/
    private String compareVersion;
    /*定时任务触发时间*/
    private String timeTrigger;
    /*创建人姓名*/
    private String userName;
    /*更改任务人姓名*/
    private String modifyUserName;
    /*扫描类型*/
    private Integer typeOfScan;
    /*扫描模式*/
    private Integer modeOfScan;
    /*前端传过来的token，用此token调用接口查询创建人姓名*/
    private String accessToken;
    /*健康度*/
    private Double health;
    /*问题数*/
    private Integer problem;
    /*代码行*/
    private String codeLine;
    /*重复行*/
    private String duplicateLine;
    /*运行人*/
    private String runUserName;
    /*删除任务人姓名*/
    private String deleteUserName;
    /*任务是否被删除，0未删除，1已删除*/
    private Integer deleted;

    /*创建时间*/
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /*更新时间*/
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;
}
