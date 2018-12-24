package com.mryx.matrix.codeanalyzer.dto;

import com.mryx.matrix.codeanalyzer.domain.CodeScanJobRecord;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

/**
 * @author lina02
 * @date 2018/10/10
 */
@Data
public class CodeScanJobRecordDto implements Serializable {
    private static final long serialVersionUID = 23345507074842804L;
    private static final Logger logger = LoggerFactory.getLogger(CodeScanJobRecordDto.class);

    private String id;
    private String ret;
    private String code;
    private String message;
    private CodeScanJobRecord data;
}
