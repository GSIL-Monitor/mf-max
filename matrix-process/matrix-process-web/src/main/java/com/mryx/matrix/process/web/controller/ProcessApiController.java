package com.mryx.matrix.process.web.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.mryx.matrix.common.domain.ResultVo;
import com.mryx.matrix.process.core.service.BetaService;
import com.mryx.matrix.process.core.service.ProjectService;
import com.mryx.matrix.process.domain.BetaDTO;
import com.mryx.matrix.process.domain.Project;
import com.mryx.matrix.process.web.vo.BetaVO;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author pengcheng
 * @description
 * @email pengcheng@missfresh.cn
 * @date 2018-10-30 22:22
 **/
@RestController
@RequestMapping("/api/process/beta")
@Slf4j
public class ProcessApiController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProcessApiController.class);

    @Resource
    private BetaService betaService;

    @Resource
    private ProjectService projectService;

    @RequestMapping(value = "/list", method = RequestMethod.POST)
    public ResultVo list(@RequestBody BetaDTO beta) {
        LOGGER.info("beta环境list请求参数:{}", JSON.toJSONString(beta));
        Integer pageNo = beta.getPageNo();
        Integer pageSize = beta.getPageSize();
        try {
            JSONObject result = new JSONObject();

            int total;
            List<BetaDTO> betaDTOs;

            Integer canUse = beta.getCanUse();
            if (Integer.valueOf(1).equals(canUse)) {
                total = betaService.pageTotalCanUse(beta);
                betaDTOs = betaService.listPageCanUse(beta);
            } else {
                total = betaService.pageTotal(beta);
                betaDTOs = betaService.listPage(beta);
            }

            List<BetaVO> betas = betaDTOs.stream().map(betaDTO -> {
                BetaVO betaVO = new BetaVO();
                BeanUtils.copyProperties(betaDTO, betaVO);
                return betaVO;
            }).collect(Collectors.toList());
            int totalPage = (total % pageSize == 0) ? (total / pageSize) : (total / pageSize + 1);
            result.put("pageNo", pageNo);
            result.put("pageSize", pageSize);
            result.put("totalPage", totalPage);
            result.put("totalSize", total);
            result.put("dataList", betas);
            LOGGER.info("beta环境list请求返回结果是：{}", JSON.toJSONString(result));
            return ResultVo.Builder.SUCC().initSuccData(result);
        } catch (Exception e) {
            LOGGER.error("查询异常", e);
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("1", "查询发生异常");
        }

    }


    /**
     * 手动释放指定服务器
     *
     * @param beta
     * @return
     */
    @RequestMapping(value = "/releaseServer", method = RequestMethod.POST)
    public ResultVo releaseServer(@RequestBody BetaDTO beta) {
        try {
            LOGGER.info("释放服务器请求参数:{}", JSON.toJSONString(beta));

            String ip = beta.getIp();
            if (StringUtils.isEmpty(ip)) {
                return ResultVo.Builder.FAIL().initErrCodeAndMsg("2000", "释放IP不允许为空");
            }
            BetaDTO condition = new BetaDTO();
            condition.setIp(ip);
            condition.setDelFlag(1);
            List<BetaDTO> betaDTOList = betaService.listByCondition(condition);
            if (betaDTOList == null) {
                return ResultVo.Builder.FAIL().initErrCodeAndMsg("3000", "未找到指定服务器");
            }
            betaDTOList.stream().forEach(betaDTO -> {
                betaDTO.setCanUse(Integer.valueOf(1));
                betaDTO.setProjectId(null);
                betaDTO.setProjectName(null);
                betaService.updateById(betaDTO);
            });

            return ResultVo.Builder.SUCC().initSuccDataAndMsg("0", "释放成功");
        } catch (Exception e) {
            LOGGER.error("系统异常", e);
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("4000", "系统异常");
        }
    }

    /**
     * 根据projectID批量解锁服务器
     *
     * @param beta
     * @return
     */
    @RequestMapping(value = "/releaseServerByProjectId", method = RequestMethod.POST)
    public ResultVo releaseServerByProjectId(@RequestBody BetaDTO beta) {
        try {
            LOGGER.info("释放服务器请求参数:{}", JSON.toJSONString(beta));

            BetaDTO condition = new BetaDTO();
            Integer projectId = beta.getProjectId();

            if (projectId == null || projectId < 0) {
                return ResultVo.Builder.FAIL().initErrCodeAndMsg("2000", "项目ID不合法");
            }
            condition.setDelFlag(1);
            condition.setProjectId(projectId);

            List<BetaDTO> betaDTOList = betaService.listByCondition(condition);
            if (betaDTOList == null) {
                return ResultVo.Builder.FAIL().initErrCodeAndMsg("3000", "未找到指定服务器");
            }
            betaDTOList.stream().forEach(betaDTO -> {
                betaDTO.setCanUse(Integer.valueOf(1));
                betaDTO.setProjectId(null);
                betaDTO.setProjectName(null);
                betaService.updateById(betaDTO);
            });

            return ResultVo.Builder.SUCC().initSuccDataAndMsg("0", "释放成功");
        } catch (Exception e) {
            LOGGER.error("系统异常", e);
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("4000", "系统异常");
        }
    }

    /**
     * 查看服务器是否可用
     *
     * @param beta
     * @return
     */
    @RequestMapping(value = "/canUse", method = RequestMethod.POST)
    public ResultVo canUse(@RequestBody BetaDTO beta) {
        try {
            LOGGER.info("查看服务器是否可用请求参数:{}", JSON.toJSONString(beta));

            String ip = beta.getIp();
            if (StringUtils.isEmpty(ip)) {
                return ResultVo.Builder.FAIL().initErrCodeAndMsg("2000", "查看IP不允许为空");
            }
            BetaDTO condition = new BetaDTO();
            condition.setIp(ip);
            condition.setDelFlag(1);
            List<BetaDTO> betaDTOList = betaService.listByCondition(condition);
            if (CollectionUtils.isEmpty(betaDTOList)) {
                return ResultVo.Builder.FAIL().initErrCodeAndMsg("3000", "未找到指定服务器");
            }
            Integer projectId = beta.getProjectId();
            Map<String, Object> json = new HashMap<>();
            for (BetaDTO betaDTO : betaDTOList) {
                // canUse==1,返回服务器可以使用
                if (Integer.valueOf(1).equals(betaDTO.getCanUse())) {
                    json.put("canUse", Integer.valueOf(1));
                    return ResultVo.Builder.SUCC().initSuccData(JSON.toJSONString(json));
                }
                // 没有指定项目ID，返回服务器不可用
                if (projectId == null) {
                    json.put("canUse", Integer.valueOf(0));
                    json.put("projectName", betaDTO.getProjectName());
                    json.put("projectId", betaDTO.getProjectId());
                    return ResultVo.Builder.SUCC().initSuccData(JSON.toJSONString(json));
                }
                // 如果指定项目，如果服务器被指定项目占用，返回可用
                if (projectId.equals(betaDTO.getProjectId())) {
                    json.put("canUse", Integer.valueOf(1));
                    return ResultVo.Builder.SUCC().initSuccData(JSON.toJSONString(json));
                }
                json.put("canUse", Integer.valueOf(0));
                json.put("projectName", betaDTO.getProjectName());
                json.put("projectId", betaDTO.getProjectId());
                // 如果指定项目与服务器占用项目不等，返回不可用
                return ResultVo.Builder.SUCC().initSuccData(JSON.toJSONString(json));
            }
            return ResultVo.Builder.SUCC().initSuccDataAndMsg("5000", "处理错误");
        } catch (Exception e) {
            LOGGER.error("系统异常", e);
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("4000", "系统异常");
        }
    }

    /**
     * 锁定指定服务器
     *
     * @param beta
     * @return
     */
    @RequestMapping(value = "/lockServer", method = RequestMethod.POST)
    public ResultVo lockServer(@RequestBody BetaDTO beta) {
        try {
            LOGGER.info("锁定服务器请求参数:{}", JSON.toJSONString(beta));

            String ip = beta.getIp();
            if (StringUtils.isEmpty(ip)) {
                return ResultVo.Builder.FAIL().initErrCodeAndMsg("2000", "释放IP不允许为空");
            }
            Integer projectId = beta.getProjectId();
            if (projectId == null || projectId < 1) {
                return ResultVo.Builder.FAIL().initErrCodeAndMsg("2000", "参数错误，项目ID不合法");
            }

            Project project = projectService.getProjectById(projectId);
            if (project == null) {
                return ResultVo.Builder.FAIL().initErrCodeAndMsg("3000", "未找到指定项目");
            }
            String projectName = project.getProjectName();

            BetaDTO condition = new BetaDTO();
            condition.setIp(ip);
            condition.setDelFlag(1);
            List<BetaDTO> betaDTOList = betaService.listByCondition(condition);
            if (betaDTOList == null) {
                return ResultVo.Builder.FAIL().initErrCodeAndMsg("3000", "未找到指定服务器");
            }
            betaDTOList.stream().forEach(betaDTO -> {
                betaDTO.setCanUse(0);
                betaDTO.setProjectId(projectId);
                betaDTO.setProjectName(projectName);
                betaService.updateById(betaDTO);
            });

            return ResultVo.Builder.SUCC().initSuccDataAndMsg("0", "锁定成功");
        } catch (Exception e) {
            LOGGER.error("系统异常", e);
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("4000", "锁定异常");
        }
    }
}
