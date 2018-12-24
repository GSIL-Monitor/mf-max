package com.mryx.matrix.publish.web.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.mryx.common.utils.HttpPoolClient;
import com.mryx.common.utils.StringUtils;
import com.mryx.matrix.common.domain.ResultVo;
import com.mryx.matrix.common.enums.ResultType;
import com.mryx.matrix.common.util.ObjectMapUtil;
import com.mryx.matrix.publish.core.producer.StartAppProducer;
import com.mryx.matrix.publish.core.service.AppStartResultService;
import com.mryx.matrix.publish.domain.AppServer;
import com.mryx.matrix.publish.domain.AppStartResult;
import com.mryx.matrix.publish.dto.AgentDTO;
import com.mryx.matrix.publish.dto.AppStartDTO;
import com.mryx.matrix.publish.enums.AgentStatus;
import com.mryx.matrix.publish.web.vo.AppServerVo;
import com.mryx.matrix.publish.web.vo.Pagination;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.*;


/**
 * 与Agent交互
 *
 * @author supeng
 * @date 2018/09/03
 */
@Slf4j
@RestController
@RequestMapping("/api/publish/agent")
public class AgentContorller {

    private final HttpPoolClient HTTP_POOL_CLIENT = com.mryx.common.utils.HttpClientUtil.create(2000, 2000, 5, 5, 1, 500);

    @Resource
    private RedisTemplate redisTemplate;

    @Autowired
    private StartAppProducer startAppProducer;

    @Autowired
    private AppStartResultService appStartResultService;

    @Value("${agentIpList}")
    private String agentIpList;

    @Value("${listAllIps}")
    private String listAllIps;
//    @Autowired
//    private AppServerService appServerService;

//    ThreadFactory namedThreadFactory = new ThreadFactoryBuilder().setNameFormat("agent-pool-%d").build();
//
//    ExecutorService executor = new ThreadPoolExecutor(30, 30, 0, TimeUnit.MICROSECONDS, new LinkedBlockingQueue<>(1024), namedThreadFactory, new ThreadPoolExecutor.AbortPolicy());

    /**
     * 收集Agent上报数据
     * TODO 废弃 后面改为MQ方式
     *
     * @return
     */
    @PostMapping(path = "/receive", produces = MediaType.APPLICATION_JSON_VALUE)
    public Object receive(@RequestBody AgentDTO agentDTO) {
        log.info("AgentDTO = {}", agentDTO);
        redisTemplate.opsForValue().set(agentDTO.getIp(), ObjectMapUtil.beanToMap(agentDTO));
        return "success";
    }

    /**
     * 测试Redis
     *
     * @return
     */
    @PostMapping(path = "/get")
    public Object get() {
        Map<String, Object> map = (Map<String, Object>) redisTemplate.opsForValue().get("192.168.96.98");
        log.info("----" + map.toString() + "--" + map.get("disk"));
        return "success";
    }

