package com.mryx.matrix.process.web.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.mryx.matrix.common.domain.ResultVo;
import com.mryx.matrix.process.core.annotation.CcsPermission;
import com.mryx.matrix.process.core.service.BetaService;
import com.mryx.matrix.process.domain.BetaDTO;
import com.mryx.matrix.process.web.vo.BetaVO;
import io.swagger.models.auth.In;
import org.assertj.core.util.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * beta环境ip相关接口
 *
 * @author juqing
 * @date 2018/09/03
 */
@RestController
@RequestMapping("/api/beta")
public class BetaIpController {
    private static final Logger LOGGER = LoggerFactory.getLogger(BetaIpController.class);
    @Value("${k8s.docker.env}")
    private String dockerEnvList;
    @Resource
    private BetaService betaService;

    @CcsPermission("matrix:beta:list")
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


    @CcsPermission("matrix:beta:listBylikeIp")
    @RequestMapping(value = "/listBylikeIp", method = RequestMethod.POST)
    public ResultVo listBylikeIp(@RequestBody BetaDTO beta) {
        LOGGER.info("beta环境list请求参数:{}", JSON.toJSONString(beta));
        String betaIp = beta.getIp();
        try {
            JSONObject result = new JSONObject();
            List<BetaDTO> betaDTOs = betaService.listbyIp(betaIp);
            List<BetaVO> betas = betaDTOs.stream().map(betaDTO -> {
                BetaVO betaVO = new BetaVO();
                BeanUtils.copyProperties(betaDTO, betaVO);
                return betaVO;
            }).collect(Collectors.toList());
            result.put("dataList", betas);
            LOGGER.info("beta环境list请求返回结果是：{}", JSON.toJSONString(result));
            return ResultVo.Builder.SUCC().initSuccData(result);
        } catch (Exception e) {
            LOGGER.error("查询异常", e);
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("1", "查询发生异常");
        }

    }


    @CcsPermission("matrix:beta:ip")
    @RequestMapping(value = "/ip", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    public ResultVo getIp(@RequestBody BetaDTO beta) {
        Integer id = beta.getId();
        BetaDTO betaDto = betaService.getById(id);
        return ResultVo.Builder.SUCC().initSuccData(betaDto);
    }

    @CcsPermission("matrix:beta:add")
    @RequestMapping(value = "/add", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    public ResultVo add(@RequestBody BetaDTO beta) {
        LOGGER.info("beta环境add请求参数：{}", JSON.toJSONString(beta));
        if (null == beta.getIp() || null == beta.getEnv()) {
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("1", "请填写相应的环境名称和ip");
        }
        try {
            List<BetaDTO> betaDTOS = betaService.listByCondition(beta);
            if (betaDTOS != null && betaDTOS.size() != 0) {
                for (BetaDTO betaDto : betaDTOS) {
                    if (betaDto.getDelFlag() != 0) {
                        return ResultVo.Builder.FAIL().initErrCodeAndMsg("1", "已经存在相应的ip或者环境名称");
                    }
                }
            }
            int result = betaService.insert(beta);
            if (result > 0) {
                return ResultVo.Builder.SUCC().initSuccDataAndMsg("0", "添加成功");
            }
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("1", "添加失败");
        } catch (Exception e) {
            LOGGER.error("添加异常", e);
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("1", "添加发生异常");
        }
    }

    @CcsPermission("matrix:beta:update")
    @RequestMapping(value = "/update", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    public ResultVo update(@RequestBody BetaDTO beta) {
        LOGGER.info("beta环境update请求参数：{}", JSON.toJSONString(beta));
        Integer id = beta.getId();
        if (id == null) {
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("1", "id不能为空");
        }
        try {
            BetaDTO env = betaService.getById(id);
            if (null == env) {
                return ResultVo.Builder.FAIL().initErrCodeAndMsg("1", "环境不存在");
            }
            int result = betaService.updateById(beta);
            if (result > 0) {
                return ResultVo.Builder.SUCC().initSuccDataAndMsg("0", "更新成功");
            }
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("1", "更新失败");
        } catch (Exception e) {
            LOGGER.error("更新异常", e);
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("1", "更新发生异常");
        }

    }

    @CcsPermission("matrix:beta:delete")
    @RequestMapping(value = "/delete", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    public ResultVo delete(@RequestBody BetaDTO beta) {
        LOGGER.info("beta环境delete请求参数：{}", JSON.toJSONString(beta));
        Integer id = beta.getId();
        if (id == null) {
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("1", "id不能为空");
        }
        try {
            BetaDTO env = betaService.getById(id);
            if (null == env) {
                return ResultVo.Builder.FAIL().initErrCodeAndMsg("1", "环境不存在");
            }
            int result = betaService.deleteById(beta);
            if (result > 0) {
                return ResultVo.Builder.SUCC().initSuccDataAndMsg("0", "删除成功");
            }
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("1", "删除失败");
        } catch (Exception e) {
            LOGGER.error("删除异常", e);
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("1", "删除发生异常");
        }

    }


    @RequestMapping(value = "/dockerEnvlist", method = RequestMethod.POST)
    public ResultVo dockerEnvList() {
        try {
            if (StringUtils.isEmpty(dockerEnvList)) {
                LOGGER.info("docker环境为空");
                return ResultVo.Builder.FAIL().initErrCodeAndMsg("1","docker环境配置为空");
            }
            List<String> dockerEnvs = Lists.newArrayList(dockerEnvList.split(","));
            JSONObject result = new JSONObject();
            result.put("dockerEnvs", dockerEnvs);
            LOGGER.info("docker环境list请求返回结果是：{}", JSON.toJSONString(result));
            return ResultVo.Builder.SUCC().initSuccData(result);
        } catch (Exception e) {
            LOGGER.error("查询异常", e);
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("1", "查询发生异常");
        }

    }

}
