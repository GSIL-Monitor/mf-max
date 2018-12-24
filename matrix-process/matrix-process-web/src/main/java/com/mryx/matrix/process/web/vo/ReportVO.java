package com.mryx.matrix.process.web.vo;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;


/**
 * 周报表
 * @author juqing
 * @email jvqing@missfresh.cn
 * @date 2018-09-05 15:14
 **/
@Data
public class ReportVO implements Serializable {


    private static final long serialVersionUID = -4572806059741574848L;
    /**id**/
    private Integer id;

    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd")
    private Date reportTime;

    private Date createTime;

    private Date updateTime;

    /**自定义项目名称**/
    private String selfProjectName;

    /**本周内容**/
    private String curWeekCnt;

    /**下周计划**/
    private String nextWeekCnt;

    /**记录人**/
    private String author;

    /**0已删除 1未删除**/
    private Integer delFlag;
    
}
