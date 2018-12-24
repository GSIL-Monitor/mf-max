package com.mryx.matrix.publish.web.controller;

import com.mryx.matrix.publish.core.service.ProjectTaskBatchRecordService;
import com.mryx.matrix.publish.domain.ProjectTaskBatchRecord;
import com.mryx.matrix.common.domain.ResultVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 *
 * @author dinglu
 * @date 2018/9/14
 */

@RestController
@RequestMapping("/api/publish/projectTaskBatchRecord")
public class ProjectTaskBatchRecordController {

    private static final Logger logger = LoggerFactory.getLogger(ProjectTaskBatchRecordController.class);

    @Resource
    ProjectTaskBatchRecordService projectTaskBatchRecordService;

    @PostMapping("/getById")
    @ResponseBody
    public ResultVo getById(@RequestParam("id") Integer id){
        try {
            if (id == null || id <= 0){
                logger.info("projectTaskBatchRecord getById fail , param = {}",id);
                return ResultVo.Builder.FAIL().initErrCodeAndMsg("2000","参数错误");
            }
            ProjectTaskBatchRecord projectTaskBatchRecord = projectTaskBatchRecordService.getById(id);
            return ResultVo.Builder.SUCC().initSuccData(projectTaskBatchRecord);
        } catch (Exception e){
            logger.error("projectTaskBatchRecord getById fail , Exception = {}",e);
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("4000","系统异常");
        }
    }

    @PostMapping("/insert")
    @ResponseBody
    public ResultVo insert(@RequestBody ProjectTaskBatchRecord projectTaskBatchRecord){
        try {
            if (projectTaskBatchRecord == null | projectTaskBatchRecord.getProjectTaskId() == null
                    || projectTaskBatchRecord.getProjectTaskId() <=0 || projectTaskBatchRecord.getProjectTaskBatchId() == null
                    || projectTaskBatchRecord.getProjectTaskBatchId() <= 0 || projectTaskBatchRecord.getDeployRecordId() == null
                    || projectTaskBatchRecord.getDeployRecordId() <= 0){
                logger.info("projectTaskBatchRecord insert fail , param = {}",projectTaskBatchRecord);
                return ResultVo.Builder.FAIL().initErrCodeAndMsg("2000","参数错误");
            }
            int result = projectTaskBatchRecordService.insert(projectTaskBatchRecord);
            if (result <= 0){
                logger.info("projectTaskBatchRecord insert fail , param = {}",projectTaskBatchRecord);
                return ResultVo.Builder.FAIL().initErrCodeAndMsg("2001","新增失败");
            }
            return ResultVo.Builder.SUCC();
        } catch (Exception e){
            logger.error("projectTaskBatchRecord insert fail , Exception = {}",e);
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("4000","系统异常");
        }
    }

    @PostMapping("/update")
    @ResponseBody
    public ResultVo update(@RequestBody ProjectTaskBatchRecord projectTaskBatchRecord){
        try {
            if (projectTaskBatchRecord == null || projectTaskBatchRecord.getId() == null
                    || projectTaskBatchRecord.getId() <= 0){
                logger.info("projectTaskBatchRecord update fail , param = {}",projectTaskBatchRecord);
                return ResultVo.Builder.FAIL().initErrCodeAndMsg("2000","参数错误");
            }
            int result = projectTaskBatchRecordService.update(projectTaskBatchRecord);
            if (result <= 0){
                logger.info("projectTaskBatchRecord update fail , param = {}",projectTaskBatchRecord);
                return ResultVo.Builder.FAIL().initErrCodeAndMsg("2001","更新失败");
            }
            return ResultVo.Builder.SUCC().initSuccData("");
        } catch (Exception e){
            logger.error("projectTaskBatchRecord update fail , Exception = {}",e);
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("4000","系统异常");
        }
    }

    @PostMapping("/pageTotal")
    @ResponseBody
    public ResultVo pageTotal(@RequestBody ProjectTaskBatchRecord projectTaskBatchRecord){
        try {
            if (projectTaskBatchRecord == null){
                logger.info("projectTaskBatchRecord pageTotal fail , param = {}",projectTaskBatchRecord);
                return ResultVo.Builder.FAIL().initErrCodeAndMsg("2000","参数错误");
            }
            int count = projectTaskBatchRecordService.pageTotal(projectTaskBatchRecord);
            return ResultVo.Builder.SUCC().initSuccData(count);
        } catch (Exception e){
            logger.error("projectTaskBatchRecord pageTotal fail , Exception = {}",e);
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("4000","系统异常");
        }
    }

