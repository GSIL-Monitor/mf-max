package com.mryx.matrix.process.web.param;

import java.io.Serializable;

/**
 * 部门dto
 *
 * @author wangwenbo
 * @create 2018-09-13 22:04
 **/
public class DeptDto implements Serializable{


    private static final long serialVersionUID = -3767546636681547693L;

    private Long deptId;
    //上级部门ID，一级部门为0
    private Long parentId;
    //部门名称
    private String name;
    //部门编码
    private String code;

    public DeptDto() {
    }

    public DeptDto(Long deptId, Long parentId, String name, String code) {
        this.deptId = deptId;
        this.parentId = parentId;
        this.name = name;
        this.code = code;
    }

    public Long getDeptId() {
        return deptId;
    }

    public void setDeptId(Long deptId) {
        this.deptId = deptId;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    @Override
    public String toString() {
        return "DeptDto{" +
                "deptId=" + deptId +
                ", parentId=" + parentId +
                ", name='" + name + '\'' +
                ", code='" + code + '\'' +
                '}';
    }
}
