package com.mryx.matrix.process.web.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.mryx.common.utils.HttpPoolClient;
import com.mryx.common.utils.StringUtils;
import com.mryx.matrix.common.domain.ResultVo;
import com.mryx.matrix.process.core.service.AppDeployConfigService;
import com.mryx.matrix.process.core.service.AppServerService;
import com.mryx.matrix.process.core.service.AppService;
import com.mryx.matrix.process.core.utils.TreeUtil;
import com.mryx.matrix.process.domain.App;
import com.mryx.matrix.process.domain.AppDeployConfig;
import com.mryx.matrix.process.domain.AppServer;
import com.mryx.matrix.process.dto.*;
import com.mryx.matrix.process.web.param.DeptDto;
import io.swagger.models.auth.In;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.TimeUnit;


/**
 * @author pengcheng
 * @description
 * @email pengcheng@missfresh.cn
 * @date 2018-10-18 20:23
 **/
@RestController
@RequestMapping("/api/process/sync/")
@Slf4j
public class DataSyncController {

    @Autowired
    private AppService appService;

    @Autowired
    private AppDeployConfigService appDeployConfigService;

    @Autowired
    private AppServerService appServerService;

//    @Resource
//    private RedisTemplate redisTemplate;

    private final HttpPoolClient HTTP_POOL_CLIENT = com.mryx.common.utils.HttpClientUtil.create(2000, 2000, 5, 5, 1, 500);

    @Value("${firstDept_remote}")
    private String listAllFirstDept;

    @Value("${appTree_remote}")
    private String appTreeRemote;

    @Value("${appDetail_remote}")
    private String getAppInfoByAppCode;
//    private final String listAllFirstDept = "http://appcenter.missfresh.cn/openapi/app/listAllFirstDept";
//    private final String appTreeRemote = "http://appcenter.missfresh.cn/openapi/app/getDeptAppTree";
//    private final String getAppInfoByAppCode = "http://appcenter.missfresh.cn/openapi/app/getAppInfoByAppCode";

    /**
     * 部署环境枚举
     */
    enum EvnCode {
        DEV("dev"),
        BETA("beta"),
        PROD("prod"),
        STRESS("stress");

        EvnCode(String desc) {
            this.desc = desc;
        }

        private String desc;

        public String getDesc() {
            return desc;
        }
    }

    /**
     * 失败标识
     */
    private static final String FAIL = "fail";

