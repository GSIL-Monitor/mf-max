package com.mryx.matrix.process.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 部门app树
 *
 * @author wangwenbo
 * @create 2018-09-19 16:27
 **/
@Data
public class DeptAppTree {

    /**
     * deptId
     */
    private Long deptId;
    /**
     * 节点的子节点
     */
    private List<DeptAppTree> children = new ArrayList<>();

    private List<AppDto> apps = new ArrayList<>();
    /**
     * 父ID
     */
    private Long parentId;
    /**
     * 是否有父节点
     */
    private boolean hasParent = false;
    /**
     * 是否有子节点
     */
    private boolean hasChildren = false;


    private DeptDto deptDto;


}
