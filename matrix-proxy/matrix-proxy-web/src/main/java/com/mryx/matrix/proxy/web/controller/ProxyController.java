package com.mryx.matrix.proxy.web.controller;

import com.mryx.matrix.publish.AgentService;
import com.mryx.matrix.publish.PublishService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 项目Controller
 *
 * @author supeng
 * @date 2018/08/30
 */
@RestController
@RequestMapping("/api/matrix")
public class ProxyController {

    @Resource
    private AgentService agentService;

    @Resource
    private PublishService publishServiceApi;

    @GetMapping(path = "/healthcheck")
    public Object healthcheck() {
        return "success";
    }

    @PostMapping(value = "/a", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public String test(){
        return agentService.test();
    }

    @PostMapping(value = "/p", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public String test2(){
        return publishServiceApi.test();
    }
    
    
}
