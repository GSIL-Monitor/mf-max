package com.mryx.matrix.process.web.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.mryx.matrix.common.domain.ResultVo;
import com.mryx.matrix.process.core.service.ReportService;
import com.mryx.matrix.process.domain.ReportDTO;
import com.mryx.matrix.process.web.vo.ReportVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/report")
public class ReportController {
    private static final Logger LOGGER = LoggerFactory.getLogger(ReportController.class);

    @Resource
    private ReportService reportService;


    /**
     * 周报列表
     */
    @RequestMapping(value = "/listReports", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    public ResultVo list(@RequestBody ReportDTO report) {
        LOGGER.info("周报list请求参数:{}", JSON.toJSONString(report));
        try{
            Integer pageNo = report.getPageNo();
            Integer pageSize = report.getPageSize();
            JSONObject result = new JSONObject();
            int total = reportService.pageTotal(report);
            List<ReportDTO> reports = reportService.listPage(report);
            List<ReportVO> reportVOS = reports.stream().map(reportDTO -> {
                ReportVO reportVO = new ReportVO();
                BeanUtils.copyProperties(reportDTO, reportVO);
                return reportVO;
            }).collect(Collectors.toList());
            int totalPage = (total % pageSize == 0) ? (total / pageSize) : (total / pageSize + 1);
            result.put("pageNo", pageNo);
            result.put("pageSize", pageSize);
            result.put("totalPage", totalPage);
            result.put("totalSize", total);
            result.put("reports", reportVOS);
            LOGGER.info("周报list请求返回结果是：{}", JSON.toJSONString(result));
            return ResultVo.Builder.SUCC().initSuccData(result);
        }catch (Exception e){
            LOGGER.error("查询发生异常",e);
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("1", "查询异常");
        }

    }

    @RequestMapping(value = "/getReport", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    public ResultVo getReport(@RequestBody ReportDTO report) {
        Integer id = report.getId();
        ReportDTO reportDTO = reportService.getById(id);
        return ResultVo.Builder.SUCC().initSuccData(reportDTO);
    }

    @RequestMapping(value = "/add", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    public ResultVo add(@RequestBody ReportDTO report) {
        LOGGER.info("周报add请求参数：{}", JSON.toJSONString(report));
        try {
            int result = reportService.insert(report);
            if (result > 0) {
                return ResultVo.Builder.SUCC().initSuccDataAndMsg("0", "添加成功");
            }
        }catch (Exception e){
            LOGGER.error("添加发生异常",e);
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("1", "添加失败");
        }
        return ResultVo.Builder.FAIL().initErrCodeAndMsg("1", "添加失败");
    }

    @RequestMapping(value = "/update", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    public ResultVo update(@RequestBody ReportDTO report) {
        LOGGER.info("周报update请求参数：{}", JSON.toJSONString(report));
        Integer id = report.getId();
        if (id == null) {
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("1", "id不能为空");
        }
        try {
            ReportDTO rep = reportService.getById(id);
            if (null == rep) {
                return ResultVo.Builder.FAIL().initErrCodeAndMsg("1", "周报不存在");
            }
            int result = reportService.updateById(report);
            if (result > 0) {
                return ResultVo.Builder.SUCC().initSuccDataAndMsg("0", "更新成功");
            }
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("1", "更新失败");
        }catch (Exception e){
            LOGGER.error("更新发生异常",e);
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("1", "更新失败");
        }
    }
}
