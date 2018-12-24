package com.mryx.matrix.publish;

import com.mryx.matrix.common.dto.AppInfoDto;
import com.mryx.matrix.publish.dto.AgentDTO;

import java.util.List;

/**
 * Agent Service
 *
 * @author supeng
 * @date 2018/09/17
 */
public interface AgentService {
    /**
     * 获取Agent信息
     *
     * @param key ip
     * @return
     */
    AgentDTO getAgentInfo(String key);

    /**
     * 获取某个应用的Agent信息
     * @param appInfoDto
     * @return
     */
    List<AgentDTO> getAppAgentInfoList(AppInfoDto appInfoDto);

    /**
     * 获取所有应用的Agent信息
     *
     * @param appInfoDtos
     * @return
     */
    List<AgentDTO> getAgentInfoList(List<AppInfoDto> appInfoDtos);
}
