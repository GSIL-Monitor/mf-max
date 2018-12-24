package com.mryx.matrix.publish.web.controller;

import com.mryx.matrix.common.domain.ResultVo;
import com.mryx.matrix.common.dto.AppInfoDto;
import com.mryx.matrix.publish.core.service.BetaDelpoyRecordService;
import com.mryx.matrix.publish.domain.BetaDelpoyRecord;
import com.mryx.matrix.publish.enums.PublishStatusEnum;
import com.mryx.matrix.publish.web.vo.BetaDeployRecordRequest;
import com.mryx.matrix.publish.web.vo.BetaDeployRecordVo;
import com.mryx.matrix.publish.web.vo.Pagination;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author dinglu
 * @date 2018/9/11
 */

@RestController
@RequestMapping("/api/publish/betaDeployRecord")
public class BetaDeployRecordController {

    private static final Logger logger = LoggerFactory.getLogger(BetaDeployRecordController.class);

    @Resource
    BetaDelpoyRecordService betaDelpoyRecordService;

    @Autowired
    AppCenterUtil appCenterUtil;

    @PostMapping("/pageTotal")
    @ResponseBody
    public ResultVo pageTotal(@RequestBody BetaDelpoyRecord betaDelpoyRecord) {
        ResultVo resultVo = new ResultVo();
        try {
            logger.info("beta deploy record ------ pageTotal , param = {}", betaDelpoyRecord);
            int count = betaDelpoyRecordService.pageTotal(betaDelpoyRecord);
            return ResultVo.Builder.SUCC().initSuccData(count);
        } catch (Exception e) {
            logger.error("beta deploy record ------ pageTotal , Exception = {}", e);
            return resultVo.initErrCodeAndMsg("4000", "系统异常");
        }
    }

    @PostMapping("/list")
    @ResponseBody
    public ResultVo list(@RequestBody BetaDeployRecordRequest betaDeployRecordRequest) {
        try {
            if (betaDeployRecordRequest == null) {
                logger.info("beta deploy record ------ list fail , param = {}", betaDeployRecordRequest.toString());
                return ResultVo.Builder.FAIL().initErrCodeAndMsg("2000", "参数错误");
            }

            Pagination<BetaDeployRecordVo> pagination = new Pagination<>();
            pagination.setPageSize(betaDeployRecordRequest.getPageSize());
            BetaDelpoyRecord betaDelpoyRecord = new BetaDelpoyRecord();
            betaDelpoyRecord.setDelFlag(1);
            BeanUtils.copyProperties(betaDeployRecordRequest, betaDelpoyRecord);
            if (!StringUtils.isEmpty(betaDeployRecordRequest.getProjectId()) && !betaDeployRecordRequest.getProjectId().matches("^[0-9]*$")) {
                pagination.setTotalPageForTotalSize(0);
                pagination.setDataList(new ArrayList<>());
                return ResultVo.Builder.SUCC().initSuccData(pagination);
            } else if (!StringUtils.isEmpty(betaDeployRecordRequest.getProjectId()) && betaDeployRecordRequest.getProjectId().matches("^[0-9]*$")) {
                betaDelpoyRecord.setProjectId(Integer.parseInt(betaDeployRecordRequest.getProjectId()));
            }
            Integer total = betaDelpoyRecordService.pageTotal(betaDelpoyRecord);
            pagination.setTotalPageForTotalSize(total);
            if (total == 0) {
                pagination.setDataList(new ArrayList<>());
                return ResultVo.Builder.SUCC().initSuccData(pagination);
            }
            List<BetaDelpoyRecord> betaDelpoyRecordList = betaDelpoyRecordService.listPage(betaDelpoyRecord);
            Map<String, AppInfoDto> map = new HashMap();

            List<BetaDeployRecordVo> betaDeployRecordVoList = betaDelpoyRecordList.stream().map(betaDelpoyRecord1 -> {
                BetaDeployRecordVo betaDeployRecordVo = new BetaDeployRecordVo();
                BeanUtils.copyProperties(betaDelpoyRecord1, betaDeployRecordVo);
                if (betaDelpoyRecord1.getDeployStatus().equals(PublishStatusEnum.BETA_FAIL.getCode())
                        && betaDelpoyRecord1.getSubDeployStatus() != null) {
                    if (betaDelpoyRecord1.getSubDeployStatus().equals(PublishStatusEnum.CODE_PUSH_FAIL.getCode()) || betaDelpoyRecord1.getSubDeployStatus().equals(PublishStatusEnum.BUILD_FAIL.getCode())) {
                        betaDeployRecordVo.setDeployStatusDesc(PublishStatusEnum.getValueByCode(betaDelpoyRecord1.getDeployStatus()) + "-" + PublishStatusEnum.getValueByCode(betaDelpoyRecord1.getSubDeployStatus()));
                    } else {
                        betaDeployRecordVo.setDeployStatusDesc(PublishStatusEnum.getValueByCode(betaDelpoyRecord1.getDeployStatus()));
                    }
                } else {
                    betaDeployRecordVo.setDeployStatusDesc(PublishStatusEnum.getValueByCode(betaDelpoyRecord1.getDeployStatus()));
                }
                if (betaDelpoyRecord1.getIsDeploy() == 0) {
                    betaDeployRecordVo.setIsDeploy(true);
                } else {
                    betaDeployRecordVo.setIsDeploy(false);
                }
                String profile = betaDelpoyRecord1.getProfile();
                if ("autotest".equals(profile)) {
                    profile = "prod";
                } else if (!"dev".equals(profile)) {
                    profile = "beta";
                }
                String appCode = betaDelpoyRecord1.getAppCode();
                String key = appCode + profile;
                AppInfoDto appInfoDto = map.get(key);
                if (appInfoDto == null) {
                    try {
                        appInfoDto = appCenterUtil.getAppInfo(betaDelpoyRecord1.getAppCode(), profile);
                        map.put(key, appInfoDto);
                    } catch (Exception e) {
                        logger.error("查询应用信息错误: {}", e.getMessage());
                    }
                }
                betaDeployRecordVo.setAppInfoDto(appInfoDto);
                return betaDeployRecordVo;
            }).collect(Collectors.toList());
            pagination.setDataList(betaDeployRecordVoList);
            return ResultVo.Builder.SUCC().initSuccData(pagination);
        } catch (Exception e) {
            logger.error("beta deploy record ------ list , Exception = {}", e);
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("4000", "系统异常");
        }
    }

