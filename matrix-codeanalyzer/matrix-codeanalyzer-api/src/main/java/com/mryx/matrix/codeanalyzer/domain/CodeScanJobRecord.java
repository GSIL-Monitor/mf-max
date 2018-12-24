package com.mryx.matrix.codeanalyzer.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.mryx.matrix.common.domain.Page;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 代码扫描结果
 *
 * @author supeng
 * @date 2018/09/26
 */
@Data
public class CodeScanJobRecord extends Page implements Serializable {
    private static final long serialVersionUID = 109245507074842804L;
    private static final Logger logger = LoggerFactory.getLogger(CodeScanJobRecord.class);

    /*任务执行记录ID*/
    private Integer id;
    /*任务ID*/
    private Integer jobId;
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
    /*扫描类型*/
    private Integer typeOfScan;
    /*手动、自动执行代码扫描，0代表手动，1代表自动*/
    private Integer manualOrAutomatic;
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
    /*运行人*/
    private String runUserName;
    /*代码行*/
    private String codeLine;
    /*重复行*/
    private String duplicateLine;
    /*标示字段，sonar有，p3c没有*/
    private String componentKey;
    /*代码扫描时间*/
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date codeScanTime;
    /*创建时间*/
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
    /*更新时间*/
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    private List<Issue> blockerIssue;
    private List<Issue> criticalIssue;
    private List<Issue> majorIssue;
}
