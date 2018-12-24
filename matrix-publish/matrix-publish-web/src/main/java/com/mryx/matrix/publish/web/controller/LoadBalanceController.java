package com.mryx.matrix.publish.web.controller;

import com.mryx.matrix.common.domain.ResultVo;
import com.mryx.matrix.publish.core.service.LoadBalanceService;
import com.mryx.matrix.publish.core.service.UlbService;
import com.mryx.matrix.publish.domain.UlbInfo;
import com.mryx.matrix.publish.dto.NodeDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 机器 摘流量/挂流量
 *
 * @author supeng
 * @date 2018/10/25
 */
@Slf4j
@RestController
@RequestMapping("/api/publish/lb")
public class LoadBalanceController extends BaseController {

    @Autowired
    private LoadBalanceService loadBalanceService;

    @Autowired
    private UlbService ulbService;

    /**
     * 摘流量/挂流量 Ulb Clb Nginx Dubbo
     *
     * @param nodeDTO
     * @return
     */
    @PostMapping(value = "switchNode", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResultVo switchNode(@RequestBody NodeDTO nodeDTO) {
        return loadBalanceService.switchNode(nodeDTO);
    }


    /**
     * 同步ulb信息
     */
    @Deprecated
    @PostMapping(value = "syncUlbDescribe", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResultVo syncUlbDescribe() {
        return loadBalanceService.syncUlbDescribe();
    }

    /**
     * 同步ulb信息
     */
    @Deprecated
    @PostMapping(value = "syncUlbDescribeByUlbId", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResultVo syncUlbDescribeByUlbId(@RequestBody UlbInfo ulbInfo) {
        Integer count = ulbService.describeULBByUlbId(ulbInfo);
        if (count > 0) {
            return ResultVo.Builder.SUCC().initSuccData(count);
        }
        return ResultVo.Builder.FAIL().initErrCodeAndMsg("1", "同步失败");
    }

    /**
     * 批量插入数据
     *
     * @param list
     * @return
     */
    @Deprecated
    @PostMapping(value = "batchInsertUlbInfo", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResultVo batchInsertUlbInfo(@RequestBody List<UlbInfo> list) {
        Integer flag = ulbService.batchInsertUlbInfo(list);
        if (flag > 0) {
            return ResultVo.Builder.SUCC().initSuccData(flag);
        }
        return ResultVo.Builder.FAIL().initErrCodeAndMsg("1", "新增失败");
    }

    /**
     * 根据IP获取Ulb信息
     *
     * @param ulbInfo
     * @return
     */
    @Deprecated
    @PostMapping(value = "getUlbInfoByIp", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResultVo getUlbInfoByIp(@RequestBody UlbInfo ulbInfo) {
        List<UlbInfo> result = ulbService.getUlbInfoByIp(ulbInfo);
        return ResultVo.Builder.SUCC().initSuccData(result);
    }

    /**
     * 根据UlbId获取Ulb信息
     *
     * @param ulbInfo
     * @return
     */
    @Deprecated
    @PostMapping(value = "getUlbInfoByUlbId", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResultVo getUlbInfoByUlbId(@RequestBody UlbInfo ulbInfo) {
        List<UlbInfo> result = ulbService.getUlbInfoByUlbId(ulbInfo);
        return ResultVo.Builder.SUCC().initSuccData(result);
    }

    /**
     * 删除全部Ulb信息
     *
     * @param ulbInfo
     * @return
     */
    @Deprecated
    @PostMapping(value = "deleteAll", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResultVo deleteAll(@RequestBody UlbInfo ulbInfo) {
        Integer flag = ulbService.deleteAll();
        return ResultVo.Builder.SUCC().initSuccData(flag);
    }


}
