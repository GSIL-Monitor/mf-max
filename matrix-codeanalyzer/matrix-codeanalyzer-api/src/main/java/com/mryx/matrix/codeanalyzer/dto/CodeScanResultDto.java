package com.mryx.matrix.codeanalyzer.dto;

import com.mryx.matrix.codeanalyzer.domain.CodeScanResult;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

/**
 * @author lina02
 * @date 2018/10/10
 */
@Data
public class CodeScanResultDto implements Serializable {
    private static final long serialVersionUID = 7917345507074842804L;
    private static final Logger logger = LoggerFactory.getLogger(CodeScanResultDto.class);

    private Integer id;
    private String ret;
    private String code;
    private String message;
    private CodeScanResult data;
}