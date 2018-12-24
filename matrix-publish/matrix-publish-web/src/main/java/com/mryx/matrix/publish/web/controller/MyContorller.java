package com.mryx.matrix.publish.web.controller;

import com.mryx.matrix.common.domain.ResultVo;
import com.mryx.matrix.publish.core.service.BetaDelpoyRecordService;
import com.mryx.matrix.publish.core.service.ReleaseDelpoyRecordService;
import com.mryx.matrix.publish.domain.BetaDelpoyRecord;
import com.mryx.matrix.publish.domain.ReleaseDelpoyRecord;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author pengcheng
 * @description
 * @email pengcheng@missfresh.cn
 * @date 2018-12-07 22:10
 **/
@Slf4j
@RestController
@RequestMapping("/api/publish/my")
public class MyContorller {

    @Resource
    BetaDelpoyRecordService betaDelpoyRecordService;

    @Resource
    ReleaseDelpoyRecordService releaseDelpoyRecordService;

    @PostMapping("/listProjectId")
    @ResponseBody
    public ResultVo listProjectId(@RequestBody BetaDelpoyRecord record) {
        try {
            Set<Integer> projectIdSet = new HashSet();
            String publishUser = record.getPublishUser();
            BetaDelpoyRecord betaCondition = new BetaDelpoyRecord();
            betaCondition.setDelFlag(1);
            betaCondition.setPublishUser(publishUser);
            List<BetaDelpoyRecord> betaRecoderList = betaDelpoyRecordService.listByCondition(betaCondition);
            if (!CollectionUtils.isEmpty(betaRecoderList)) {
                betaRecoderList.stream().forEach(betaRecoder -> {
                    Integer projectId = betaRecoder.getProjectId();
                    if (projectId != null && projectId > 0) {
                        projectIdSet.add(projectId);
                    }
                });
            }

            ReleaseDelpoyRecord releaseCondition = new ReleaseDelpoyRecord();
            releaseCondition.setPublishUser(publishUser);
            releaseCondition.setDelFlag(1);
            List<ReleaseDelpoyRecord> releaseRecoderList = releaseDelpoyRecordService.listByCondition(releaseCondition);
            if (!CollectionUtils.isEmpty(releaseRecoderList)) {
                releaseRecoderList.stream().forEach(releaseRecoder -> {
                    Integer projectId = releaseRecoder.getProjectId();
                    if (projectId != null && projectId > 0) {
                        projectIdSet.add(projectId);
                    }
                });
            }

            StringBuilder builder = new StringBuilder();
            if (!projectIdSet.isEmpty()) {
                projectIdSet.stream().forEach(projectId -> {
                    if (builder.length() > 0) {
                        builder.append(",");
                    }
                    builder.append(projectId);
                });
            }

            return ResultVo.Builder.SUCC().initSuccData(builder.toString());
        } catch (Exception e) {

            return ResultVo.Builder.FAIL().initErrCodeAndMsg("2000", "获取任务ID失败");
        }

    }
}
