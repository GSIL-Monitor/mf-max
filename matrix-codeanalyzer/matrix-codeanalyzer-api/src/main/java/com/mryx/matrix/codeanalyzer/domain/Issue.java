package com.mryx.matrix.codeanalyzer.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.util.Date;

@Data
public class Issue implements Serializable {
    private static final long serialVersionUID = 10924591842804L;

//    /*问题记录ID*/
//    private Integer id;
//    /*问题所属CodeScanJobRecord的ID*/
//    private Integer codeScanJobRecordId;
    /*问题ID*/
    private String Problem;
    /*问题出现的文件*/
    private String File;
    /*问题优先级*/
    private String Priority;
    /*问题出现的文件所在行*/
    private String Line;
    /*问题描述*/
    private String Description;
    /*问题违反的规则集*/
    private String Ruleset;
    /*问题违反的规则*/
    private String Rule;
    /*创建时间*/
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
    /*更新时间*/
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;
}
