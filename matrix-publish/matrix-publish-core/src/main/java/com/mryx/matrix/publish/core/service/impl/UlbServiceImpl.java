package com.mryx.matrix.publish.core.service.impl;

import cn.ucloud.common.pojo.Account;
import cn.ucloud.ulb.client.DefaultULBClient;
import cn.ucloud.ulb.client.ULBClient;
import cn.ucloud.ulb.model.DescribeULBParam;
import cn.ucloud.ulb.model.DescribeULBResult;
import cn.ucloud.ulb.model.UpdateBackendAttributeParam;
import cn.ucloud.ulb.model.UpdateBackendAttributeResult;
import cn.ucloud.ulb.pojo.ULBConfig;
import com.alibaba.fastjson.JSONObject;
import com.mryx.matrix.publish.core.dao.UlbInfoDao;
import com.mryx.matrix.publish.core.service.UlbService;
import com.mryx.matrix.publish.domain.UlbInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * UlbServcice Impl
 *
 * @author supeng
 * @date 2018/10/31
 */
@Slf4j
@Service
public class UlbServiceImpl implements UlbService {
    /**
     * ulb publicKey
     */
    private static final String PublicKey = "u/1ZiMhXacZ9Jm0IgdCqBsJ/lKKLiULxmbbXvNV3R1c26rBa";
    /**
     * ulb privateKey
     */
    private static final String PrivateKey = "39a49275bf98f5d792eb6d9fce5aefedea599978";
    /**
     * 目前全部在北京二
     */
    private static final String Region = "cn-bj2";

    private ULBClient client = new DefaultULBClient(new ULBConfig(new Account(PrivateKey, PublicKey)));

    @Resource
    private UlbInfoDao ulbInfoDao;

    public void init() {
        if (client == null) {
            client = new DefaultULBClient(new ULBConfig(new Account(PrivateKey, PublicKey)));
        }
    }

    @Override
    public Integer describeULB() {
        int count = 0;
        try {
            DescribeULBParam param = new DescribeULBParam(Region);
            count = describeUlb(param);
        } catch (Exception e) {
            log.error("describeULB error ", e);
        }
        return count;
    }

    @Override
    public Integer describeULBByUlbId(UlbInfo ulbInfo) {
        log.info("describeULBByUlbId ulbInfo = {}", ulbInfo);
        int count = 0;
        if (ulbInfo == null || ulbInfo.getUlbId() == null || "".equals(ulbInfo.getUlbId())) {
            return count;
        }
        try {
            init();
            DescribeULBParam param = new DescribeULBParam(Region);
            param.setUlbId(ulbInfo.getUlbId());
            count = describeUlb(param);
        } catch (Exception e) {
            log.error("describeULB error ", e);
        }
        return count;
    }

    /**
     * 调用Ucloud接口获取ulb信息
     *
     * @param param
     * @return
     * @throws Exception
     */
    private int describeUlb(DescribeULBParam param) throws Exception {
        log.info("param = {}", JSONObject.toJSONString(param));
        int count = 0;
        List<UlbInfo> list = new ArrayList<>();
        DescribeULBResult describeULBResult = client.describeULB(param);
        log.info("describeULBResult = {}", JSONObject.toJSONString(describeULBResult));
        if (describeULBResult != null && describeULBResult.getRetCode() == 0 && describeULBResult.getUlbs() != null && !describeULBResult.getUlbs().isEmpty()) {
            for (DescribeULBResult.ULB ulb : describeULBResult.getUlbs()) {
                if (ulb == null || ulb.getUlbId() == null || "".equals(ulb.getUlbId()) || ulb.getUlbvServers() == null || ulb.getUlbvServers().isEmpty()) {
                    continue;
                }
                for (DescribeULBResult.ULBVServer ulbvServer : ulb.getUlbvServers()) {
                    List<DescribeULBResult.ULBBackend> ulbBackends = ulbvServer.getBackends();
                    if (ulbBackends == null || ulbBackends.isEmpty()) {
                        continue;
                    }
                    for (DescribeULBResult.ULBBackend backend : ulbBackends) {
                        UlbInfo newUlbInfo = new UlbInfo();
                        newUlbInfo.setUlbName(ulb.getName());
                        if (ulb.getPrivateIp() != null) {
                            newUlbInfo.setPrivateIp(ulb.getPrivateIp());
                        }else {
                            newUlbInfo.setPrivateIp("");
                        }
                        if (ulb.getIps() != null && !ulb.getIps().isEmpty() && ulb.getIps().get(0) != null) {
                            newUlbInfo.setEip(ulb.getIps().get(0).getEip());
                        }else {
                            newUlbInfo.setEip("");
                        }
                        newUlbInfo.setIp(backend.getPrivateIp());
                        newUlbInfo.setBackendPort(backend.getPort());
                        newUlbInfo.setUlbId(ulb.getUlbId());
                        newUlbInfo.setBackendId(backend.getBackendId());
                        list.add(newUlbInfo);
                    }
                }
            }
            count = ulbInfoDao.batchInsertUlbInfo(list);
            log.info("TotalCount = {},count = {}", describeULBResult.getTotalCount(), count);
        }
        return count;
    }

