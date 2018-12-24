package com.mryx.matrix.publish.web.controller;

import com.mryx.matrix.common.domain.ResultVo;
import com.mryx.matrix.publish.core.service.DeployPlanRecordService;
import com.mryx.matrix.publish.domain.DeployPlanRecord;
import com.mryx.matrix.publish.enums.PublishStatusEnum;
import com.mryx.matrix.publish.web.vo.DeployPlanRecordVo;
import com.mryx.matrix.publish.web.vo.Pagination;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 发布计划发布记录controller
 *
 * @author dinglu
 * @date 2018/10/19
 */

@RestController
@RequestMapping("/api/publish/deployPlanRecord")
public class DeployPlanRecordController {

    private static final Logger logger = LoggerFactory.getLogger(DeployPlanRecordController.class);

    @Resource
    DeployPlanRecordService deployPlanRecordService;

    @PostMapping("/getById")
    @ResponseBody
    public ResultVo getById(@RequestParam("id") Integer id){
        try {
            logger.info("get deployPlanRecord by id , param = {}",id);
            if (id == null || id <= 0){
                logger.info("get deployPlanRecord by id fail , 参数错误 , param = {}",id);
                return ResultVo.Builder.FAIL().initErrCodeAndMsg("2000","参数错误");
            }
            DeployPlanRecord deployPlanRecord = deployPlanRecordService.getById(id);
            if (deployPlanRecord == null){
                logger.info("get deployPlanRecord by id no data , param = {}",id);
                return ResultVo.Builder.SUCC().initSuccData(new DeployPlanRecordVo());
            }
            DeployPlanRecordVo deployPlanRecordVo = convertTo(deployPlanRecord);
            return ResultVo.Builder.SUCC().initSuccData(deployPlanRecordVo);
        } catch (Exception e) {
            logger.error("get deployPlanRecord by id exception , Exception = {}",e);
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("4000","系统异常");
        }
    }

    @PostMapping("/getByPlanId")
    @ResponseBody
    public ResultVo getByPlanId(@RequestParam("planId") Integer planId){
        try {
            logger.info("get deployPlanRecord by deployPlanId ,param = {}",planId);
            if (planId == null || planId <= 0){
                logger.info("get deployPlanRecord by deployPlanId fail , 参数错误 , param = {}",planId);
                return ResultVo.Builder.FAIL().initErrCodeAndMsg("2000","参数错误");
            }
            List<DeployPlanRecord> deployPlanRecordList = deployPlanRecordService.getByPlanId(planId);
            if (deployPlanRecordList == null || deployPlanRecordList.size() <= 0){
                logger.info("get deployPlanRecord by deployPlanId no data , param = {}",planId);
                return ResultVo.Builder.SUCC().initSuccData(new ArrayList<>());
            }
            List<DeployPlanRecordVo> deployPlanRecordVoList = deployPlanRecordList.stream().map(deployPlanRecord -> {
                DeployPlanRecordVo deployPlanRecordVo = convertTo(deployPlanRecord);
                return deployPlanRecordVo;
            }).collect(Collectors.toList());
            return ResultVo.Builder.SUCC().initSuccData(deployPlanRecordVoList);
        } catch (Exception e) {
            logger.error("get deployPlanRecord by deployPlanId exception , Exception = {}",e);
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("4000","系统异常");
        }
    }

    @PostMapping("/listByCondition")
    @ResponseBody
    public ResultVo listByCondition(@RequestBody DeployPlanRecord deployPlanRecord){
        try {
            logger.info("list deployPlanRecord by condition , param = {}",deployPlanRecord.toString());
            if (deployPlanRecord == null){
                logger.info("list deployPlanRecord by condition fail , 参数错误 , param = {}",deployPlanRecord.toString());
                return ResultVo.Builder.FAIL().initErrCodeAndMsg("2000","参数错误");
            }
            List<DeployPlanRecord> deployPlanRecordList = deployPlanRecordService.listByCondition(deployPlanRecord);
            if (deployPlanRecordList == null || deployPlanRecordList.size() <= 0){
                logger.info("list deployPlanRecord by condition no data , param = {}",deployPlanRecord.toString());
                return ResultVo.Builder.SUCC().initSuccData(new ArrayList<>());
            }
            List<DeployPlanRecordVo> deployPlanRecordVoList = deployPlanRecordList.stream().map(deployPlanRecord1 -> {
                DeployPlanRecordVo deployPlanRecordVo = convertTo(deployPlanRecord1);
                return deployPlanRecordVo;
            }).collect(Collectors.toList());
            return ResultVo.Builder.SUCC().initSuccData(deployPlanRecordVoList);
        } catch (Exception e) {
            logger.error("list deployPlanRecord by condition exception , Exception = {}",e);
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("4000","系统异常");
        }
    }

