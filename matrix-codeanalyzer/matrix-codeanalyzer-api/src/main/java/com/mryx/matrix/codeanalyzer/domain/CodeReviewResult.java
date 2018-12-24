package com.mryx.matrix.codeanalyzer.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.Date;

/**
 * 代码评审结果表
 *
 * @author lina02
 * @date 2018/9/26
 */
@Data
public class CodeReviewResult implements Serializable {
    private static final long serialVersionUID = 7907345507074842804L;
    private static final Logger logger = LoggerFactory.getLogger(CodeReviewResult.class);

    /*代码评审ID*/
    private Integer id;

    /*关联的应用ID*/
    private Integer projectTaskId;

    /*代码分支*/
    private String codeBranch;

    /*代码评审时间*/
    @JsonFormat(timezone = "GTM+8", pattern = "yyyy-mm-dd")
    private Date codeReviewTime;

    /*代码评审结果 0:Needs Review正在评审,1:Needs Revision评审失败,2:Accepted评审通过,3:Closed关闭*/
    private String codeReviewResult;

    /*代码评审人*/
    private String codeReviewPerson;

    /*代码评审结果链接*/
    private String linkURL;

    /*表创建时间*/
    private Date createTime;

    /*表更新时间*/
    private Date updateTime;

    public CodeReviewResult() {
    }

    public CodeReviewResult(Integer id, Integer projectTaskId, String codeBranch, Date codeReviewTime, String codeReviewResult, String codeReviewPerson, String linkURL, Date createTime, Date updateTime) {
        this.id = id;
        this.projectTaskId = projectTaskId;
        this.codeBranch = codeBranch;
        this.codeReviewTime = codeReviewTime;
        this.codeReviewResult = codeReviewResult;
        this.codeReviewPerson = codeReviewPerson;
        this.linkURL = linkURL;
        this.createTime = createTime;
        this.updateTime = updateTime;
    }
}