package com.mryx.matrix.publish.core.dao;

import com.mryx.matrix.publish.domain.UlbInfo;

import java.util.List;

/**
 * UlbInfoDao
 *
 * @author supeng
 * @date 2018/11/04
 */
public interface UlbInfoDao {
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
     * 根据IP获取Ulb信息
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
     * 删除数据
     * @return
     */
    Integer deleteAll();
}
