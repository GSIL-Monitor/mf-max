package com.mryx.matrix.proxy.core.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.mryx.matrix.common.domain.ResultVo;
import com.mryx.matrix.proxy.core.service.AgentService;
import com.mryx.matrix.publish.dto.AgentDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * AgentService Impl
 *
 * @author supeng
 * @date 2018/10/10
 */
@Slf4j
//@Service(value = "agentService")
public class AgentServiceImpl implements AgentService {
    @Override
    public ResultVo getAgentList(AgentDTO agentDTO) {
        log.info("agent list agentDTO = {}", agentDTO);
        JSONObject result = new JSONObject();
        Integer pageNo = agentDTO.getPageNo();
        Integer pageSize = agentDTO.getPageSize();
        String appCode = agentDTO.getAppCode();
        //TODO 分页
//        int total = projectService.*;
//        int totalPage = (total % pageSize == 0) ? (total / pageSize) : (total / pageSize + 1);
//        try {
//            List<AgentDTO> list = agentService.getAppAgentInfoList(agentDTO);
//        } catch (Exception e) {
//            LOGGER.info("getAgentInfoList error ", e);
//        }
        return ResultVo.Builder.SUCC().initSuccData(result);
    }
}
