package com.mryx.matrix.proxy.web.controller;

import com.mryx.matrix.common.domain.ResultVo;
import com.mryx.matrix.proxy.core.service.AgentService;
import com.mryx.matrix.publish.dto.AgentDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * Agent Controller
 *
 * @author supeng
 * @date 2018/09/17
 */
@Slf4j
@RestController
@RequestMapping("/api/proxy/agent")
public class AgentController extends BaseController{

//    @Resource
    private AgentService agentService;

    @PostMapping(value = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResultVo list(@RequestBody AgentDTO agentDTO) {
        return agentService.getAgentList(agentDTO);
    }
}
