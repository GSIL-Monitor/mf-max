package com.mryx.matrix.project.domain;

import lombok.Data;

/**
 * @author pengcheng
 * @description
 * @email pengcheng@missfresh.cn
 * @date 2018-11-14 13:14
 **/
@Data
public class JiraFieldJson {

    /**
     * 字段ID
     */
    private String id;

    /**
     * 字段名称
     */
    private String name;

    /**
     * 是否为自定义字段
     */
    private Boolean custom;

    /**
     * 是否可导航
     */
    private Boolean navigable;

    /**
     * 是否可搜索
     */
    private Boolean searchable;
}
