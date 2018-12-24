package com.mryx.matrix.process.web.controller;

import com.mryx.common.utils.StringUtils;
import com.mryx.matrix.common.domain.ResultVo;
import com.mryx.matrix.process.core.service.AppDeployConfigService;
import com.mryx.matrix.process.core.service.AppServerService;
import com.mryx.matrix.process.core.service.AppService;
import com.mryx.matrix.process.domain.App;
import com.mryx.matrix.process.domain.AppDeployConfig;
import com.mryx.matrix.process.domain.AppServer;
import com.mryx.matrix.process.dto.AppInfoDto;
import com.mryx.matrix.process.dto.AppServerDTO;
import com.mryx.matrix.process.dto.GroupInfoDto;
import com.mryx.matrix.process.dto.ServerResourceDto;
import com.mryx.matrix.process.web.vo.Pagination;
import com.mryx.matrix.publish.dto.AgentDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author pengcheng
 * @description 机器与应用映射关系Controller
 * @email pengcheng@missfresh.cn
 * @date 2018-10-23 11:07
 **/
@RestController
@RequestMapping("/api/process/appServer")
@Slf4j
public class AppServerController {

    @Autowired
    private AppServerService appServerService;

    @Autowired
    private AppService appService;

    @Autowired
    private AppDeployConfigService appDeployConfigService;

//    @Resource
//    private RedisTemplate redisTemplate;

    /**
     * 根据app code，分组信息，IP地址确定出一条记录，修改状态和标识
     */
    @RequestMapping("/update")
    @ResponseBody
    public ResultVo<AppServer> update(@RequestBody AppServer appServer) {
        try {
            String appCode = appServer.getAppCode();
            String groupName = appServer.getGroupName();
            String hostIP = appServer.getHostIp();

            AppServerDTO condition = new AppServerDTO();
            condition.setAppCode(appCode);
            condition.setGroupName(groupName);
            condition.setHostIp(hostIP);
            condition.setDelFlag(1);

            List<AppServer> appServerList = appServerService.listByCondition(condition);
            if (CollectionUtils.isEmpty(appServerList)) {
                log.error("could not found app server mapping , app code is {} , group name is {} , host ip is {}", appCode, groupName, hostIP);
                return ResultVo.Builder.FAIL().initErrCodeAndMsg("1000", "could not found app server mapping");
            }

            appServerList.stream().forEach(appServer1 -> {
                appServer1.setStatus(appServer.getStatus());
                appServer1.setTag(appServer.getTag());
                appServerService.updateById(appServer1);
            });
            return ResultVo.Builder.SUCC().initSuccData(appServerList.get(0));
        } catch (Exception e) {
            log.error("update app server mapping failure , Exception", e);
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("1000", "系统错误");
        }
    }

    @RequestMapping("/getServerCount")
    @ResponseBody
    public ResultVo<Integer> getServerCount(@RequestBody AppServer appServer) {
        try {

            String appCode = appServer.getAppCode();
            String groupName = appServer.getGroupName();

            String key = appCode + groupName;

//            ValueOperations<String, Object> ops = redisTemplate.opsForValue();
//            Integer count = (Integer) ops.get(key);
//            if (count != null) {
//                return ResultVo.Builder.SUCC().initSuccData(count);
//            }

            AppServerDTO condition = new AppServerDTO();
            condition.setAppCode(appCode);
            condition.setGroupName(groupName);
            condition.setDelFlag(1);

            List<AppServer> appServerList = appServerService.listByCondition(condition);
            if (appServerList == null) {
                return ResultVo.Builder.SUCC().initSuccData(0);
            }
            int count = appServerList.size();

//            ops.set(key, count, 1, TimeUnit.DAYS);
            return ResultVo.Builder.SUCC().initSuccData(appServerList.size());
        } catch (Exception e) {
            log.error("get server count failure , Exception", e);
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("1000", "系统错误");
        }
    }

