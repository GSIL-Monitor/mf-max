package com.mryx.matrix.codeanalyzer.core.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import com.mryx.common.utils.HttpClientUtil;
import com.mryx.common.utils.HttpPoolClient;
import com.mryx.matrix.codeanalyzer.core.dao.CodeDao;
import com.mryx.matrix.codeanalyzer.core.service.CodeScanResultService;
import com.mryx.matrix.codeanalyzer.core.service.CodeService;
import com.mryx.matrix.codeanalyzer.domain.CodeScanResult;
import com.mryx.matrix.codeanalyzer.dto.CodeScanResultDto;
import com.mryx.matrix.common.domain.ResultVo;
import com.mryx.matrix.publish.domain.ProjectTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Code Service Impl
 *
 * @author supeng
 * @date 2018/09/25
 */
@Service("codeService")
public class CodeServiceImpl implements CodeService {
    private static final Logger LOGGER = LoggerFactory.getLogger(CodeServiceImpl.class);
    private static final HttpPoolClient HTTP_POOL_CLIENT = HttpClientUtil.create(150000, 150000, 5, 5, 0, 150000);

    @Resource
    private CodeDao codeDao;

    @Resource
    private CodeScanResultService codeScanResultService;

    @Override
    public Map<String, Object> setParameters(String gitAddress, String codeBranch, String projectTaskId) {
        Map<String, Object> parameters = Maps.newHashMap();
        parameters.put("git", gitAddress);
        parameters.put("branch", codeBranch);
        parameters.put("id", projectTaskId);
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmm");
        String date = df.format(new Date());
        parameters.put("time", date);
        return parameters;
    }

    @Override
    public Optional<String> codeScanRequest(String url, Map<String, Object> json) {
        Optional<String> optional = null;
        try {
            optional = HTTP_POOL_CLIENT.postJson(url, JSONObject.toJSONString(json));
        } catch (NullPointerException e) {
            LOGGER.error("error ", e);
        }
        return optional;
    }

    @Override
    public int insertCodeScanResult(CodeScanResult codeScanResult) {
        return codeDao.insertCodeScanResult(codeScanResult);
    }

    @Override
    public ResultVo<String> getGitAddress(String url, String appCode) {
        Map<String, Object> mapInfo = Maps.newHashMap();
        mapInfo.put("appCode", appCode);
        mapInfo.put("envCode", "beta");
        Optional<String> optional = codeScanRequest(url, mapInfo);
        String gitAddress = "";
        if (optional.isPresent()) {
            JSONObject apps = (JSONObject) JSONObject.parse(optional.get().toString());
            if (("fail").equals(apps.get("ret"))) {
                LOGGER.error("调用失败: {}", url + JSON.toJSONString(optional));
            }
            if (null == apps.get("data") || apps.get("data").toString() == "") {
                return ResultVo.Builder.FAIL().initErrCodeAndMsg("1", "代码git地址获取失败！！！");
            }
            JSONObject appdatas = (JSONObject) JSONObject.parse(apps.get("data").toString());
            gitAddress = appdatas.getString("git");
            LOGGER.info("git address is " + gitAddress);
        }
        return ResultVo.Builder.SUCC().initSuccData(gitAddress);
    }

