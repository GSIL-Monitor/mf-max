package com.mryx.matrix.publish.core.service;

import com.mryx.matrix.common.domain.ResultVo;
import com.mryx.matrix.publish.dto.NodeDTO;

/**
 * LB Service
 *
 * @author supeng
 * @date 2018/10/28
 */
public interface LoadBalanceService {
    /**
     * 摘流量/挂流量
     * @param nodeDTO
     * @return
     */
    ResultVo switchNode(NodeDTO nodeDTO);

    /**
     * 同步Ulb信息到数据库
     * @return
     */
    @Deprecated
    ResultVo syncUlbDescribe();
}
