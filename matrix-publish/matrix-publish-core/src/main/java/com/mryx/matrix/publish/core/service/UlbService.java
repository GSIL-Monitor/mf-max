package com.mryx.matrix.publish.core.service;

import com.mryx.matrix.publish.domain.UlbInfo;

import java.util.List;
import java.util.Map;

/**
 * ULB Service
 *
 * @author supeng
 * @date 2018/10/31
 */
public interface UlbService {
    /**
     * 获取ULB信息并入库
     *
     * @return
     */
    Integer describeULB();


    /**
     * 根据UlbId获取ULB信息并入库
     *
     * @param ulbInfo
     * @return
     */
    Integer describeULBByUlbId(UlbInfo ulbInfo);

    /**
     * 摘流量/挂流量
     *
     * @param ip
     * @param enabled 1 开  0 关
     * @return
     */
    Map<String,String> updateBackendAttribute(String ip, int backendPort, int enabled);

    /**
     * 批量插入数据
     *
     * @param list
     * @return
     */
    Integer batchInsertUlbInfo(List<UlbInfo> list);

    /**
     * 根据IP获取Ulb信息
     *
     * @param ulbInfo
     * @return
     */
    List<UlbInfo> getUlbInfoByIp(UlbInfo ulbInfo);

    /**
     * 根据IP和端口获取Ulb信息
     *
     * @param ulbInfo
     * @return
     */
    List<UlbInfo> getUlbInfoByIpAndBackendPort(UlbInfo ulbInfo);

    /**
     * 根据UlbID获取Ulb信息
     *
     * @param ulbInfo
     * @return
     */
    List<UlbInfo> getUlbInfoByUlbId(UlbInfo ulbInfo);

    /**
     * 根据IP更新数据
     *
     * @param ulbInfo
     * @return
     */
    Integer updateUlbInfoByIp(UlbInfo ulbInfo);

    /**
     * 删除
     * @return
     */
    Integer deleteAll();

}
