package com.mryx.matrix.codeanalyzer.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class AppDto implements Serializable {
    private static final long serialVersionUID = 102074842804L;

    /*ID*/
    private Integer id;
    /*应用名字*/
    private String appName;
    /*应用代号*/
    private String appCode;
}
