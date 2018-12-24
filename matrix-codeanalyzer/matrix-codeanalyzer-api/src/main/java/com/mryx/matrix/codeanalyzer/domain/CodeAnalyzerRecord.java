package com.mryx.matrix.codeanalyzer.domain;

import lombok.Data;

/**
 * 代码分析记录
 * code diff、code review、code scan
 *
 * @author supeng
 * @date 2018/09/26
 */
@Data
public class CodeAnalyzerRecord {
    /**
     * ID
     */
    private Integer id;
    /**
     * 项目Task ID
     */
    private Integer projectTaskId;
    /**
     * 是否code diff （1：是 0：否）
     */
    private Integer codeDiff;
    /**
     * 是否code review （1：是，0：否）
     */
    private Integer codeReview;
    /**
     * 是否进行代码扫描 （1：是，0：否）
     */
    private Integer codeScan;
}