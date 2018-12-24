package com.mryx.matrix.publish.web.controller;

import com.mryx.matrix.common.domain.ResultVo;
import com.mryx.matrix.common.dto.AppInfoDto;
import com.mryx.matrix.publish.core.service.ReleaseDelpoyRecordService;
import com.mryx.matrix.publish.domain.ReleaseDelpoyRecord;
import com.mryx.matrix.publish.enums.PublishStatusEnum;
import com.mryx.matrix.publish.web.vo.Pagination;
import com.mryx.matrix.publish.web.vo.ReleaseDeployRecordRequest;
import com.mryx.matrix.publish.web.vo.ReleaseDeployRecordVo;
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
 * @date 2018/9/14
 */
@RestController
@RequestMapping("/api/publish/releaseDeployRecord")
public class ReleaseDeployRecordController {

    private static final Logger logger = LoggerFactory.getLogger(ReleaseDeployRecordController.class);

    @Resource
    ReleaseDelpoyRecordService releaseDelpoyRecordService;

    @Autowired
    AppCenterUtil appCenterUtil;

    @PostMapping("/getById")
    @ResponseBody
    public ResultVo getById(@RequestParam("id") Integer id) {
        try {
            if (id == null || id <= 0) {
                logger.info("releaseDeployRecord getById fail , param = {}", id);
                return ResultVo.Builder.FAIL().initErrCodeAndMsg("2000", "参数错误");
            }
            ReleaseDelpoyRecord releaseDelpoyRecord = releaseDelpoyRecordService.getById(id);
            return ResultVo.Builder.SUCC().initSuccData(releaseDelpoyRecord);
        } catch (Exception e) {
            logger.error("releaseDeployRecord getById Exception , e = {}", e);
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("4000", "系统异常");
        }
    }

    @PostMapping("/insert")
    @ResponseBody
    public ResultVo insert(@RequestBody ReleaseDelpoyRecord releaseDelpoyRecord) {
        try {
            if (releaseDelpoyRecord == null || releaseDelpoyRecord.getProjectTaskId() == null
                    || releaseDelpoyRecord.getProjectTaskId() <= 0) {
                logger.info("releaseDeployRecord insert fail , param = {}", releaseDelpoyRecord);
                return ResultVo.Builder.FAIL().initErrCodeAndMsg("2000", "参数错误");
            }
            int result = releaseDelpoyRecordService.insert(releaseDelpoyRecord);
            if (result <= 0) {
                return ResultVo.Builder.FAIL().initErrCodeAndMsg("2001", "新增失败");
            }
            return ResultVo.Builder.SUCC();
        } catch (Exception e) {
            logger.error("releaseDeployRecord insert Exception , e = {}", e);
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("4000", "系统异常");
        }
    }

    @PostMapping("/update")
    @ResponseBody
    public ResultVo update(@RequestBody ReleaseDelpoyRecord releaseDelpoyRecord) {
        try {
            if (releaseDelpoyRecord == null || releaseDelpoyRecord.getId() == null
                    || releaseDelpoyRecord.getId() <= 0) {
                logger.info("releaseDeployRecord update fail , param = {}", releaseDelpoyRecord);
                return ResultVo.Builder.FAIL().initErrCodeAndMsg("2000", "参数错误");
            }
            int result = releaseDelpoyRecordService.updateById(releaseDelpoyRecord);
            if (result <= 0) {
                return ResultVo.Builder.FAIL().initErrCodeAndMsg("2001", "更新失败");
            }
            return ResultVo.Builder.SUCC();
        } catch (Exception e) {
            logger.error("releaseDeployRecord update Exception , e = {}", e);
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("4000", "系统异常");
        }
    }

    @PostMapping("/pageTotal")
    @ResponseBody
    public ResultVo pageTotal(@RequestBody ReleaseDelpoyRecord releaseDelpoyRecord) {
        try {
            if (releaseDelpoyRecord == null) {
                logger.info("releaseDeployRecord pageTotal fail , param = {}", releaseDelpoyRecord);
                return ResultVo.Builder.FAIL().initErrCodeAndMsg("2000", "参数错误");
            }
            int count = releaseDelpoyRecordService.pageTotal(releaseDelpoyRecord);
            return ResultVo.Builder.SUCC().initSuccData(count);
        } catch (Exception e) {
            logger.error("releaseDeployRecord pageTotal Exception , e = {}", e);
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("4000", "系统异常");
        }
    }