    @PostMapping("/list")
    @ResponseBody
    public ResultVo list(@RequestBody ProjectTaskBatchRecord projectTaskBatchRecord){
        try {
            if (projectTaskBatchRecord == null){
                logger.info("projectTaskBatchRecord list fail , param = {}",projectTaskBatchRecord);
                return ResultVo.Builder.FAIL().initErrCodeAndMsg("2000","参数错误");
            }
            List<ProjectTaskBatchRecord> projectTaskBatchRecordList = projectTaskBatchRecordService.list(projectTaskBatchRecord);
            return ResultVo.Builder.SUCC().initSuccData(projectTaskBatchRecordList);
        } catch (Exception e){
            logger.error("projectTaskBatchRecord batchInsert fail , Exception = {}",e);
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("4000","系统异常");
        }
    }

    @PostMapping("/listByCondition")
    @ResponseBody
    public ResultVo listByCondition(@RequestBody ProjectTaskBatchRecord projectTaskBatchRecord){
        try {
            if (projectTaskBatchRecord == null){
                logger.info("projectTaskBatchRecord listByCondition fail , param = {}",projectTaskBatchRecord);
                return ResultVo.Builder.FAIL().initErrCodeAndMsg("2000","参数错误");
            }
            List<ProjectTaskBatchRecord> projectTaskBatchRecordList = projectTaskBatchRecordService.listByCondition(projectTaskBatchRecord);
            return ResultVo.Builder.SUCC().initSuccData(projectTaskBatchRecordList);
        } catch (Exception e){
            logger.error("projectTaskBatchRecord listByCondition fail , Exception = {}",e);
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("4000","系统异常");
        }
    }

    @PostMapping("/delete")
    @ResponseBody
    public ResultVo delete(@RequestBody ProjectTaskBatchRecord projectTaskBatchRecord){
        try {
            if(projectTaskBatchRecord == null || projectTaskBatchRecord.getId() == null || projectTaskBatchRecord.getId()<=0){
                logger.info("projectTaskBatchRecord delete fail , param = {}",projectTaskBatchRecord);
                return ResultVo.Builder.FAIL().initErrCodeAndMsg("2000","参数错误");
            }
            int result = projectTaskBatchRecordService.delete(projectTaskBatchRecord);
            if (result <= 0){
                logger.info("projectTaskBatchRecord delete fail , param = {}",projectTaskBatchRecord);
                return ResultVo.Builder.FAIL().initErrCodeAndMsg("2001","删除失败");
            }
            return ResultVo.Builder.SUCC();
        } catch (Exception e){
            logger.error("projectTaskBatchRecord delete fail , Exception = {}",e);
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("4000","系统异常");
        }
    }

    @PostMapping("/batchInsert")
    @ResponseBody
    public ResultVo batchInsert(@RequestBody List<ProjectTaskBatchRecord> projectTaskBatchRecordList){
        try {
            if (projectTaskBatchRecordList.isEmpty() || projectTaskBatchRecordList.size() <= 0){
                logger.info("projectTaskBatchRecord batchInsert fail , param = {}",projectTaskBatchRecordList);
                return ResultVo.Builder.FAIL().initErrCodeAndMsg("2000","参数错误");
            }
            int result = projectTaskBatchRecordService.batchInsert(projectTaskBatchRecordList);
            if (result <= 0){
                logger.info("projectTaskBatchRecord batchInsert fail , param = {}",projectTaskBatchRecordList);
                return ResultVo.Builder.FAIL().initErrCodeAndMsg("2001","新增失败");
            }
            return ResultVo.Builder.SUCC().initSuccData("");
        } catch (Exception e){
            logger.error("projectTaskBatchRecord batchInsert fail , Exception = {}",e);
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("4000","系统异常");
        }
    }

    @PostMapping("/batchDelete")
    @ResponseBody
    public ResultVo batchDelete(@RequestBody List<ProjectTaskBatchRecord> projectTaskBatchRecordList){
        try {
            if (projectTaskBatchRecordList.isEmpty() || projectTaskBatchRecordList.size() <= 0){
                logger.info("projectTaskBatchRecord batchDelete fail , param = {}",projectTaskBatchRecordList);
                return ResultVo.Builder.FAIL().initErrCodeAndMsg("2000","参数错误");
            }
            int result = projectTaskBatchRecordService.batchDelete(projectTaskBatchRecordList);
            if (result <= 0){
                logger.info("projectTaskBatchRecord batchDelete fail , param = {}",projectTaskBatchRecordList);
                return ResultVo.Builder.FAIL().initErrCodeAndMsg("2001","删除失败");
            }
            return ResultVo.Builder.SUCC();
        } catch (Exception e){
            logger.error("projectTaskBatchRecord batchDelete fail , Exception = {}",e);
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("4000","系统异常");
        }
    }
}
