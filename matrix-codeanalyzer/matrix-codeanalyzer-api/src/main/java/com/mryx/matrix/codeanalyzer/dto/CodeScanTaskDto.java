package com.mryx.matrix.codeanalyzer.dto;

import com.mryx.matrix.codeanalyzer.domain.CodeScanResult;
import com.mryx.matrix.codeanalyzer.domain.ProjectCodeScanTask;
import com.mryx.matrix.common.domain.Page;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

@Data
public class CodeScanTaskDto extends Page implements Serializable {
    private static final long serialVersionUID = 7917345817074842804L;
    private static final Logger logger = LoggerFactory.getLogger(CodeScanTaskDto.class);

    /*任务ID*/
    private Integer id;
    private ProjectCodeScanTask projectCodeScanTask;
    private CodeScanResult codeScanResult;
//    /*系统错误*/
//    private Integer blocker;
//    /*系统严重缺陷*/
//    private Integer critical;
//    /*系统一般缺陷*/
//    private Integer major;
//    /*系统建议缺陷*/
//    private Integer minor;
//    /*代码扫描结果信息*/
//    private Integer info;
//    /*代码扫描结果状态标志：1失败，0成功，2无结果*/
//    private Integer status;

    public CodeScanTaskDto() {
    }

    public CodeScanTaskDto(Integer id, ProjectCodeScanTask projectCodeScanTask, CodeScanResult codeScanResult) {
        this.id = id;
        this.projectCodeScanTask = projectCodeScanTask;
        this.codeScanResult = codeScanResult;
    }
}
