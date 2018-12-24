package com.mryx.matrix.process.web.param;

import lombok.Data;

/**
 * @author wangwenbo
 * @create 2018-09-20 11:25
 **/
@Data
public class GetDeptAppTreeByDeptIdParam extends BaseParam {


    private static final long serialVersionUID = -3204994242896329134L;

    private Long deptId;

    /**
     * app code 模糊查询
     */
    private String appCode;
}