    @PostMapping("/getByCondition")
    @ResponseBody
    public ResultVo getByCondition(@RequestBody BetaDelpoyRecord betaDelpoyRecord) {
        try {
            logger.info("beta deploy record ------ getByCondition , param = {}", betaDelpoyRecord);
            if (betaDelpoyRecord == null) {
                logger.info("beta deploy record ------ getByCondition 参数错误");
                return ResultVo.Builder.FAIL().initErrCodeAndMsg("2000", "参数错误");
            }
            List<BetaDelpoyRecord> betaDelpoyRecordList = betaDelpoyRecordService.listByCondition(betaDelpoyRecord);
            return ResultVo.Builder.SUCC().initSuccData(betaDelpoyRecordList);
        } catch (Exception e) {
            logger.error("beta deploy record ------ getByCondition , Exception = {}", e);
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("4000", "系统异常");
        }
    }

    @PostMapping("/insert")
    @ResponseBody
    public ResultVo insert(@RequestBody BetaDelpoyRecord betaDelpoyRecord) {
        try {
            logger.info("beta deploy record ------ insert , param = {}", betaDelpoyRecord);
            if (betaDelpoyRecord == null) {
                logger.info("beta deploy record ------ insert 参数错误, param = {}", betaDelpoyRecord);
                return ResultVo.Builder.FAIL().initErrCodeAndMsg("2000", "参数错误");
            }
            int result = betaDelpoyRecordService.insert(betaDelpoyRecord);
            if (result <= 0) {
                logger.info("beta deploy record ------ insert fail ,param = {}", betaDelpoyRecord);
                return ResultVo.Builder.FAIL().initErrCodeAndMsg("2001", "插入失败");
            } else {
                return ResultVo.Builder.SUCC();
            }
        } catch (Exception e) {
            logger.error("beta deploy record ------ insert , Exception", e);
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("4000", "系统异常");
        }
    }

    @PostMapping("/update")
    @ResponseBody
    public ResultVo update(@RequestBody BetaDelpoyRecord betaDelpoyRecord) {
        try {
            logger.info("beta deploy record ------ update , param = {}", betaDelpoyRecord);
            if (betaDelpoyRecord == null || betaDelpoyRecord.getId() == null || betaDelpoyRecord.getId() <= 0) {
                logger.info("beta deploy record ------ update 参数错误 , param = {}", betaDelpoyRecord);
                return ResultVo.Builder.FAIL().initErrCodeAndMsg("2000", "参数错误");
            }
            int result = betaDelpoyRecordService.updateById(betaDelpoyRecord);
            if (result <= 0) {
                logger.info("beta deploy record ------ update fail ,param = {}", betaDelpoyRecord);
                return ResultVo.Builder.FAIL().initErrCodeAndMsg("2001", "更新失败");
            } else {
                return ResultVo.Builder.SUCC();
            }
        } catch (Exception e) {
            logger.error("beta deploy record ------ update , Exception", e);
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("4000", "系统异常");
        }
    }

    @PostMapping("/getById")
    @ResponseBody
    public ResultVo getById(@RequestParam("id") Integer i) {
        ResultVo resultVo = new ResultVo();
        try {
            logger.info("beta deploy record ------ pageTotal , param = {}", i);
            BetaDelpoyRecord betaDelpoyRecord = betaDelpoyRecordService.getById(i);
            return ResultVo.Builder.SUCC().initSuccData(betaDelpoyRecord);
        } catch (Exception e) {
            logger.error("beta deploy record ------ pageTotal , Exception = {}", e);
            return resultVo.initErrCodeAndMsg("4000", "系统异常");
        }
    }

    @PostMapping("/batchInsert")
    @ResponseBody
    public ResultVo batchInsert(@RequestBody List<BetaDelpoyRecord> betaDelpoyRecordList) {
        try {
            if (betaDelpoyRecordList.isEmpty() || betaDelpoyRecordList.size() <= 0) {
                logger.info("beta deploy record ------ batchInsert fail ,param = {}", betaDelpoyRecordList);
                return ResultVo.Builder.FAIL().initErrCodeAndMsg("2000", "参数错误");
            }
            betaDelpoyRecordService.batchInsert(betaDelpoyRecordList);
            return ResultVo.Builder.SUCC().initSuccData(betaDelpoyRecordList);
        } catch (Exception e) {
            logger.error("beta deploy record ------ batchInsert , Exception = {}", e);
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("4000", "系统异常");
        }
    }

}
