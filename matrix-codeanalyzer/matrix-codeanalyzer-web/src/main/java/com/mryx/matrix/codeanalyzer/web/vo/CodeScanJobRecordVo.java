package com.mryx.matrix.codeanalyzer.web.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.mryx.matrix.common.domain.Page;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.Date;

@Data
public class CodeScanJobRecordVo implements Serializable {
    private static final long serialVersionUID = 1345507074842804L;
    private static final Logger logger = LoggerFactory.getLogger(CodeScanJobRecordVo.class);

    /*代码扫描结果ID*/
    private Integer id;
    /*系统错误*/
    private Integer blocker;
    /*系统严重缺陷*/
    private Integer critical;
    /*系统一般缺陷*/
    private Integer major;
    /*任务扫描时间*/
    private String jobScanTime;
}