    /**
     * 启动应用
     *
     * @param appStartDTO
     * @return
     */
    @PostMapping(value = "/startapp", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResultVo startApp(@RequestBody AppStartDTO appStartDTO) {
        log.info("------" + appStartDTO.toString() + "------");
        return startAppProducer.send(appStartDTO);
    }

    /**
     * 获取启动结果
     *
     * @param appStartResult
     * @return
     */
    @PostMapping(value = "/getresult", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResultVo getResult(@RequestBody AppStartResult appStartResult) {
        log.info("getresult appStartResult = {}", appStartResult);
        if (appStartResult == null || appStartResult.getIp() == null || "".equals(appStartResult.getIp())
                || appStartResult.getRecordId() == null || "".equals(appStartResult.getRecordId())
                || appStartResult.getBuildType() == null || "".equals(appStartResult.getBuildType())) {
            return ResultVo.Builder.FAIL().initErrCodeAndMsg(ResultType.PARAMETER_ERROR.getCode(), ResultType.PARAMETER_ERROR.getMessage());
        }
        try {
            //TODO 发布和回滚记录是一张表吗?
            AppStartResult result = appStartResultService.selectByParameter(appStartResult);
            log.info("getresult result = {}", result);
            if (result != null && result.getId() != null && result.getId() != 0) {
                if ("0".equals(result.getResultCode())) {
                    return ResultVo.Builder.SUCC().initSuccData(result);
                } else {
                    return ResultVo.Builder.FAIL().initErrCodeAndMsg("1", result.getMessage());
                }
            } else {
                return ResultVo.Builder.FAIL().initErrCodeAndMsg("4", "暂无启动结果！");
            }
        } catch (Exception e) {
            log.error("getresult error ", e);
        }
        return ResultVo.Builder.FAIL().initErrCodeAndMsg(ResultType.SYSTEM_EXCEPTION.getCode(), ResultType.SYSTEM_EXCEPTION.getMessage());
    }

    /**
     * 操作Agent 更新/重启/获取服务器信息/...
     *
     * @param params
     * @return
     */
    @PostMapping(value = "/handleAgent", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResultVo handleAgent(@RequestBody AppStartDTO params) {
        log.info("handleAgent params = {}", params);
        if (params == null || (StringUtils.isEmpty(params.getIp()) && !"all".equals(params.getMode()))
                || (!"upgrade".equals(params.getAction()) && !"restart".equals(params.getAction()))) {
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("1", "参数错误！");
        }
        try {
            String[] ips;
            if ("all".equals(params.getMode())) {
                Optional<String> optional = HTTP_POOL_CLIENT.postJson(listAllIps, JSON.toJSONString(params));
                List<String> object = JSONObject.parseArray(JSONObject.parseObject(optional.get()).get("data").toString(), String.class);
                ips = object.toArray(new String[0]);
            } else {
                ips = params.getIp().split(",");
            }
            int count = 0;
            Set<String> deduplicate = new HashSet<>();
            for (String ip : ips) {
                if (ip == null || "".equals(ip)) {
                    continue;
                }
                if (deduplicate.contains(ip)) {
                    continue;
                }
                deduplicate.add(ip);

                AppStartDTO appStartDTO = new AppStartDTO();
                appStartDTO.setIp(ip);
                appStartDTO.setAction(params.getAction());
                appStartDTO.setBuildType("release");
                appStartDTO.setPackageType("agent");
                appStartDTO.setPackageName("matrix-agent");
                appStartDTO.setPackagePath("/data/app");
                appStartDTO.setPackageUrl("http://matrix-storage.missfresh.net/matrix-agent.zip");
                appStartDTO.setMd5Name("upgradeAgent");

                ResultVo resultVo = startAppProducer.send(appStartDTO);
                log.info("send resultVo = {}", JSON.toJSONString(resultVo));
                if (resultVo != null && "0".equals(resultVo.getCode())) {
                    count++;
                }
            }
            return ResultVo.Builder.SUCC().initSuccData(count);
        } catch (Exception e) {
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("4000", "更新失败： " + e.getMessage());
        }
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
            Optional<String> optional = HTTP_POOL_CLIENT.postJson(agentIpList, JSON.toJSONString(parameter));

            Pagination<AppServerVo> pagination = new Pagination<>();
            pagination.setPageNo(parameter.getPageNo());
            pagination.setPageSize(parameter.getPageSize());

            String json = optional.get();
            JSONObject jsonObject = JSONObject.parseObject(json);
            JSONObject data = jsonObject.getJSONObject("data");

            Integer totalSize = data.getInteger("totalSize");
            if (totalSize <= 0) {
                pagination.setDataList(new ArrayList<>());
                return ResultVo.Builder.SUCC().initSuccData(pagination);
            }
            Integer totalPage = data.getInteger("totalPage");
            pagination.setTotalPage(totalPage);
            pagination.setTotalSize(totalSize);
            List<AppServerVo> appServerVoList = new ArrayList<>();
            List<AppServer> appServerList = JSON.parseArray(data.getString("dataList"), AppServer.class);
            Map<String, AgentDTO> ipMap = new HashMap<>();
            appServerList.stream().forEach(appServer -> {
                AppServerVo vo = new AppServerVo();
                BeanUtils.copyProperties(appServer, vo);
                String ip = appServer.getHostIp();
                AgentDTO dto = ipMap.get(ip);
                if (dto == null) {
                    try {
                        String value = (String) redisTemplate.opsForValue().get("matrix:report:" + ip);
                        dto = JSONObject.parseObject(value, AgentDTO.class);
                    } catch (Exception e) {
                        log.error("获取IP缓存异常：" + ip, e);
                    }
                    if (dto == null) {
                        dto = new AgentDTO();
                    }
                    ipMap.put(ip, dto);
                }
                AgentStatus agentStatus = AgentStatus.OFFLINE;
                Date updateTime = dto.getAgentUpdateTime();
                if (updateTime != null) {
                    Date now = new Date();
                    if (now.getTime() - updateTime.getTime() < 1000 * 60 * 5) {
                        agentStatus = AgentStatus.ONLINE;
                    }
                }
                vo.setIp(ip);
                vo.setAgentStatus(agentStatus.getValue());
                vo.setAgentStatusDesc(agentStatus.getCommand());
                vo.setJava(dto.getJava());
                vo.setCpu(dto.getCpu());
                vo.setLoad(dto.getLoad());
                vo.setMem(dto.getMem());
                vo.setDisk(dto.getDisk());
                vo.setOs(dto.getOs());
                vo.setAgentCreateTime(dto.getAgentCreateTime());
                vo.setAgentStartTime(dto.getAgentStartTime());
                vo.setAgentUpdateTime(dto.getAgentUpdateTime());
                appServerVoList.add(vo);
            });
            pagination.setDataList(appServerVoList);
            return ResultVo.Builder.SUCC().initSuccData(pagination);
        } catch (Exception e) {
            log.error("", e);
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("2000", e.getMessage());
        }
    }
}
