package com.mryx.matrix.codeanalyzer.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class FirstDeptDto implements Serializable {
    private static final long serialVersionUID = 102345817074842804L;

    /*ID*/
    private Integer id;
    /*部门代号*/
    private String code;
    /*部门ID*/
    private Integer deptId;
    /*部门名字*/
    private String name;
    /*父级部门*/
    private Integer parentId;
}
