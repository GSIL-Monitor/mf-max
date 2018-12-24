package com.mryx.matrix.project.provider;

import com.mryx.matrix.common.domain.ResultVo;
import com.mryx.matrix.project.api.ProjectService;
import com.mryx.matrix.project.dao.ProjectDao;
import com.mryx.matrix.project.domain.Issue;
import com.mryx.matrix.project.domain.Project;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * ProjectService Impl
 *
 * @author supeng
 * @date 2018/11/11
 */
@Slf4j
@Service
public class ProjectServiceImpl implements ProjectService {

    @Resource
    private ProjectDao projectDao;

    @Override
    public ResultVo createProject(Project project) {
        try {
            Integer flag = projectDao.createProject(project);
            if (flag > 0) {
                return ResultVo.Builder.SUCC();
            }
        } catch (Exception e) {
            log.error("createProject error ", e);
        }
        return ResultVo.Builder.FAIL().initErrCodeAndMsg("1", "创建失败");
    }

    @Override
    public ResultVo updateProject(Project project) {
        try {
            Integer flag = projectDao.updateProject(project);
            if (flag > 0) {
                return ResultVo.Builder.SUCC();
            }
        } catch (Exception e) {
            log.error("updateProject error ", e);
        }
        return ResultVo.Builder.FAIL().initErrCodeAndMsg("1", "更新失败");
    }

    @Override
    public ResultVo listProject(Project project) {
        try {
            List<Project> list = projectDao.listProject(project);
            if (list != null && list.size() > 0) {
                return ResultVo.Builder.SUCC().initSuccData(list);
            }
        } catch (Exception e) {
            log.error("listProject error ", e);
        }
        return ResultVo.Builder.SUCC().initSuccData(new ArrayList<>());
    }
}
