package com.mryx.matrix.proxy.core.service.impl;

import com.mryx.matrix.common.domain.ResultVo;
import com.mryx.matrix.process.domain.Project;
import com.mryx.matrix.proxy.core.service.PublishService;
import com.mryx.matrix.publish.domain.ProjectTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


/**
 * PublishService Impl
 *
 * @author supeng
 * @date 2018/10/09
 */
@Slf4j
@Service(value = "publishService")
public class PublishServiceImpl implements PublishService {

    //TODO
    @Override
    public ResultVo interruptProjectPublish(Project project) {
        log.info("interruptProjectPublish project = {}", project);
        if (project == null || project.getProjectTasks() == null || project.getProjectTasks().isEmpty()) {
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("1", "参数错误！");
        }
//        boolean flag= publishServiceRpc.updateTaskReleasePublishStatus(project.getProjectTasks());

        return null;
    }
    //TODO
    @Override
    public ResultVo interruptPublish(ProjectTask projectTask) {
        log.info("interruptPublish projectTask = {}", projectTask);
        if (projectTask == null || projectTask.getId() == null || projectTask.getId().equals(0)) {
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("1", "参数错误！");
        }
        return null;
    }
}
