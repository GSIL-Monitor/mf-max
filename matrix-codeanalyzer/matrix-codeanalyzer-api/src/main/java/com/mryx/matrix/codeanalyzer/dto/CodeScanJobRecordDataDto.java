package com.mryx.matrix.codeanalyzer.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

@Data
public class CodeScanJobRecordDataDto implements Serializable {
    private static final long serialVersionUID = 12867074842804L;
    private static final Logger logger = LoggerFactory.getLogger(CodeScanJobRecordDataDto.class);

    /*代码扫描结果ID*/
    private Integer id;
    /*系统错误*/
    private Integer blocker;
    /*系统严重缺陷*/
    private Integer critical;
    /*系统一般缺陷*/
    private Integer major;
    /*代码行*/
    private String codeLine;
    /*重复行*/
    private String duplicateLine;
    /*任务扫描时间*/
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private String codeScanTime;
}
