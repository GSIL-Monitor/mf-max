package com.mryx.matrix.project.controller;

import com.mryx.matrix.common.domain.ResultVo;
import com.mryx.matrix.project.api.ProjectService;
import com.mryx.matrix.project.domain.Project;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Project Controller
 *
 * @author supeng
 * @date 2018/11/08
 */
@Slf4j
@RestController
@RequestMapping(value = "/api/jira/project")
public class ProjectController extends BaseController {

    @Autowired
    private ProjectService projectService;

    @PostMapping(value = "/createProject", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResultVo createProject(@RequestBody Project project) {
        return projectService.createProject(project);
    }

    @PostMapping(value = "/updateProject", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResultVo updateProject(@RequestBody Project project) {
        return projectService.updateProject(project);
    }

    @PostMapping(value = "/listProject", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResultVo listProject(@RequestBody Project project) {
        return projectService.listProject(project);
    }

}