    @PostMapping("/list")
    @ResponseBody
    public ResultVo list(@RequestBody DeployPlanRecord deployPlanRecord){
        try {
            logger.info("list deployPlanRecord by page , param ={}",deployPlanRecord.toString());
            if (deployPlanRecord == null){
                logger.info("list deployPlanRecord by page fail , 参数错误 , param = {}",deployPlanRecord.toString());
                return ResultVo.Builder.FAIL().initErrCodeAndMsg("2000","参数错误");
            }
            Integer total = deployPlanRecordService.pageTotal(deployPlanRecord);
            Pagination<DeployPlanRecordVo> pagination = new Pagination<>();
            pagination.setPageSize(deployPlanRecord.getPageSize());
            pagination.setTotalPageForTotalSize(total);
            if (total <= 0){
                logger.info("list deployPlanRecord by page no data , param = {}",deployPlanRecord.toString());
                pagination.setDataList(new ArrayList<>());
                return ResultVo.Builder.SUCC().initSuccData(pagination);
            }
            List<DeployPlanRecord> deployPlanRecordList = deployPlanRecordService.listByPage(deployPlanRecord);
            List<DeployPlanRecordVo> deployPlanRecordVoList = deployPlanRecordList.stream().map(deployPlanRecord1 -> {
                DeployPlanRecordVo deployPlanRecordVo = convertTo(deployPlanRecord1);
                return deployPlanRecordVo;
            }).collect(Collectors.toList());
            pagination.setDataList(deployPlanRecordVoList);
            return ResultVo.Builder.SUCC().initSuccData(pagination);
        } catch (Exception e) {
            logger.error("list deployPlanRecord by page exception , Exception = {}",e);
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("4000","系统异常");
        }
    }

    @PostMapping("/updateById")
    @ResponseBody
    public ResultVo updateById(@RequestBody DeployPlanRecord deployPlanRecord){
        try {
            logger.info("update deployPlanRecord by id , param = {}",deployPlanRecord.toString());
            if (deployPlanRecord == null || deployPlanRecord.getId() == null || deployPlanRecord.getId() <= 0){
                logger.info("update deployPlanRecord by id fail , 参数错误 , param = {}",deployPlanRecord.toString());
                return ResultVo.Builder.FAIL().initErrCodeAndMsg("2000","参数错误");
            }
            int result = deployPlanRecordService.updateById(deployPlanRecord);
            if (result <= 0){
                logger.info("update deployPlanRecord by id fail , 更新失败 , param = {}",deployPlanRecord.toString());
                return ResultVo.Builder.FAIL().initErrCodeAndMsg("2001","更新失败");
            }
            return ResultVo.Builder.SUCC().initSuccData("更新成功");
        } catch (Exception e) {
            logger.error("update deployPlanRecord by id exception , Exception = {}",e);
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("4000","系统异常");
        }
    }

    @PostMapping("/insert")
    @ResponseBody
    public ResultVo insert(@RequestBody DeployPlanRecord deployPlanRecord){
        try {
            logger.info("insert deployPlanRecord , param = {}",deployPlanRecord.toString());
            if (deployPlanRecord == null){
                logger.info("insert deployPlanRecord fail , 参数错误 , param = {}",deployPlanRecord.toString());
                return ResultVo.Builder.FAIL().initErrCodeAndMsg("2000","参数错误");
            }
            int result = deployPlanRecordService.insert(deployPlanRecord);
            if (result <= 0){
                logger.info("insert deployPlanRecord fail , 新增失败 , param = {}",deployPlanRecord.toString());
                return ResultVo.Builder.FAIL().initErrCodeAndMsg("2001","新增失败");
            }
            return ResultVo.Builder.SUCC().initSuccData("新增成功");
        } catch (Exception e) {
            logger.error("insert deployPlanRecord exception , Exception = {}",e);
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("4000","系统异常");
        }
    }

    private DeployPlanRecordVo convertTo(DeployPlanRecord deployPlanRecord){
        DeployPlanRecordVo deployPlanRecordVo = new DeployPlanRecordVo();
        BeanUtils.copyProperties(deployPlanRecord,deployPlanRecordVo);
        deployPlanRecordVo.setDeployStatusDesc(PublishStatusEnum.getValueByCode(deployPlanRecord.getDeployStatus()));
        return deployPlanRecordVo;
    }
}
