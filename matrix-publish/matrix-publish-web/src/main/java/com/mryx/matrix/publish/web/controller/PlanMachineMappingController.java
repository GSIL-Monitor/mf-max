package com.mryx.matrix.publish.web.controller;

import com.mryx.matrix.common.domain.ResultVo;
import com.mryx.matrix.publish.core.service.PlanMachineMappingService;
import com.mryx.matrix.publish.domain.PlanMachineMapping;
import com.mryx.matrix.publish.enums.PublishStatusEnum;
import com.mryx.matrix.publish.web.vo.Pagination;
import com.mryx.matrix.publish.web.vo.PlanMachineMappingVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 发布计划关联机器controller
 * @author dinglu
 * @date 2018/10/19
 */

@RestController
@RequestMapping("/api/publish/planMachineMapping")
public class PlanMachineMappingController {

    private static final Logger logger = LoggerFactory.getLogger(PlanMachineMappingController.class);

    @Resource
    PlanMachineMappingService planMachineMappingService;

    @PostMapping("/getById")
    @ResponseBody
    public ResultVo getById(@RequestParam("id") Integer id){
        try {
            logger.info("get planMachineMapping by id , param = {}",id);
            if (id == null || id <= 0){
                logger.info("get planMachineMapping by id fail , 参数错误 , param = {}",id);
                return ResultVo.Builder.FAIL().initErrCodeAndMsg("2000","参数错误");
            }
            PlanMachineMapping planMachineMapping = planMachineMappingService.getById(id);
            if (planMachineMapping == null){
                logger.info("get planMachineMapping by id no data , param = {}",id);
                return ResultVo.Builder.SUCC().initSuccData(new PlanMachineMappingVo());
            }
            PlanMachineMappingVo planMachineMappingVo = covertTo(planMachineMapping);
            return ResultVo.Builder.SUCC().initSuccData(planMachineMappingVo);
        } catch (Exception e) {
            logger.error("get planMachineMapping by id exception , Exception = {}",e);
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("4000","系统异常");
        }
    }

    @PostMapping("/getByPlanId")
    @ResponseBody
    public ResultVo getByPlanId(@RequestParam("planId") Integer planId ){
        try {
            logger.info("get planMachineMapping by plan ，param = {}",planId);
            if (planId == null || planId <= 0){
                logger.info("get planMachineMapping by plan fail , 参数错误 ，param = {}",planId);
                return ResultVo.Builder.FAIL().initErrCodeAndMsg("2000","参数错误");
            }
            List<PlanMachineMapping> planMachineMappingList = planMachineMappingService.listPublishByPlanId(planId);
            if (planMachineMappingList == null || planMachineMappingList.size() <= 0){
                logger.info("get planMachineMapping by plan no data , param = {}",planId);
                return ResultVo.Builder.SUCC().initSuccData(new ArrayList<>());
            }
            List<PlanMachineMappingVo> planMachineMappingVoList = planMachineMappingList.stream().map(planMachineMapping -> {
                PlanMachineMappingVo planMachineMappingVo = covertTo(planMachineMapping);
                return planMachineMappingVo;
            }).collect(Collectors.toList());
            return ResultVo.Builder.SUCC().initSuccData(planMachineMappingVoList);
        } catch (Exception e) {
            logger.error("get planMachineMapping by plan exception , Exception = {}",e);
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("4000","系统异常");
        }
    }

    @PostMapping("/listByCondition")
    @ResponseBody
    public ResultVo listByCondition(@RequestBody PlanMachineMapping planMachineMapping){
        try {
            logger.info("list planMachineMapping by conditions , param = {}",planMachineMapping.toString());
            if (planMachineMapping == null) {
                logger.info("list planMachineMapping by list fail , 参数错误 ，param = {}",planMachineMapping.toString());
                return ResultVo.Builder.FAIL().initErrCodeAndMsg("2000","参数错误");
            }
            planMachineMapping.setDelFlag(1);
            List<PlanMachineMapping> planMachineMappingList = planMachineMappingService.listByCondition(planMachineMapping);
            if (planMachineMappingList == null || planMachineMappingList.size() <= 0){
                logger.info("list planMachineMapping by list no data , param = {}",planMachineMapping.toString());
                return ResultVo.Builder.SUCC().initSuccData(new ArrayList<>());
            }
            List<PlanMachineMappingVo> planMachineMappingVoList = planMachineMappingList.stream().map(planMachineMapping1 -> {
                PlanMachineMappingVo planMachineMappingVo = covertTo(planMachineMapping1);
                return planMachineMappingVo;
            }).collect(Collectors.toList());
            return ResultVo.Builder.SUCC().initSuccData(planMachineMappingVoList);
        } catch (Exception e) {
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("4000","系统异常");
        }
    }