    // 如果需要项目启动后直接同步应用信息，需要打开下面注解
    //    @PostConstruct
    @RequestMapping("update")
    public Object dataSync() {
        Optional<String> optional = HTTP_POOL_CLIENT.postJson(listAllFirstDept, "{}");
        String response = optional.isPresent() ? optional.get() : "";
        ResultVo resultVo = JSON.parseObject(response, ResultVo.class);

        List<Object> deptDtoList = (List<Object>) resultVo.getData();

        List<App> appList = new ArrayList<>();
        List<AppDeployConfig> appDeployConfigList = new ArrayList<>();
        List<AppServer> appServerList = new ArrayList<>();

        Map<String, Integer> cache = new HashMap<>();
        Date now = new Date();
        deptDtoList.stream().forEach(deptDto -> {

            DeptDto dept = JSON.toJavaObject((JSON) deptDto, DeptDto.class);
            String deptId = dept.getDeptId().toString();

            if (StringUtils.isEmpty(deptId)) {
                log.info("error: {}");
                return;
            }

            Map mapInfo = new HashMap<>();
            mapInfo.put("deptId", deptId);
            Optional<String> appInfoList = HTTP_POOL_CLIENT.postJson(appTreeRemote, JSONObject.toJSONString(mapInfo));
            List<AppListsDto> appCodeAndNameDtoList = new ArrayList<>();

            JSONObject apps = (JSONObject) JSONObject.parse(appInfoList.get().toString());
            ResultVo vo = JSON.toJavaObject(apps, ResultVo.class);
            if (FAIL.equals(vo.getRet())) {
                log.error("{}", vo.getMessage());
                log.info("map : {}", mapInfo);
                return;
            }
            DeptAppTree tree = JSONObject.parseObject(apps.get("data").toString(), DeptAppTree.class);

            TreeUtil.merge(tree, appCodeAndNameDtoList, null);

            Map mapInfo2 = new HashMap();
            appCodeAndNameDtoList.stream().forEach(app -> {
                App a = new App();
                a.setAppName(app.getAppName());
                a.setDeptId(Long.valueOf(app.getDeptId()).intValue());
                a.setAppCode(app.getAppCode());
                a.setGmtModified(now);
                appList.add(a);

                mapInfo2.put("appCode", app.getAppCode());
                for (EvnCode evnCode : EvnCode.values()) {
                    mapInfo2.put("envCode", evnCode.getDesc());
                    Optional<String> appListDetail = HTTP_POOL_CLIENT.postJson(getAppInfoByAppCode, JSONObject.toJSONString(mapInfo2));
                    ResultVo vo2 = JSON.toJavaObject(JSON.parseObject(appListDetail.get()), ResultVo.class);

                    if (FAIL.equals(vo2.getRet())) {
                        log.error("error: {}; mapInfo2: {}", vo2.getMessage(), mapInfo2);
                        break;
                    }

                    AppInfoDto appInfoDto = JSON.toJavaObject((JSON) vo2.getData(), AppInfoDto.class);

                    AppDeployConfig appDeployConfig = new AppDeployConfig();
                    appDeployConfig.setAppCode(appInfoDto.getAppCode());
                    appDeployConfig.setAppEnv(evnCode.getDesc());
                    appDeployConfig.setDeployParameters(appInfoDto.getDeployParameters());
                    appDeployConfig.setDeployPath(appInfoDto.getDeployPath());
                    appDeployConfig.setGit(appInfoDto.getGit());
                    appDeployConfig.setGmtModified(now);
                    appDeployConfig.setHealthcheck(appInfoDto.getHealthcheck());
                    appDeployConfig.setPkgName(appInfoDto.getPkgName());
                    appDeployConfig.setPkgType(appInfoDto.getPkgType());
                    appDeployConfig.setPort(appInfoDto.getPort());
                    appDeployConfig.setVmOption(appInfoDto.getVmOption());
                    appDeployConfigList.add(appDeployConfig);

                    List<GroupInfoDto> groupInfoList = appInfoDto.getGroupInfo();

                    if (!CollectionUtils.isEmpty(groupInfoList)) {
                        groupInfoList.stream().forEach(groupInfo -> {
                            String groupName = groupInfo.getGroupName();
                            List<ServerResourceDto> serverInfoList = groupInfo.getServerInfo();
                            if (!CollectionUtils.isEmpty(serverInfoList)) {
                                serverInfoList.stream().forEach(serverInfo -> {
                                    if (!StringUtils.isEmpty(serverInfo.getHostIp())) {
                                        AppServer appServer = new AppServer();
                                        appServer.setAppCode(appInfoDto.getAppCode());
                                        appServer.setAppName(appInfoDto.getAppName());
                                        appServer.setDeptId(app.getDeptId());
                                        appServer.setDeptName(app.getDeptName());
                                        appServer.setGroupName(groupName);
                                        appServer.setHostIp(serverInfo.getHostIp().trim());
                                        appServer.setGmtModified(now);
                                        appServerList.add(appServer);

                                        String key = appInfoDto.getAppCode() + groupName;
                                        Integer count = cache.get(key);
                                        if (count == null) {
                                            count = new Integer(1);
                                        } else {
                                            count++;
                                        }
                                        cache.put(key, count);
                                    }
                                });
                            }
                        });
                    }
                }
            });
        });
        log.info("app total count: {}", appList.size());

        this.updateCache(cache);

        if (appList.size() > 0) {
            //现将目前未删除的所有应用记录设置为删除状态
            App condition = new App();
            condition.setDelFlag(1);
            List<App> list = appService.listByCondition(condition);
            Map<String, App> appMap = new HashMap<>();
            if (!CollectionUtils.isEmpty(list)) {
                list.stream().forEach(app -> {
                    String appCode = app.getAppCode();
                    appMap.put(appCode, app);
                    app.setDelFlag(0);
                });
                appService.batchUpdateOrInsert(list);
            }

            //遍历新的应用列表，如果旧的应用列表中包含相同app code的应用记录，直接更新应用记录的id，否则作为新纪录插入，同时修改删除标识
            appList.stream().forEach(app -> {
                String appCode = app.getAppCode();
                if (appMap.get(appCode) != null) {
                    app.setId(appMap.get(appCode).getId());
                }
                app.setDelFlag(1);
            });
            System.out.println("update app: " + this.appService.batchUpdateOrInsert(appList));
        }

        if (appDeployConfigList.size() > 0) {
            AppDeployConfig condition = new AppDeployConfig();
            condition.setDelFlag(1);
            List<AppDeployConfig> list = appDeployConfigService.listByCondition(condition);
            Map<String, AppDeployConfig> appDeployConfigMap = new HashMap<>();
            if (!CollectionUtils.isEmpty(list)) {
                list.stream().forEach(appDeployConfig -> {
                    String appCode = appDeployConfig.getAppCode();
                    String envCode = appDeployConfig.getAppEnv();
                    String combine = appCode + envCode;
                    appDeployConfigMap.put(combine, appDeployConfig);
                    appDeployConfig.setDelFlag(0);
                });
                appDeployConfigService.batchUpdateOrInsert(list);
            }

            //遍历新的应用列表，如果旧的应用列表中包含相同app code的应用记录，直接更新应用记录的id，否则作为新纪录插入，同时修改删除标识
            appDeployConfigList.stream().forEach(app -> {
                String appCode = app.getAppCode();
                String envCode = app.getAppEnv();
                String combine = appCode + envCode;
                if (appDeployConfigMap.get(combine) != null) {
                    app.setId(appDeployConfigMap.get(combine).getId());
                }
                app.setDelFlag(1);
            });
            System.out.println("update app config: " + this.appDeployConfigService.batchUpdateOrInsert(appDeployConfigList));
        }

        if (appServerList.size() > 0) {
            AppServerDTO condition = new AppServerDTO();
            condition.setDelFlag(1);
            List<AppServer> list = appServerService.listByCondition(condition);
            Map<String, AppServer> appServerMap = new HashMap<>();
            if (!CollectionUtils.isEmpty(list)) {
                list.stream().forEach(appServer -> {
                    String appCode = appServer.getAppCode();
                    String hostIp = appServer.getHostIp();
                    String groupName = appServer.getGroupName();
                    String combine = hostIp + appCode + groupName;
                    appServerMap.put(combine, appServer);
                    appServer.setDelFlag(0);
                });
                appServerService.batchUpdateOrInsert(list);
            }

            //遍历新的应用列表，如果旧的应用列表中包含相同app code的应用记录，直接更新应用记录的id，否则作为新纪录插入，同时修改删除标识
            appServerList.stream().forEach(appServer -> {
                String hostIp = appServer.getHostIp();
                String appCode = appServer.getAppCode();
                String groupName = appServer.getGroupName();
                String combine = hostIp + appCode + groupName;
                if (appServerMap.get(combine) != null) {
                    appServer.setId(appServerMap.get(combine).getId());
                }
                appServer.setGmtCreate(now);
                appServer.setDelFlag(1);
            });

            System.out.println("update app server: " + this.appServerService.batchUpdateOrInsert(appServerList));
        }

        return "success";
    }

    /**
     * 应用在每个分组下的机器数量
     *
     * @param cache
     */
    private void updateCache(Map<String, Integer> cache) {
//        ValueOperations<String, Object> ops = redisTemplate.opsForValue();
//
//        for (Map.Entry<String, Integer> entry : cache.entrySet()) {
//            String key = entry.getKey();
//            Integer count = entry.getValue();
//            ops.set(key, count, 1, TimeUnit.DAYS);
//        }
    }
}