    @PostMapping("/list")
    @ResponseBody
    public ResultVo list(@RequestBody ReleaseDeployRecordRequest releaseDeployRecordRequest) {
        try {
            if (releaseDeployRecordRequest == null) {
                logger.info("releaseDeployRecord list fail , param = {}", releaseDeployRecordRequest.toString());
                return ResultVo.Builder.FAIL().initErrCodeAndMsg("2000", "参数错误");
            }
            Pagination<ReleaseDeployRecordVo> pagination = new Pagination<>();
            pagination.setPageSize(releaseDeployRecordRequest.getPageSize());
            ReleaseDelpoyRecord releaseDelpoyRecord = new ReleaseDelpoyRecord();
            releaseDelpoyRecord.setDelFlag(1);
            BeanUtils.copyProperties(releaseDeployRecordRequest, releaseDelpoyRecord);
            if (!StringUtils.isEmpty(releaseDeployRecordRequest.getProjectId()) && !releaseDeployRecordRequest.getProjectId().matches("^[0-9]*$")) {
                pagination.setTotalPageForTotalSize(0);
                pagination.setDataList(new ArrayList<>());
                return ResultVo.Builder.SUCC().initSuccData(pagination);
            } else if (!StringUtils.isEmpty(releaseDeployRecordRequest.getProjectId()) && releaseDeployRecordRequest.getProjectId().matches("^[0-9]*$")) {
                releaseDelpoyRecord.setProjectId(Integer.parseInt(releaseDeployRecordRequest.getProjectId()));
            }
            Integer total = releaseDelpoyRecordService.pageTotal(releaseDelpoyRecord);
            pagination.setTotalPageForTotalSize(total);
            if (total == 0) {
                pagination.setDataList(new ArrayList<>());
                return ResultVo.Builder.SUCC().initSuccData(pagination);
            }

            Map<String, AppInfoDto> map = new HashMap();
            List<ReleaseDelpoyRecord> releaseDelpoyRecordList = releaseDelpoyRecordService.listPage(releaseDelpoyRecord);
            List<ReleaseDeployRecordVo> releaseDeployRecordVoList = releaseDelpoyRecordList.stream().map(releaseDelpoyRecord1 -> {
                ReleaseDeployRecordVo releaseDeployRecordVo = new ReleaseDeployRecordVo();
                BeanUtils.copyProperties(releaseDelpoyRecord1, releaseDeployRecordVo);
                if (releaseDelpoyRecord1.getDeployStatus().equals(PublishStatusEnum.RELEASE_FAIL) && releaseDelpoyRecord1.getSubDeployStatus() != null) {
                    releaseDeployRecordVo.setDeployStatusDesc(PublishStatusEnum.getValueByCode(releaseDelpoyRecord1.getDeployStatus()) + "-" + PublishStatusEnum.getValueByCode(releaseDelpoyRecord1.getSubDeployStatus()));
                } else {
                    releaseDeployRecordVo.setDeployStatusDesc(PublishStatusEnum.getValueByCode(releaseDelpoyRecord1.getDeployStatus()));
                }
                AppInfoDto appInfoDto = map.get(releaseDeployRecordVo.getAppCode());
                if (appInfoDto == null) {
                    try {
                        appInfoDto = appCenterUtil.getAppInfo(releaseDeployRecordVo.getAppCode(), "prod");
                        map.put(releaseDeployRecordVo.getAppCode(), appInfoDto);
                    } catch (Exception e) {
                        logger.error("查询应用信息错误: {}", e.getMessage());
                    }
                }
                releaseDeployRecordVo.setAppInfoDto(appInfoDto);

                return releaseDeployRecordVo;
            }).collect(Collectors.toList());
            pagination.setDataList(releaseDeployRecordVoList);
            return ResultVo.Builder.SUCC().initSuccData(pagination);
        } catch (Exception e) {
            logger.error("releaseDeployRecord list Exception , e = {}", e);
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("4000", "系统异常");
        }
    }

