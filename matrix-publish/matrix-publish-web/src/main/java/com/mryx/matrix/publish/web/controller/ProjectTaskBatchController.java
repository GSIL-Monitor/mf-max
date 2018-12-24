package com.mryx.matrix.publish.web.controller;

import com.mryx.matrix.publish.core.service.ProjectTaskBatchService;
import com.mryx.matrix.publish.domain.ProjectTaskBatch;
import com.mryx.matrix.common.domain.ResultVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 *
 * @author dinglu
 * @date 2018/9/10
 */
@RestController
@RequestMapping("/api/publish/projectTaskBatch")
public class ProjectTaskBatchController {

    private static final Logger logger = LoggerFactory.getLogger(ProjectTaskBatchController.class);

    @Resource
    ProjectTaskBatchService projectTaskBatchService;

    @PostMapping("/getById")
    @ResponseBody
    public ResultVo getById(@RequestParam("id") Integer id){
        try {
            if (id ==null || id <= 0){
                logger.info("projectTaskBatch getById fail , param = {}",id);
                return ResultVo.Builder.FAIL().initErrCodeAndMsg("2000","参数错误");
            }
            ProjectTaskBatch projectTaskBatch = projectTaskBatchService.getById(id);
            return ResultVo.Builder.SUCC().initSuccData(projectTaskBatch);
        } catch (Exception e) {
            logger.error("projectTaskBatch getById exception , param = {}",e);
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("4000","系统异常");
        }
    }

    @PostMapping("/insert")
    @ResponseBody
    public ResultVo insert(@RequestBody ProjectTaskBatch projectTaskBatch){
        try {
            if (projectTaskBatch == null || projectTaskBatch.getProjectTaskId() ==null
                    || projectTaskBatch.getProjectTaskId() <= 0 ){
                logger.info("projectTaskBatch insert fail , param = {}",projectTaskBatch);
                return ResultVo.Builder.FAIL().initErrCodeAndMsg("2000","参数错误");
            }
            int result = projectTaskBatchService.insert(projectTaskBatch);
            if (result <= 0){
                logger.info("projectTaskBatch insert fail , param = {}",projectTaskBatch);
                return ResultVo.Builder.FAIL().initErrCodeAndMsg("2001","新增失败");
            }
            return ResultVo.Builder.SUCC();
        } catch (Exception e) {
            logger.error("projectTaskBatch insert exception , param = {}",e);
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("4000","系统异常");
        }
    }

    @PostMapping("/update")
    @ResponseBody
    public ResultVo update(@RequestBody ProjectTaskBatch projectTaskBatch){
        try {
            if (projectTaskBatch == null || projectTaskBatch.getId() == null
                    || projectTaskBatch.getId() <= 0){
                logger.info("projectTaskBatch update fail , param = {}",projectTaskBatch);
                return ResultVo.Builder.FAIL().initErrCodeAndMsg("2000","参数错误");
            }
            int result = projectTaskBatchService.updateById(projectTaskBatch);
            if (result <= 0){
                logger.info("projectTaskBatch update fail , param = {}",projectTaskBatch);
                return ResultVo.Builder.FAIL().initErrCodeAndMsg("2001","更新失败");
            }
            return ResultVo.Builder.SUCC();
        } catch (Exception e){
            logger.error("projectTaskBatch update exception , param = {}",e);
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("4000","系统异常");
        }
    }

    @PostMapping("/pageTotal")
    @ResponseBody
    public ResultVo pageTotal(@RequestBody ProjectTaskBatch projectTaskBatch){
        try {
            if (projectTaskBatch == null){
                logger.info("projectTaskBatch pageTotal fail , param = {}",projectTaskBatch);
                return ResultVo.Builder.FAIL().initErrCodeAndMsg("2000","参数错误");
            }
            int count =projectTaskBatchService.pageTotal(projectTaskBatch);
            return ResultVo.Builder.SUCC().initSuccData(count);
        } catch (Exception e){
            logger.error("projectTaskBatch pageTotal exception , param = {}",e);
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("4000","系统异常");
        }
    }

    @PostMapping("/list")
    @ResponseBody
    public ResultVo list(@RequestBody ProjectTaskBatch projectTaskBatch){
        try {
            if (projectTaskBatch == null){
                logger.info("projectTaskBatch list fail , param = {}",projectTaskBatch);
                return ResultVo.Builder.FAIL().initErrCodeAndMsg("2000","参数错误");
            }
            List<ProjectTaskBatch> projectTaskBatchList = projectTaskBatchService.listPage(projectTaskBatch);
            return ResultVo.Builder.SUCC().initSuccData(projectTaskBatchList);
        } catch (Exception e){
            logger.error("projectTaskBatch list exception , param = {}",e);
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("4000","系统异常");
        }
    }

    @PostMapping("/listByCondition")
    @ResponseBody
    public ResultVo listByCondition(ProjectTaskBatch projectTaskBatch){
        try {
            if (projectTaskBatch == null){
                logger.info("projectTaskBatch listByCondition fail , param = {}",projectTaskBatch);
                return ResultVo.Builder.FAIL().initErrCodeAndMsg("2000","参数错误");
            }
            List<ProjectTaskBatch> projectTaskBatchList = projectTaskBatchService.listByCondition(projectTaskBatch);
            return ResultVo.Builder.SUCC().initSuccData(projectTaskBatchList);
        } catch (Exception e){
            logger.error("projectTaskBatch listByCondition exception , param = {}",e);
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("4000","系统异常");
        }
    }

    @PostMapping("/batchInsert")
    @ResponseBody
    public ResultVo batchInsert(@RequestBody List<ProjectTaskBatch> projectTaskBatchList){
        try {
            if (projectTaskBatchList.isEmpty() || projectTaskBatchList.size() <= 0) {
                logger.info("projectTaskBatch batchInsert fail , param = {}",projectTaskBatchList);
                return ResultVo.Builder.FAIL().initErrCodeAndMsg("2000","参数错误");
            }
            projectTaskBatchService.batchInsert(projectTaskBatchList);
            return ResultVo.Builder.SUCC().initSuccData(projectTaskBatchList);
        } catch (Exception e){
            logger.error("projectTaskBatch batchInsert exception , param = {}",e);
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("4000","系统异常");
        }
    }

    @PostMapping("/save")
    @ResponseBody
    public ResultVo save(@RequestBody List<ProjectTaskBatch> projectTaskBatchList){
        try {
            if (projectTaskBatchList.isEmpty() || projectTaskBatchList.size() <= 0) {
                logger.info("projectTaskBatch save fail , param = {}",projectTaskBatchList);
                return ResultVo.Builder.FAIL().initErrCodeAndMsg("2000","参数错误");
            }
            int result = projectTaskBatchService.save(projectTaskBatchList);
            if (result <= 0) {
                logger.info("projectTaskBatch save fail , param = {}",projectTaskBatchList);
                return ResultVo.Builder.FAIL().initErrCodeAndMsg("2001","保存失败");
            }
            return ResultVo.Builder.SUCC();
        } catch (Exception e) {
            logger.error("projectTaskBatch save exception , param = {}",e);
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("4000","系统异常");
        }
    }
}

