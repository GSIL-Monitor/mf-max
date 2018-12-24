package com.mryx.matrix.process.web.param;

import lombok.Data;

import java.io.Serializable;

/**
 * 基础参数
 *
 * @author wangwenbo
 * @create 2018-08-31 16:05
 **/
@Data
public class BaseParam implements Serializable{


    private static final long serialVersionUID = -7518199473553185016L;
    public final static int DEFAULT_PAGE_NO = 1;
    public final static int DEFAULT_PAGE_SIZE = 20;

    /**
     * 来源
     */
    private String source;


    private Integer pageNo = DEFAULT_PAGE_NO;

    private Integer pageSize = DEFAULT_PAGE_SIZE;
}