    @PostMapping("/list")
    @ResponseBody
    public ResultVo list(@RequestBody PlanMachineMapping planMachineMapping){
        try {
            logger.info("list planMachineMapping by page , param = {}",planMachineMapping.toString());
            if (planMachineMapping == null){
                logger.info("list planMachineMapping by page fail , 参数错误 , param = {}",planMachineMapping.toString());
                return ResultVo.Builder.FAIL().initErrCodeAndMsg("2000","参数错误");
            }
            planMachineMapping.setDelFlag(1);
            int total = planMachineMappingService.pageTotal(planMachineMapping);
            Pagination<PlanMachineMappingVo> pagination = new Pagination<>();
            pagination.setPageSize(planMachineMapping.getPageSize());
            pagination.setTotalPageForTotalSize(total);
            if (total <= 0){
                logger.info("list planMachineMapping by page no data , param = {}",planMachineMapping.toString());
                pagination.setDataList(new ArrayList<>());
                return ResultVo.Builder.SUCC().initSuccData(pagination);
            }
            List<PlanMachineMapping> planMachineMappingList = planMachineMappingService.listByPage(planMachineMapping);
            List<PlanMachineMappingVo> planMachineMappingVoList = planMachineMappingList.stream().map(planMachineMapping1 -> {
                PlanMachineMappingVo planMachineMappingVo = covertTo(planMachineMapping1);
                return planMachineMappingVo;
            }).collect(Collectors.toList());
            pagination.setDataList(planMachineMappingVoList);
            return ResultVo.Builder.SUCC().initSuccData(pagination);
        } catch (Exception e) {
            logger.error("list planMachineMapping by page exception , Exception = {}",e);
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("4000","系统异常");
        }
    }

    @PostMapping("/updateById")
    @ResponseBody
    public ResultVo updateById(@RequestBody PlanMachineMapping planMachineMapping){
        try {
            logger.info("update planMachineMapping by id , param = {}",planMachineMapping.toString());
            if (planMachineMapping == null || planMachineMapping.getId() == null || planMachineMapping.getId() <= 0){
                logger.info("update planMachineMapping by id fail , 参数错误 , param = {}",planMachineMapping.toString());
                return ResultVo.Builder.FAIL().initErrCodeAndMsg("2000","参数错误");
            }
            int result = planMachineMappingService.updateById(planMachineMapping);
            if (result <= 0){
                logger.info("update planMachineMapping by id fail , 更新失败 , param = {}",planMachineMapping.toString());
                return ResultVo.Builder.FAIL().initErrCodeAndMsg("2001","更新失败");
            }
            return ResultVo.Builder.SUCC().initSuccData("更新成功");
        } catch (Exception e) {
            logger.error("update planMachineMapping by id exception , Exception = {}",e);
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("4000","系统异常");
        }
    }

    @PostMapping("/insert")
    @ResponseBody
    public ResultVo insert(@RequestBody PlanMachineMapping planMachineMapping){
        try {
            logger.info("insert planMachineMapping , param = {}",planMachineMapping.toString());
            if (planMachineMapping == null){
                logger.info("insert planMachineMapping fail , 参数错误 , param = {}",planMachineMapping.toString());
                return ResultVo.Builder.FAIL().initErrCodeAndMsg("2000","参数错误");
            }
            int result = planMachineMappingService.insert(planMachineMapping);
            if (result <= 0){
                logger.info("insert planMachineMapping fail , 新增失败 , param = {}",planMachineMapping.toString());
                return ResultVo.Builder.FAIL().initErrCodeAndMsg("2001","新增失败");
            }
            return ResultVo.Builder.SUCC().initSuccData("新增成功");
        } catch (Exception e) {
            logger.error("insert planMachineMapping exception , Exception = {}",e);
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("4000","系统异常");
        }
    }

    private PlanMachineMappingVo covertTo(PlanMachineMapping planMachineMapping){
        PlanMachineMappingVo planMachineMappingVo = new PlanMachineMappingVo();
        BeanUtils.copyProperties(planMachineMapping,planMachineMappingVo);
        planMachineMappingVo.setDeployStatusDesc(PublishStatusEnum.getValueByCode(planMachineMapping.getDeployStatus()));
        return planMachineMappingVo;
    }
}
