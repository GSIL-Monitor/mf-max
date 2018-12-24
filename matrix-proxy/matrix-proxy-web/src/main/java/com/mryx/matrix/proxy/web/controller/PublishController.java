package com.mryx.matrix.proxy.web.controller;

import com.mryx.matrix.common.domain.ResultVo;
import com.mryx.matrix.process.domain.Project;
import com.mryx.matrix.proxy.core.service.PublishService;
import com.mryx.matrix.publish.domain.ProjectTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Publish Controller
 *
 * @author supeng
 * @date 2018/10/09
 */
@RestController
@RequestMapping(value = "/api/matrix/publish")
public class PublishController extends BaseController {

    @Autowired
    private PublishService publishService;

    /**
     * 项目发布中断
     *
     * @return
     */
    @PostMapping(value = "interruptProjectPublish", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResultVo interruptProjectPublish(@RequestBody Project project) {
        return publishService.interruptProjectPublish(project);
    }

    /**
     * 应用发布中断
     *
     * @return
     */
    @PostMapping(value = "interruptPublish", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResultVo interruptPublish(@RequestBody ProjectTask projectTask) {
        return publishService.interruptPublish(projectTask);
    }
}