package com.mryx.matrix.publish.provider;

import com.mryx.common.utils.HttpClientUtil;
import com.mryx.matrix.common.domain.HttpClientResult;
import com.mryx.matrix.common.dto.AppInfoDto;
import com.mryx.matrix.common.dto.GroupInfoDto;
import com.mryx.matrix.common.dto.ServerResourceDto;
import com.mryx.matrix.common.util.HttpClientUtils;
import com.mryx.matrix.publish.AgentService;
import com.mryx.matrix.publish.dto.AgentDTO;
import com.mryx.matrix.publish.enums.AgentStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

/**
 * Agent Service impl
 *
 * @author supeng
 * @date 2018/09/17
 */
@Service("agentService")
public class AgentServiceImpl implements AgentService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AgentServiceImpl.class);

    @Resource
    private RedisTemplate redisTemplate;

    @Override
    public AgentDTO getAgentInfo(String key) {
        LOGGER.info("getAgentInfo key = {}", key);
        Map<String, Object> map = (Map<String, Object>) redisTemplate.opsForValue().get(key);
        if (Objects.isNull(map)) {
            return null;
        }
        AgentDTO agentDTO = new AgentDTO(null, null, null, null, getAgentStatus(key), String.valueOf(map.get("ip")), String.valueOf(map.get("java")),
                String.valueOf(map.get("cpu")), String.valueOf(map.get("load")),
                String.valueOf(map.get("mem")), String.valueOf(map.get("disk")),
                String.valueOf(map.get("os")), null);
        // TODO
        LOGGER.info("getAgentInfo agentDTO = {}", agentDTO.toString());
        return agentDTO;
    }

    @Override
    public List<AgentDTO> getAppAgentInfoList(AppInfoDto appInfoDto) {
        if (appInfoDto == null || appInfoDto.getAppCode() == null
                || appInfoDto.getGroupInfo() == null || appInfoDto.getGroupInfo().isEmpty()) {
            return new ArrayList<>();
        }
        List<AgentDTO> agentDTOS = new ArrayList<>();
        assemAgentDTOS(appInfoDto, agentDTOS);
        return agentDTOS;
    }

    /**
     * 拼装AgentDTOS数据
     *
     * @param appInfoDto
     * @param agentDTOS
     * @return
     */
    private void assemAgentDTOS(AppInfoDto appInfoDto, List<AgentDTO> agentDTOS) {
        List<GroupInfoDto> groupInfoDtos = appInfoDto.getGroupInfo();
        for (GroupInfoDto groupInfoDto : groupInfoDtos) {
            if (groupInfoDto == null || groupInfoDto.getServerInfo() == null || groupInfoDto.getServerInfo().isEmpty()) {
                continue;
            }
            List<ServerResourceDto> serverResourceDtos = groupInfoDto.getServerInfo();
            for (ServerResourceDto serverResourceDto : serverResourceDtos) {
                if (serverResourceDto == null || serverResourceDto.getHostIp() == null) {
                    continue;
                }
                Map<String, Object> map = (Map<String, Object>) redisTemplate.opsForValue().get(serverResourceDto.getHostIp());
                if (Objects.isNull(map)) {
                    map = new HashMap<>();
                }
                AgentDTO agentDTO = new AgentDTO(appInfoDto.getAppCode(), appInfoDto.getAppName(), null, null, getAgentStatus(serverResourceDto.getHostIp()), String.valueOf(map.get("ip")), String.valueOf(map.get("java")),
                        String.valueOf(map.get("cpu")), String.valueOf(map.get("load")),
                        String.valueOf(map.get("mem")), String.valueOf(map.get("disk")),
                        String.valueOf(map.get("os")), null);
                // TODO
                agentDTOS.add(agentDTO);
            }
        }
    }

    @Override
    public List<AgentDTO> getAgentInfoList(List<AppInfoDto> appInfoDtos) {
        if (appInfoDtos == null || appInfoDtos.isEmpty()) {
            return new ArrayList<>();
        }
        List<AgentDTO> agentDTOS = new ArrayList<>();
        for (AppInfoDto appInfoDto : appInfoDtos) {
            if (appInfoDto == null || appInfoDto.getGroupInfo() == null) {
                continue;
            }
            assemAgentDTOS(appInfoDto, agentDTOS);
        }
        return agentDTOS;
    }

    /**
     * 获取Agent状态
     *
     * @param ip
     * @return
     */
    public String getAgentStatus(String ip) {
        String url = "http://" + ip + ":30000/api/healthcheck";
        try {
            HttpClientResult result = HttpClientUtils.doPost(url);
            if (result.getCode() == 200) {
                return AgentStatus.ONLINE.getCommand();
            }
        } catch (Exception e) {
            LOGGER.error("agent healthcheck error ", e);
        }
        return AgentStatus.OFFLINE.getCommand();
    }
}