    @PostMapping("/listByCondition")
    @ResponseBody
    public ResultVo listByCondition(@RequestBody ReleaseDelpoyRecord releaseDelpoyRecord) {
        try {
            if (releaseDelpoyRecord == null) {
                logger.info("releaseDeployRecord listByCondition fail , param = {}", releaseDelpoyRecord);
                return ResultVo.Builder.FAIL().initErrCodeAndMsg("2000", "参数错误");
            }
            List<ReleaseDelpoyRecord> releaseDelpoyRecordList = releaseDelpoyRecordService.listByCondition(releaseDelpoyRecord);
            return ResultVo.Builder.SUCC().initSuccData(releaseDelpoyRecordList);
        } catch (Exception e) {
            logger.error("releaseDeployRecord listByCondition Exception , e = {}", e);
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("4000", "系统异常");
        }
    }

    @PostMapping("/batchInsert")
    @ResponseBody
    public ResultVo batchInsert(@RequestBody List<ReleaseDelpoyRecord> releaseDelpoyRecordList) {
        try {
            if (releaseDelpoyRecordList.isEmpty() || releaseDelpoyRecordList.size() <= 0) {
                logger.info("releaseDeployRecord batchInsert fail , param = {}", releaseDelpoyRecordList);
                return ResultVo.Builder.FAIL().initErrCodeAndMsg("2000", "参数错误");
            }
            int result = releaseDelpoyRecordService.batchInsert(releaseDelpoyRecordList);
            if (result <= 0) {
                logger.info("releaseDeployRecord batchInsert fail , param = {}", releaseDelpoyRecordList);
                return ResultVo.Builder.FAIL().initErrCodeAndMsg("2001", "新增失败");
            }
            return ResultVo.Builder.SUCC().initSuccData(releaseDelpoyRecordList);
        } catch (Exception e) {
            logger.error("releaseDeployRecord batchInsert Exception , e = {}", e);
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("4000", "系统异常");
        }
    }

    @PostMapping("/delete")
    @ResponseBody
    public ResultVo delete(@RequestBody ReleaseDelpoyRecord releaseDelpoyRecord) {
        try {
            if (releaseDelpoyRecord == null || releaseDelpoyRecord.getId() == null
                    || releaseDelpoyRecord.getId() <= 0) {
                logger.info("releaseDeployRecord delete fail , param = {}", releaseDelpoyRecord);
                return ResultVo.Builder.FAIL().initErrCodeAndMsg("2000", "参数错误");
            }
            int result = releaseDelpoyRecordService.deleteById(releaseDelpoyRecord);
            if (result <= 0) {
                logger.info("releaseDeployRecord delete fail , param = {}", releaseDelpoyRecord);
                return ResultVo.Builder.FAIL().initErrCodeAndMsg("2001", "删除失败");
            }
            return ResultVo.Builder.SUCC();
        } catch (Exception e) {
            logger.error("releaseDeployRecord delete Exception , e = {}", e);
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("4000", "系统异常");
        }
    }

    @PostMapping("/batchDelete")
    @ResponseBody
    public ResultVo batchDelete(@RequestBody List<ReleaseDelpoyRecord> releaseDelpoyRecordList) {
        try {
            if (releaseDelpoyRecordList.isEmpty() || releaseDelpoyRecordList.size() <= 0) {
                logger.info("releaseDeployRecord batchDelete fail , param = {}", releaseDelpoyRecordList);
                return ResultVo.Builder.FAIL().initErrCodeAndMsg("2000", "参数错误");
            }
            int result = releaseDelpoyRecordService.batchDelete(releaseDelpoyRecordList);
            if (result <= 0) {
                logger.info("releaseDeployRecord batchDelete fail , param = {}", releaseDelpoyRecordList);
                return ResultVo.Builder.FAIL().initErrCodeAndMsg("2001", "删除失败");
            }
            return ResultVo.Builder.SUCC();
        } catch (Exception e) {
            logger.error("releaseDeployRecord batchDelete Exception , e = {}", e);
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("4000", "系统异常");
        }
    }
}
