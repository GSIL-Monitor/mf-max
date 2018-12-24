package com.mryx.matrix.proxy.core.service;

import com.mryx.matrix.common.domain.ResultVo;
import com.mryx.matrix.publish.dto.AgentDTO;

/**
 * Agent Service interface
 *
 * @author supeng
 * @date 2018/10/10
 */
public interface AgentService {
    ResultVo getAgentList(AgentDTO agentDTO);
}