    @Override
    public Map<String, String> updateBackendAttribute(String ip, int backendPort, int enabled) {
        log.info("updateBackendAttribute ip = {},backendPort = {},enabled = {}", ip, backendPort, enabled);
        Map<String, String> returnMap = new HashMap<>();
        returnMap.put("code", "1");
        if (ip == null || "".equals(ip) || backendPort == 0 || (enabled != 0 && enabled != 1)) {
            if (enabled == 1) {
                returnMap.put("msg", "挂ULB参数错误！");
            } else {
                returnMap.put("msg", "摘ULB参数错误！");
            }
            return returnMap;
        }
        try {
            UlbInfo ulbInfo = new UlbInfo();
            ulbInfo.setIp(ip);
            ulbInfo.setBackendPort(backendPort);
            List<UlbInfo> resultList = ulbInfoDao.getUlbInfoByIpAndBackendPort(ulbInfo);
            log.info("getUlbInfoByIp resultList = {}", resultList);
            /**
             * 如果同一个IP存在多个ulb 全部摘掉
             */
            if (resultList == null || resultList.isEmpty()) {
                returnMap.put("code", "0");
                returnMap.put("msg", "服务【" + ip + ":" + backendPort + "]不存在ULB！");
                return returnMap;
            }
            //初始化client
            init();
            int count = 0;
            for (UlbInfo result : resultList) {
                if (result == null || result.getUlbId() == null || "".equals(result.getUlbId()) || result.getBackendId() == null || "".equals(result.getBackendId())) {
                    continue;
                }
                UpdateBackendAttributeParam param = new UpdateBackendAttributeParam(Region, result.getUlbId(), result.getBackendId());
                //开关 1：开 0：关
                param.setEnabled(enabled);
                UpdateBackendAttributeResult updateBackendAttributeResult = client.updateBackendAttribute(param);
                log.info("updateBackendAttributeResult = {}", updateBackendAttributeResult);
                if (updateBackendAttributeResult != null && updateBackendAttributeResult.getRetCode() == 0) {
                    count++;
                }
            }
            /**
             * TODO 多个LB的情况 全部摘掉/挂上才算成功
             */
            if (count == resultList.size()) {
                returnMap.put("code", "0");
                if (enabled == 1) {
                    returnMap.put("msg", "挂ULB成功！");
                } else {
                    returnMap.put("msg", "摘ULB成功！");
                }
                return returnMap;
            }
        } catch (Exception e) {
            log.error("updateBackendAttribute error ", e);
        }
        if (enabled == 1) {
            returnMap.put("msg", "挂ULB失败！");
        } else {
            returnMap.put("msg", "摘ULB失败！");
        }
        return returnMap;
    }

    @Override
    public Integer batchInsertUlbInfo(List<UlbInfo> list) {
        log.info("batchInsertUlbInfo list = {}", list);
        return ulbInfoDao.batchInsertUlbInfo(list);
    }

    @Override
    public List<UlbInfo> getUlbInfoByIp(UlbInfo ulbInfo) {
        log.info("getUlbInfoByIp ulbInfo = {}", ulbInfo);
        return ulbInfoDao.getUlbInfoByIp(ulbInfo);
    }

    @Override
    public List<UlbInfo> getUlbInfoByIpAndBackendPort(UlbInfo ulbInfo) {
        log.info("getUlbInfoByIpAndBackendPort ulbInfo = {}", ulbInfo);
        return ulbInfoDao.getUlbInfoByIpAndBackendPort(ulbInfo);
    }

    @Override
    public List<UlbInfo> getUlbInfoByUlbId(UlbInfo ulbInfo) {
        log.info("getUlbInfoByUlbId ulbInfo = {}", ulbInfo);
        return ulbInfoDao.getUlbInfoByUlbId(ulbInfo);
    }

    @Override
    public Integer updateUlbInfoByIp(UlbInfo ulbInfo) {
        log.info("updateUlbInfoByIp ulbInfo = {}", ulbInfo);
        return ulbInfoDao.updateUlbInfoByIp(ulbInfo);
    }

    @Override
    public Integer deleteAll() {
        log.info("deleteAll");
        return ulbInfoDao.deleteAll();
    }

    public static void main(String[] args) {
//        ULBClient client = new DefaultULBClient(new ULBConfig(new Account(PrivateKey, PublicKey)));
//        DescribeULBParam param = new DescribeULBParam(Region);
//        try {
//            DescribeULBResult result = client.describeULB(param);
//            log.info("result = {}", JSONObject.toJSONString(result));
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

//        UpdateBackendAttributeParam param2 = new UpdateBackendAttributeParam(Region, " ulb-t5fbgi", "backend-nr1tga");
//        //开关 1：开 0：关
//        param2.setEnabled(1);
//        try {
//            UpdateBackendAttributeResult updateBackendAttributeResult = client.updateBackendAttribute(param2);
//            log.info("updateBackendAttributeResult = {}", updateBackendAttributeResult);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

    }
}
