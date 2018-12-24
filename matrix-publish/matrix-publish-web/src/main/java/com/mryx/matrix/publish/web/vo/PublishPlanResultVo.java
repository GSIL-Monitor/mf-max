package com.mryx.matrix.publish.web.vo;

import com.mryx.matrix.publish.domain.DeployPlanRecord;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author pengcheng
 * @description
 * @email pengcheng@missfresh.cn
 * @date 2018-11-21 15:30
 **/
@Data
public class PublishPlanResultVo implements Serializable {

    private static final long serialVersionUID = 6692677398273015155L;

    /**
     * 发布记录列表
     */
    private List<DeployPlanRecord> deployPlanRecordList;

    /**
     * 本次发布批次的数量
     */
    private Integer publishCount;

    /**
     * 是否有警告，1为有警告；0为无警告
     */
    private Integer hasTips;

    /**
     * 发布警告
     */
    private String tips;
}
