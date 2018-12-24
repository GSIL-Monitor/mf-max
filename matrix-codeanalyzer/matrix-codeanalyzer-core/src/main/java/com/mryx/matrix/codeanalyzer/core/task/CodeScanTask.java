package com.mryx.matrix.codeanalyzer.core.task;

import com.mryx.common.utils.HttpClientUtil;
import com.mryx.common.utils.HttpPoolClient;
import com.mryx.matrix.codeanalyzer.core.service.CodeService;
import com.mryx.matrix.codeanalyzer.core.utils.BranchAppCodeUtil;
import com.mryx.matrix.codeanalyzer.domain.CodeScanResult;
import com.mryx.matrix.publish.domain.ProjectTask;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;
import java.util.Map;
import java.util.Optional;

/**
 * Code Scan任务
 *
 * @author supeng
 * @date 2018/09/29
 */
@Slf4j
public class CodeScanTask implements Runnable {

    private static final HttpPoolClient HTTP_POOL_CLIENT = HttpClientUtil.create(2000, 2000, 5, 5, 1, 500);


    private CodeService codeService;

    private String codeScanCallbackRemote;

    private String appDetailRemote;

    private ProjectTask projectTask;

    private String appProjecttasksRemote;

    public CodeScanTask(CodeService codeService, String codeScanCallbackRemote, String appDetailRemote, String appProjecttasksRemote, ProjectTask projectTask) {
        this.codeService = codeService;
        this.codeScanCallbackRemote = codeScanCallbackRemote;
        this.appDetailRemote = appDetailRemote;
        this.appProjecttasksRemote = appProjecttasksRemote;
        this.projectTask = projectTask;
    }

    @Override
    public void run() {
        String appBranch = "";
        String appCode = "";
        Map<String, String> map = BranchAppCodeUtil.getBranchAndAppCode(projectTask, appProjecttasksRemote);
        if (!map.isEmpty()) {
            appBranch = map.get("branch");
            appCode = map.get("appCode");
        }
        log.info("branch is " + appBranch + " appCode is " + appCode);
        String gitAddress = codeService.getGitAddress(appDetailRemote, appCode).getData();
        log.info("gitAddress is {}", gitAddress);
        CodeScanResult codeScan = new CodeScanResult();
        codeScan.setProjectTaskId(projectTask.getId());
        codeScan.setManualOrAutomatic(0);
        codeScan.setGitAddress(gitAddress);
        codeScan.setCodeBranch(appBranch);
        codeScan.setCodeScanTime(new Date());
        codeScan.setStatus(2);
        codeScan.setIsMaster(0);
        Integer id=0;
        try {
            synchronized (CodeScanTask.class) {
                int cnt = codeService.insertCodeScanResult(codeScan);
                if (cnt > 0) {
                    log.info("创建代码扫描入库成功");
                    id = codeService.getIdByProjectTaskId(projectTask.getId());
                    if(id>0){
                        log.info("获取主键ID成功");
                    }else{
                        log.error("获取主键ID失败");
                    }
                }else{
                    log.error("创建代码扫描入库失败");
                }
            }
            Map<String, Object> parameters = codeService.setParameters(gitAddress, appBranch, id.toString());
            Optional<String> codeScanResult = codeService.codeScanRequest(codeScanCallbackRemote, parameters);
            if (codeScanResult.isPresent()) {
                log.info("代码扫描创建成功，已经成功调用对方接口");
            } else {
                log.error("代码扫描创建请求失败");
            }
        }catch (Exception e){
            log.error("{}",e);
        }
    }
}
