package com.mryx.matrix.codeanalyzer.dto;

import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

@Data
public class PieDto implements Serializable {
    private static final long serialVersionUID = 2109274842804L;
    private static final Logger logger = LoggerFactory.getLogger(PieDto.class);

    private Integer id;
    private String item;
    private Integer count;
}
