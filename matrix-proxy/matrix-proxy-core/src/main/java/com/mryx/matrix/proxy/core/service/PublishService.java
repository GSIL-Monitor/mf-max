package com.mryx.matrix.proxy.core.service;

import com.mryx.matrix.common.domain.ResultVo;
import com.mryx.matrix.process.domain.Project;
import com.mryx.matrix.publish.domain.ProjectTask;

/**
 * Publish Service
 *
 * @author supeng
 * @date 2018/10/09
 */
public interface PublishService {
    /**
     * 项目发布中断
     * @param project
     * @return
     */
    ResultVo interruptProjectPublish(Project project);

    /**
     * 应用发布中断
     * @param projectTask
     * @return
     */
    ResultVo interruptPublish(ProjectTask projectTask);
}