    @RequestMapping("/getAppConfig")
    @ResponseBody
    public ResultVo<AppInfoDto> getAppConfig(@RequestBody AppDeployConfig appDeployConfig) {

        String appCode = appDeployConfig.getAppCode();
        String envCode = appDeployConfig.getAppEnv();

        AppInfoDto appInfoDto = new AppInfoDto();
        appInfoDto.setAppCode(appCode);

        App app = new App();
        app.setAppCode(appCode);
        app.setDelFlag(1);
        List<App> appList = appService.listByCondition(app);
        if (CollectionUtils.isEmpty(appList)) {
            log.error("无法找到应用信息: {}", appCode);
            ResultVo resultVo = ResultVo.Builder.FAIL().initErrCodeAndMsg("1000", "无法找到应用信息");
            resultVo.setData(null);
            return resultVo;
        } else {
            String appName = appList.get(0).getAppName();
            appInfoDto.setAppName(appName);
        }

        AppDeployConfig condition = new AppDeployConfig();
        condition.setAppEnv(envCode);
        condition.setAppCode(appCode);
        condition.setDelFlag(1);

        List<AppDeployConfig> appDeployConfigList = this.appDeployConfigService.listByCondition(condition);
        if (CollectionUtils.isEmpty(appDeployConfigList)) {
            log.error("没有找到应用的部署信息： app code: {}, env code: {}", appCode, envCode);
            ResultVo resultVo = ResultVo.Builder.FAIL().initErrCodeAndMsg("1000", "没有找到应用的部署信息");
            resultVo.setData(null);
            return resultVo;
        } else {
            AppDeployConfig ret = appDeployConfigList.get(0);
            appInfoDto.setGit(ret.getGit());
            appInfoDto.setDeployPath(ret.getDeployPath());
            appInfoDto.setHealthcheck(ret.getHealthcheck());
            appInfoDto.setDeployParameters(ret.getDeployParameters());
            appInfoDto.setPkgName(ret.getPkgName());
            appInfoDto.setPkgType(ret.getPkgType());
            appInfoDto.setPort(ret.getPort());
            appInfoDto.setVmOption(ret.getVmOption());
        }

        AppServerDTO appServer = new AppServerDTO();
        appServer.setDelFlag(1);
        appServer.setAppCode(appCode);
        List<AppServer> appServerList = appServerService.listByCondition(appServer);
        if (CollectionUtils.isEmpty(appServerList)) {
            log.error("没有找到机器分配信息: {}", appCode);
        } else {
            Map<String, List<ServerResourceDto>> map = new HashMap<>();
            appServerList.stream().forEach(appServer1 -> {
                String groupName = appServer1.getGroupName();
                String hostIp = appServer1.getHostIp();

                ServerResourceDto serverResourceDto = new ServerResourceDto();
                serverResourceDto.setHostIp(hostIp);

                List<ServerResourceDto> serverResourceDtoList = map.get(groupName);
                if (serverResourceDtoList == null) {
                    serverResourceDtoList = new ArrayList<>();
                }
                serverResourceDtoList.add(serverResourceDto);
                map.put(groupName, serverResourceDtoList);
            });
            if (!map.isEmpty()) {
                List<GroupInfoDto> groupInfoDtoList = new ArrayList<>(map.size());
                map.keySet().forEach(key -> {
                    GroupInfoDto groupInfoDto = new GroupInfoDto();
                    groupInfoDto.setGroupName(key);
                    groupInfoDto.setServerInfo(map.get(key));
                    groupInfoDtoList.add(groupInfoDto);
                });
                appInfoDto.setGroupInfo(groupInfoDtoList);
            }
        }

        ResultVo resultVo = ResultVo.Builder.SUCC().initSuccDataAndMsg("0", "查询成功");
        resultVo.setData(appInfoDto);
        return resultVo;
    }

    /**
     * 获取机器列表
     *
     * @param parameter
     * @return
     */
    @PostMapping(value = "/list", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResultVo list(@RequestBody AppServer parameter) {
        try {
            AppServerDTO condition = new AppServerDTO();
            BeanUtils.copyProperties(parameter, condition);
            String appCode = condition.getAppCode();
            if (!StringUtils.isEmpty(appCode)) {
                condition.setAppCodeLike(appCode);
                condition.setAppCode(null);
            }
            String appName = condition.getAppName();
            if (!StringUtils.isEmpty(appName)) {
                condition.setAppNameLike(appName);
                condition.setAppName(null);
            }
            String hostIp = condition.getHostIp();
            if (!StringUtils.isEmpty(hostIp)) {
                condition.setHostIpLike(hostIp);
                condition.setHostIp(null);
            }

            Integer agentStatus = condition.getAgentStatus();
            if (agentStatus != null) {
                condition.setMsReport(System.currentTimeMillis() - 1000 * 60 * 5);
            }

            Integer total = appServerService.pageTotal(condition);
            Pagination<AppServer> pagination = new Pagination<>();
            pagination.setPageSize(parameter.getPageSize());
            pagination.setTotalPageForTotalSize(total);
            if (total <= 0) {
                pagination.setDataList(new ArrayList<>());
                return ResultVo.Builder.SUCC().initSuccData(pagination);
            }
            List<AppServer> appServerList = appServerService.listPage(condition);
            pagination.setDataList(appServerList);
            return ResultVo.Builder.SUCC().initSuccData(pagination);
        } catch (Exception e) {
            log.error("获取机器列表失败", e);
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("2000", e.getMessage());
        }
    }

    @PostMapping(value = "/updateStatus", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResultVo updateStatus(@RequestBody AppServer paramter) {
        try {
            appServerService.batchUpdateByIp(paramter);
            return ResultVo.Builder.SUCC();
        } catch (Exception e) {
            log.error("更新Agent状态失败", e);
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("2000", e.getMessage());
        }
    }

    @PostMapping(value = "/listAllIps", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResultVo listAllIps(@RequestBody AppServer paramter) {
        try {
            List<String> hostIps = appServerService.listAllIps();
            return ResultVo.Builder.SUCC().initSuccData(hostIps);
        } catch (Exception e) {
            log.error("获取IP列表失败", e);
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("2000", e.getMessage());
        }
    }
}

