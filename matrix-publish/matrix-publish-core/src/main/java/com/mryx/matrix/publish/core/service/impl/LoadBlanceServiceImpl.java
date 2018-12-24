package com.mryx.matrix.publish.core.service.impl;

import com.mryx.matrix.common.domain.ResultVo;
import com.mryx.matrix.publish.core.service.LoadBalanceService;
import com.mryx.matrix.publish.core.service.UlbService;
import com.mryx.matrix.publish.dto.NodeDTO;
import com.mryx.matrix.publish.enums.LbAction;
import com.mryx.matrix.publish.enums.LbType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Objects;

/**
 * LB Service Impl
 *
 * @author supeng
 * @date 2018/10/28
 */
@Slf4j
@Service
public class LoadBlanceServiceImpl implements LoadBalanceService {

    @Autowired
    private UlbService ulbService;

    @Override
    public ResultVo switchNode(NodeDTO nodeDTO) {
        log.info("switchNode nodeDTO = {}", nodeDTO);
        if (nodeDTO == null || Objects.equals(null, nodeDTO.getIp()) || Objects.equals("", nodeDTO.getIp())
                || Objects.equals(null, nodeDTO.getBackendPort()) || Objects.equals("", nodeDTO.getBackendPort()) || Objects.equals(0, nodeDTO.getBackendPort())
                || Objects.equals(null, nodeDTO.getType()) || Objects.equals("", nodeDTO.getType())
                || Objects.equals(null, nodeDTO.getAction()) || Objects.equals("", nodeDTO.getAction())) {
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("1", "操作ULB参数错误！");
        }
        if (LbType.ULB.name().equals(nodeDTO.getType())) {
            return handleUlb(nodeDTO);
        }

        if (LbType.CLB.name().equals(nodeDTO.getType())) {
            return handleClb(nodeDTO);
        }

        if (LbType.NGINX.name().equals(nodeDTO.getType())) {
            return handleNginx(nodeDTO);
        }

        if (LbType.DUBBO.name().equals(nodeDTO.getType())) {
            return handleDubbo(nodeDTO);
        }
        return ResultVo.Builder.FAIL().initErrCodeAndMsg("1", "操作ULB操作失败！");
    }

    /**
     * 操作ULB
     *
     * @param nodeDTO
     * @return
     */
    private ResultVo handleUlb(NodeDTO nodeDTO) {
        LbAction lbAction = LbAction.getLbActionByName(nodeDTO.getAction());
        log.info("handleUlb lbAction = {}", lbAction);
        if (lbAction == null) {
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("1", "操作ULB参数action不正确");
        }
        Map<String, String> resultMap = ulbService.updateBackendAttribute(nodeDTO.getIp(), nodeDTO.getBackendPort(), lbAction.getCode());
        if (resultMap == null || resultMap.isEmpty()) {
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("1", "操作ULB失败！");
        }
        if ("0".equals(resultMap.get("code"))) {
            return ResultVo.Builder.SUCC().initSuccDataAndMsg("0", resultMap.get("msg"));
        } else if ("1".equals(resultMap.get("code"))) {
            return ResultVo.Builder.SUCC().initSuccDataAndMsg("1", resultMap.get("msg"));
        }
        return ResultVo.Builder.FAIL().initErrCodeAndMsg("1", "操作ULB失败！");
    }

    /**
     * 操作CLB
     *
     * @param nodeDTO
     * @return
     */
    private ResultVo handleClb(NodeDTO nodeDTO) {
        return ResultVo.Builder.FAIL().initErrCodeAndMsg("1", "操作失败！");
    }

    /**
     * 操作Nginx
     *
     * @param nodeDTO
     * @return
     */
    private ResultVo handleNginx(NodeDTO nodeDTO) {
        return ResultVo.Builder.FAIL().initErrCodeAndMsg("1", "操作失败！");
    }

    /**
     * 操作Dubbo
     *
     * @param nodeDTO
     * @return
     */
    private ResultVo handleDubbo(NodeDTO nodeDTO) {
        return ResultVo.Builder.FAIL().initErrCodeAndMsg("1", "操作失败！");
    }

    @Override
    public ResultVo syncUlbDescribe() {
        log.info("start syncUlbDescribe");
        Integer flag = ulbService.describeULB();
        if (flag > 0) {
            return ResultVo.Builder.SUCC().initSuccData(flag);
        }
        return ResultVo.Builder.FAIL().initErrCodeAndMsg("1", "同步失败！");
    }

}
