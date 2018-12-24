package com.mryx.matrix.codeanalyzer.dto;

import com.mryx.matrix.common.domain.Page;
import lombok.Data;

import java.io.Serializable;

@Data
public class ScanTaskDto extends Page implements Serializable {
    private static final long serialVersionUID = -801333715310299209L;

    /*任务ID*/
    private Integer id;
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
}