    @Override
    public ResultVo updateCodeScanStatus(CodeScanResultDto codeScanResultDto) {
        LOGGER.info("updateCodeScanStatus codeScanResultDto = {}", codeScanResultDto);
        if (codeScanResultDto == null || codeScanResultDto.getId() == null || codeScanResultDto.getId().equals(0)) {
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("1", "参数错误！");
        }
        try {
            LOGGER.info("尝试更新代码扫描结果");
            String ret = codeScanResultDto.getRet();
            CodeScanResult codeScanResult = codeScanResultDto.getData();
            if (codeScanResult == null) {
                codeScanResult = new CodeScanResult();
            }
            codeScanResult.setId(codeScanResultDto.getId());
            if ("success".equals(ret)) {
                codeScanResult.setStatus(0);
            } else if ("fail".equals(ret)) {
                codeScanResult.setStatus(1);
            }
            LOGGER.info("ret = {},codeScanResult = {}", ret, codeScanResult);
            int flag = codeDao.updateCodeScanStatus(codeScanResult);
            LOGGER.info("flag = {}", flag);
            if (flag > 0) {
                LOGGER.info("代码扫描结果更新成功");
                return ResultVo.Builder.SUCC();
            }
        } catch (Exception e) {
            LOGGER.error("代码扫描结果更新失败", e);
        }
        return ResultVo.Builder.FAIL().initErrCodeAndMsg("1", "更新失败！");
    }

    @Override
    public Integer getIdByProjectTaskId(Integer projectTaskId) {
        return codeDao.getIdByProjectTaskId(projectTaskId);
    }

    @Override
    public Map<String, String> compareResult(ProjectTask projectTask) {
        Map<String, String> codeScanResult = codeScanResultService.getCodeScanResult(projectTask.getId());
        Map<String, String> masterCodeScanResult = codeScanResultService.getMasterCodeScanResult(projectTask.getId());
        Map<String, String> result = Maps.newHashMap();
        try {
            if (codeScanResult != null && masterCodeScanResult != null) {
                //TODO 判断id
                String id = String.valueOf(codeScanResult.get("id"));
                String idMaster = String.valueOf(masterCodeScanResult.get("id"));
                if (Integer.valueOf(id) <= Integer.valueOf(idMaster)) {
                    result.put("flag", "false");
                    result.put("message", "应用代码上次发布之后未扫描过");
                    return result;
                } else {
                    Integer blocker = Integer.MAX_VALUE;
                    Integer critical = Integer.MAX_VALUE;
                    Integer blockerMaster = 0;
                    Integer criticalMaster = 0;
                    String blockerS = codeScanResult.get("blocker");
                    String criticalS = codeScanResult.get("critical");
                    String blockerMasterS = masterCodeScanResult.get("blocker");
                    String criticalMasterS = masterCodeScanResult.get("critical");
                    if (blockerS != null && blockerS != "") {
                        blocker = Integer.valueOf(blockerS);
                    }
                    if (criticalS != null && criticalS != "") {
                        critical = Integer.valueOf(criticalS);
                    }
                    if (blockerMasterS != null && blockerMasterS != "") {
                        blockerMaster = Integer.valueOf(blockerMasterS);
                    }
                    if (criticalMasterS != null && criticalMasterS != "") {
                        criticalMaster = Integer.valueOf(criticalMasterS);
                    }
                    if ((blocker <= blockerMaster) && (critical <= criticalMaster)) {
                        result.put("flag", "true");
                        result.put("message", "本次扫描结果低于上次发布时的扫描结果，可以发布！blocker:" + blocker + ",critical:" + critical);
                    } else {
                        result.put("flag", "false");
                        result.put("message", "本次扫描结果不低于上次发布时的扫描结果，不建议发布！blocker:" + blocker + ",critical:" + critical);
                    }
                }
            } else {
                result.put("flag", "false");
                result.put("message", "获取代码扫描结果失败");
            }
        } catch (Exception e) {
            result.put("flag", "false");
            result.put("message", "获取比对结果失败");
            LOGGER.error("获取比对结果失败", e);
        }
        return result;
    }

    @Override
    public Integer updateMaster(CodeScanResult codeScanResult) {
        return codeDao.updateMaster(codeScanResult);
    }

    @Override
    public Integer updateCodeScanResult(CodeScanResult codeScanResult) {
        return codeDao.updateCodeScanResult(codeScanResult);
    }

    @Override
    public Integer getIdByProjectTaskIdAndIsMaster(Map<String,Integer> para) {
        return codeDao.getIdByProjectTaskIdAndIsMaster(para);
    }
}
