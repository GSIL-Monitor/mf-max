package com.mryx.matrix.codeanalyzer.domain;

import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

/**
 * 代码差异比较结果表
 *
 * @author lina02
 * @date 2018/9/26
 */
@Data
public class CodeDiffResult {
    private static final long serialVersionUID = 7907345507074842804L;
    private static final Logger logger = LoggerFactory.getLogger(CodeDiffResult.class);

    /*代码差异比较ID*/
    private Integer id;
    /*关联的应用ID*/
    private Integer projectTaskId;
    /*代码分支*/
    private String codeBranch;
    /*代码差异比较时间*/
    private Date codeDiffTime;
    /*代码差异比较结果*/
    private String codeDiffResult;
    /*代码差异比较结果链接*/
    private String linkURL;
    /*表创建时间*/
    private Date createTime;
    /*表更新时间*/
    private Date updateTime;
     /*
    ID，记录task的ID，分支，链接，结果
     */
}