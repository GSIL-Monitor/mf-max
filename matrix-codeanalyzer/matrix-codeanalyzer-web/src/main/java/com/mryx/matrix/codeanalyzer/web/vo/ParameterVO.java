package com.mryx.matrix.codeanalyzer.web.vo;

import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.Date;

/**
 * @author lina02
 * @date 2018/9/26
 */
@Data
public class ParameterVO implements Serializable {
    private static final long serialVersionUID = 7907345507074842804L;
    private static final Logger logger = LoggerFactory.getLogger(ParameterVO.class);

    private Integer id;
    private String address;
    private String branch;
    private Date time;
}