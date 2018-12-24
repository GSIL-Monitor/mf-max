package com.mryx.matrix.publish.web.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import com.mryx.common.utils.HttpPoolClient;
import com.mryx.grampus.ccs.domain.OauthUser;
import com.mryx.matrix.common.domain.ResultVo;
import com.mryx.matrix.common.dto.AppInfoDto;
import com.mryx.matrix.common.dto.GroupInfoDto;
import com.mryx.matrix.common.dto.ServerResourceDto;
import com.mryx.matrix.common.enums.PackageType;
import com.mryx.matrix.publish.core.service.*;
import com.mryx.matrix.publish.core.utils.FileUtil;
import com.mryx.matrix.publish.domain.*;
import com.mryx.matrix.publish.enums.NexusTag;
import com.mryx.matrix.publish.enums.PublishStatusEnum;
import com.mryx.matrix.publish.web.vo.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * 发布 Controller
 *
 * @author supeng
 * @date 2018/09/03
 */
@RestController
@RequestMapping("/api/publish/publish")
public class PublishController {
    private static final Logger logger = LoggerFactory.getLogger(PublishController.class);
    private static final ExecutorService EXECUTE_SHELL = new ThreadPoolExecutor(10, 20, 10, TimeUnit.MINUTES, new LinkedBlockingQueue<>(10));
    private final HttpPoolClient HTTP_POOL_CLIENT = com.mryx.common.utils.HttpClientUtil.create(2000, 2000, 5, 5, 1, 500);
    private static HashMap<String, String> BETA_ADDRESS_MAP = new HashMap<>();
    private static HashMap<String, String> RELEASE_ADDRESS_MAP = new HashMap<>();
    private static HashMap<Integer, Integer> BETA_RECORD_MAP = new HashMap<>();
    private static HashMap<Integer, Integer> RELEASE_RECORD_MAP = new HashMap<>();
    private static HashMap<Integer, Boolean> BETA_STATUS_MAP = new HashMap<>();
    private static HashMap<Integer, Boolean> RELEASE_STATUS_MAP = new HashMap<>();
    private static HashMap<Integer, Integer> PLAN_RECORD_MAP = new HashMap<>();
    private static HashMap<Integer, Boolean> PLAN_STATUS_MAP = new HashMap<>();
    private static HashMap<Integer, Integer> PLAN_PLANRECORD_MAP = new HashMap<>();

    @Value("${betaDeployShellFilePwd}")
    private String betaDeployShellFilePwd;

    @Value("${releaseDeployShellFilePwd}")
    private String releaseDeployShellFilePwd;

    @Value("${getServerCountRemote}")
    private String appServerGetServerCountRemote;

    @Value("${checkIpCanUse}")
    private String checkIpCanUse;

    @Value("${lockServer}")
    private String lockServer;

    @Value("${releaseServer}")
    private String releaseServer;

    @Value("${releaseServerByProjectId}")
    private String releaseServerByProjectId;

    @Value("${mavenPath}")
    private String mavenPath;

    @Resource
    private RedisTemplate redisTemplate;

    @Resource
    BetaDelpoyRecordService betaDelpoyRecordService;

    @Resource
    PublishService publishService;

    @Resource
    ProjectTaskService projectTaskService;

    @Resource
    ReleaseDelpoyRecordService releaseDelpoyRecordService;

    @Resource
    ProjectTaskBatchRecordService projectTaskBatchRecordService;

    @Resource
    ProjectTaskBatchService projectTaskBatchService;

    @Resource
    DeployPlanService deployPlanService;

    @Resource
    DeployPlanRecordService deployPlanRecordService;

    @Resource
    PlanMachineMappingService planMachineMappingService;

    @Autowired
    AppCenterUtil appCenterUtil;

    @Value("${appDetail_remote}")
    private String appDetailRemote;
    @Value("${dealAccesstocken_url}")
    private String dealAccesstockenUrl;
    @Value("${betaScript}")
    private String betaScript;
    @Value("${k8sBetaScript}")
    private String k8sBetaScript;
    @Value("${releaseScript}")
    private String releaseScript;
    @Value("${codeReviewStatus_remote}")
    private String codeReviewStatusRemote;
    @Value("${modifyMachineRemote}")
    private String modifyMachineRemote;
    @Value("${getAppConfigRemote}")
    private String getAppConfigRemote;
    @Value("${getServerCountRemote}")
    private String getServerCountRemote;

    /*项目内测试发布入口*/
    @PostMapping("/betaPublish")
    @ResponseBody
    public ResultVo betaPublish(@RequestBody PublishParamVo publishParamVo, HttpServletRequest request) {
        try {
            logger.info("beta publish , param = {}", publishParamVo.toString());
            if (publishParamVo == null || StringUtils.isEmpty(publishParamVo.getPublishSequeneceVos())
                    || publishParamVo.getPublishSequeneceVos().size() <= 0) {
                logger.info("beta publish fail , 参数错误 ,param = {}", publishParamVo);
                return ResultVo.Builder.FAIL().initErrCodeAndMsg("2000", "参数错误");
            }
            Boolean isStressDeploy = publishParamVo.getIsStressDeploy();
            boolean isStress = isStressDeploy != null && isStressDeploy.booleanValue();
            Integer projectId = publishParamVo.getProjectId();
            if (isStress || publishParamVo.getIsDockerDeploy() == null) {
                publishParamVo.setIsDockerDeploy(false);
            }

            //非docker发布时，锁定服务器
            if (!publishParamVo.getIsDockerDeploy()) {
                StringBuilder builder = new StringBuilder();
                for (PublishSequeneceVo publishSequeneceVo : publishParamVo.getPublishSequeneceVos()) {
                    String appCode = publishSequeneceVo.getAppCode();
                    String checkIps;
                    if (isStress) {
                        AppInfoDto appInfo = appCenterUtil.getAppInfo(publishSequeneceVo.getAppCode(), "stress");
                        String serverIps = getServerIps(appInfo);
                        publishSequeneceVo.setStressIps(serverIps);
                        checkIps = serverIps;
                    } else {
                        checkIps = publishSequeneceVo.getServiceIps();
                    }

                    if (!betaCheck(appCode, checkIps)) {
                        logger.info("beta publish fail , 存在发布中的任务 , param = {}", checkIps);
                        return ResultVo.Builder.FAIL().initErrCodeAndMsg("2002", "存在发布中的应用" + appCode);
                    }
                    if (!isStress) {
                        builder.append(this.betaCheckIps(projectId, checkIps));
                        if (builder.length() > 0) {
                            builder.append(";  \r\n");
                        }
                    }
                    if (builder.length() > 0) {
                        ResultVo resultVo = ResultVo.Builder.FAIL().initErrCodeAndMsg("2003", builder.toString());
                        return resultVo;
                    }
                }

                if (!isStress) {
                    // 锁定项目使用的服务器
                    for (PublishSequeneceVo publishSequeneceVo : publishParamVo.getPublishSequeneceVos()) {
                        String ip = publishSequeneceVo.getServiceIps();
                        this.lockServer(ip, projectId);
                    }
                }
            }
            if (StringUtils.isEmpty(publishParamVo.getAccessToken())) {
                logger.error("beta publish no user info , param = {}", publishParamVo.getAccessToken());
                return ResultVo.Builder.FAIL().initErrCodeAndMsg("2004", "当前用户无token已过期或无发布权限");
            }
            String userAgent = request.getHeader("user-agent");
            String ip = getHttpIp(request);
            String userName = getUser(publishParamVo.getAccessToken(), userAgent, ip);
            if (userName == null) {
                logger.error("beta publish no user info , param = {}", publishParamVo.getAccessToken());
                return ResultVo.Builder.FAIL().initErrCodeAndMsg("2004", "当前用户无token已过期或无发布权限");
            }
            HashMap<String, Integer> map = Maps.newHashMap();
            for (PublishSequeneceVo publishSequeneceVo : publishParamVo.getPublishSequeneceVos()) {
                if (isStress) {
                    if (StringUtils.isEmpty(publishSequeneceVo.getStressIps())) {
                        logger.info("压测IP不能为空 , param = {}", publishSequeneceVo);
                        continue;
                    }
                } else if (publishParamVo.getIsDockerDeploy()) {
                    publishSequeneceVo.setIsDockerDeploy(true);
                } else {
                    publishSequeneceVo.setIsDockerDeploy(false);
                    if (publishSequeneceVo.getServiceIps().isEmpty() || publishSequeneceVo.getServiceIps().length() <= 0) {
                        logger.info("发布IP不能为空 , param = {}", publishSequeneceVo);
                        continue;
                    }
                }
                if (publishSequeneceVo.getAppCode() == null || publishSequeneceVo.getId() == null || publishSequeneceVo.getId() <= 0) {
                    logger.info("beta publish fail , 发布参数错误 , param = {}", publishSequeneceVo);
                    continue;
                }
                String profile = "beta";
                //未设置profile时，docker发布默认profile为autotest，beta发布默认发布profile为beta
                if (isStress) {
                    profile = "stress";
                    publishParamVo.setIsDockerDeploy(false);
                    publishSequeneceVo.setProfile("presstest");
                } else if (publishParamVo.getIsDockerDeploy()) {
                    publishSequeneceVo.setProfile("autotest");
                } else {
                    if (publishSequeneceVo.getProfile() != null && publishSequeneceVo.getProfile() == "dev") {
                        profile = publishSequeneceVo.getProfile();
                    }
                    if (publishSequeneceVo.getProfile() == null || publishSequeneceVo.getProfile() == "") {
                        publishSequeneceVo.setProfile("beta");
                    }
                }
                String appCode = publishSequeneceVo.getAppCode();
                AppInfoDto appInfo = publishParamVo.getIsDockerDeploy() ? appCenterUtil.getAppInfo(appCode, "prod") : appCenterUtil.getAppInfo(appCode, profile);
                if (appInfo == null) {
                    logger.info("未查询到对应的appInfo信息，appCode = {}", appCode);
                    continue;
                }
                if (!isStress && publishSequeneceVo.getIsDockerDeploy() != null && publishSequeneceVo.getIsDockerDeploy()) {
                    //这里需要注意，将业务线替换为应用中心的Id
                    publishSequeneceVo.setBizLine(appInfo.getDeptDto().getDeptId().toString());
                }
                BetaDelpoyRecord betaDelpoyRecord = trans(publishSequeneceVo, publishParamVo);
                betaDelpoyRecord.setPublishUser(userName);
                if (isStress) {
                    String serverIps = getServerIps(appInfo);
                    betaDelpoyRecord.setServiceIps(serverIps);
                    publishSequeneceVo.setStressDeploy(true);
                    publishSequeneceVo.setProfile("presstest");
                }
                int i = betaDelpoyRecordService.insert(betaDelpoyRecord);
                if (i <= 0) {
                    logger.info("beta publish record fail , 新增beta发布记录失败 , param = {}", publishSequeneceVo.toString());
                    continue;
                }
                ProjectTask projectTask = new ProjectTask();
                BeanUtils.copyProperties(publishSequeneceVo, projectTask);
                projectTask.setProjectId(publishParamVo.getProjectId());
                if (publishSequeneceVo.getIsDeploy()) {
                    projectTask.setIsDeploy(0);
                } else {
                    projectTask.setIsDeploy(1);
                }
                if (publishSequeneceVo.getIsDockerDeploy() != null && publishSequeneceVo.getIsDockerDeploy()) {
                    projectTask.setIsDockerDeploy(1);
                } else {
                    projectTask.setIsDockerDeploy(0);
                }
                projectTaskService.updateById(projectTask);
                map.put(betaDelpoyRecord.getAppCode(), betaDelpoyRecord.getId());
                BETA_RECORD_MAP.put(betaDelpoyRecord.getProjectTaskId(), betaDelpoyRecord.getId());
                //脚本中会回掉这个进行修改
                BETA_STATUS_MAP.put(betaDelpoyRecord.getId(), false);
                final AppInfoDto finalAppInfo = appInfo;
                Callable betaCallable = new Callable() {
                    @Override
                    public Object call() throws Exception {
                        deploy(betaDelpoyRecord, finalAppInfo, userName);
                        return null;
                    }
                };
                EXECUTE_SHELL.submit(betaCallable);
            }
            PublishResultVo publishResultVo = new PublishResultVo();
            publishResultVo.setBuildType("beta");
            publishResultVo.setAppCodeRecordMapping(map);

            try {
                logger.info("projectTasks{}" + projectTaskService.getProjectTasks(publishParamVo.getProjectId()));
                Optional<String> codeReviewStatus = HTTP_POOL_CLIENT.postJson(codeReviewStatusRemote, JSON.toJSONString(projectTaskService.getProjectTasks(publishParamVo.getProjectId())));
                logger.info("beta publish 调用获取代码评审状态的返回值为{}" + codeReviewStatus.get());
            } catch (RuntimeException e) {
                logger.error("beta publish codeReview error", e);
            }
            return ResultVo.Builder.SUCC().initSuccData(publishResultVo);
        } catch (Exception e) {
            logger.error("beta publish exception , e = {}", e);
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("4000", "系统异常");
        }
    }

    private String getServerIps(AppInfoDto appInfo) {
        final StringBuilder serverIps = new StringBuilder();
        List<GroupInfoDto> groupInfos = appInfo.getGroupInfo();
        if (!CollectionUtils.isEmpty(groupInfos)) {
            groupInfos.stream().forEach(groupInfoDto -> {
                List<ServerResourceDto> serverInfos = groupInfoDto.getServerInfo();
                if (!CollectionUtils.isEmpty(serverInfos)) {
                    serverInfos.stream().forEach(serverResourceDto -> {
                        String serverIp = serverResourceDto.getHostIp();
                        if (serverIps.length() > 0) {
                            serverIps.append(",");
                        }
                        serverIps.append(serverIp);
                    });
                }
            });
        }
        return serverIps.toString();
    }

    @PostMapping("/betaCreatePublish")
    @ResponseBody
    public ResultVo betaCreatePublish(@RequestBody ProjectTaskVo projectTaskVo, HttpServletRequest request) {
        try {
            if (projectTaskVo == null || projectTaskVo.getAppCode() == null
                    || projectTaskVo.getAppBranch() == null) {
                logger.info("beta create publish fail , param = {} ", projectTaskVo.toString());
                return ResultVo.Builder.FAIL().initErrCodeAndMsg("2000", "参数错误");
            }
            if (projectTaskVo.getServiceIps() == null && projectTaskVo.getDockerEnv() == null) {
                logger.info("环境不能为空");
                return ResultVo.Builder.FAIL().initErrCodeAndMsg("2000", "环境不能为空");
            }
            if (projectTaskVo.getIsDockerDeploy() == null) {
                projectTaskVo.setIsDockerDeploy(false);
            }
            Integer projectId = projectTaskVo.getProjectId();
            //非docker发布，锁机器
            String canUse = null;
            if (!projectTaskVo.getIsDockerDeploy()) {
                if (!betaCheck(projectTaskVo.getAppCode(), projectTaskVo.getServiceIps())) {
                    logger.info("存在发布的任务，发布任务param = {}", projectTaskVo.toString());
                    return ResultVo.Builder.FAIL().initErrCodeAndMsg("2002", "存在发布中的应用" + projectTaskVo.getAppCode());
                }
                String ip = projectTaskVo.getServiceIps();
                canUse = this.betaCheckIps(projectId, ip);
                if (!StringUtils.isEmpty(canUse)) {
                    logger.info("检查服务器占用失败，发布任务param = {}， 错误消息： {}", projectTaskVo.toString(), canUse);
//                return ResultVo.Builder.FAIL().initErrCodeAndMsg("2003", canUse);
                }

//            // 测试发布前锁定机器
//            this.lockServer(ip, projectId);
            }
            String profile = "beta";
            if (projectTaskVo.getIsDockerDeploy()) {
                projectTaskVo.setProfile("autotest");
            } else {
                if (projectTaskVo.getProfile() == null || projectTaskVo.getProfile() == "") {
                    projectTaskVo.setProfile("beta");
                } else if (projectTaskVo.getProfile() == "dev") {
                    profile = "dev";
                }
            }
            ResultVo resultVo = projectTaskVo.getIsDockerDeploy() ? getAppInfo(projectTaskVo.getAppCode(), "prod") : getAppInfo(projectTaskVo.getAppCode(), profile);
            logger.info("调用应用中心，请求结果：{}", resultVo.toString());
            AppInfoDto appInfo = null;
            if (resultVo.getCode().equals("0") && resultVo.getRet().equals("success")) {
                appInfo = (AppInfoDto) JSON.parseObject(String.valueOf(resultVo.getData()), AppInfoDto.class);
            } else {
                logger.info("未查询到对应的appInfo信息，appCode = {}", projectTaskVo.getAppCode());
                return ResultVo.Builder.FAIL().initErrCodeAndMsg("2001", "发布任务创建失败,无对应的应用信息");
            }
            if (projectTaskVo.getIsDockerDeploy()) {
                projectTaskVo.setBizLine(appInfo.getDeptDto().getDeptId().toString());
            }
            ProjectTask projectTask = new ProjectTask();
            BeanUtils.copyProperties(projectTaskVo, projectTask);
            if (projectTaskVo.getIsDockerDeploy()) {
                projectTask.setIsDockerDeploy(1);
            } else {
                projectTask.setIsDockerDeploy(0);
            }
            if (projectTaskVo.getIsDeploy()) {
                projectTask.setIsDeploy(0);
            } else {
                projectTask.setIsDeploy(1);
            }
            int result = projectTaskService.insert(projectTask);
            if (result <= 0) {
                logger.info("beta create publish fail , param = {} ", projectTask);
                return ResultVo.Builder.FAIL().initErrCodeAndMsg("2001", "发布任务创建失败");
            }
            if (StringUtils.isEmpty(projectTask.getAccessToken())) {
                logger.error("beta create publish no user info , param = {}", projectTask.getAccessToken());
                return ResultVo.Builder.FAIL().initErrCodeAndMsg("2004", "当前用户无token已过期或无发布权限");
            }
            String userAgent = request.getHeader("user-agent");
            String ip1 = getHttpIp(request);
            String userName = getUser(projectTask.getAccessToken(), userAgent, ip1);
            if (userName == null) {
                logger.error("beta create publish no user info , param = {}", projectTask.getAccessToken());
                return ResultVo.Builder.FAIL().initErrCodeAndMsg("2004", "当前用户无token已过期或无发布权限");
            }
            BetaDelpoyRecord betaDelpoyRecord = new BetaDelpoyRecord();
            betaDelpoyRecord.setAppCode(projectTask.getAppCode());
            betaDelpoyRecord.setAppBranch(projectTask.getAppBranch());
            betaDelpoyRecord.setServiceIps(projectTask.getServiceIps());
            betaDelpoyRecord.setProjectTaskId(projectTask.getId());
            betaDelpoyRecord.setPublishUser(userName);
            betaDelpoyRecord.setProfile(projectTask.getProfile());
            betaDelpoyRecord.setIsDeploy(projectTask.getIsDeploy());
            betaDelpoyRecord.setIsDockerDeploy(projectTask.getIsDockerDeploy());
            betaDelpoyRecord.setBizLine(projectTask.getBizLine());
            betaDelpoyRecord.setDockerEnv(projectTask.getDockerEnv());
            result = betaDelpoyRecordService.insert(betaDelpoyRecord);
            if (result <= 0) {
                logger.info("beta create publish fail , param = {} ", projectTask);
                return ResultVo.Builder.FAIL().initErrCodeAndMsg("2002", "发布记录创建失败");
            }
            BETA_STATUS_MAP.put(betaDelpoyRecord.getId(), false);
            final AppInfoDto finalAppInfo = appInfo;
            Callable betaCallable = new Callable() {
                @Override
                public Object call() throws Exception {
                    deploy(betaDelpoyRecord, finalAppInfo, userName);
                    return null;
                }
            };
            EXECUTE_SHELL.submit(betaCallable);
            HashMap<String, Integer> resultMap = new HashMap<>();
            resultMap.put(projectTask.getAppCode(), betaDelpoyRecord.getId());
            PublishResultVo publishResultVo = new PublishResultVo();
            publishResultVo.setBuildType("beta");
            publishResultVo.setAppCodeRecordMapping(resultMap);
            if (!StringUtils.isEmpty(canUse)) {
                publishResultVo.setTips(canUse + "，但发布流程不受影响将继续执行。");
            }
            return ResultVo.Builder.SUCC().initSuccData(publishResultVo);
        } catch (Exception e) {
            logger.error("beta create publish exception , e = {}", e);
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("4000", "系统异常");
        }
    }

    @PostMapping("/betaCallBack")
    @ResponseBody
    public ResultVo betaCallBack(@RequestBody DeployCallBack deployCallBack) {
        try {
            logger.info("beta call back param = {}", deployCallBack.toString());
            if (deployCallBack == null || Integer.valueOf(deployCallBack.getRecordId()) <= 0) {
                logger.info("publish beta callback fail:参数错误 , param = {}", deployCallBack);
                return ResultVo.Builder.FAIL().initErrCodeAndMsg("2000", "参数错误");
            }
            BetaDelpoyRecord betaDelpoyRecord = betaDelpoyRecordService.getById(Integer.valueOf(deployCallBack.getRecordId()));
            if (betaDelpoyRecord == null) {
                logger.info("publish beta callback fail:没有对应的发布记录 , param = {}", deployCallBack);
                return ResultVo.Builder.FAIL().initErrCodeAndMsg("2001", "没有对应的发布记录");
            }
            ProjectTask projectTask = projectTaskService.getById(betaDelpoyRecord.getProjectTaskId());
            if (deployCallBack.getTag() != "") {
                betaDelpoyRecord.setAppBtag(deployCallBack.getTag());
                projectTask.setAppBtag(deployCallBack.getTag());
            }
            if (deployCallBack.getDeployStatus() != "") {
                Integer status = betaDelpoyRecord.getDeployStatus();
                if (status.equals(PublishStatusEnum.BETA_RELEASE.getCode()) || status.equals(PublishStatusEnum.BETA_WAIT.getCode())
                        || status.equals(PublishStatusEnum.BUILD_SUCCESS.getCode()) || status.equals(PublishStatusEnum.CODE_PUSH_SUCCESS.getCode())
                        || status.equals(PublishStatusEnum.INIT.getCode())) {
                    Integer callBackStatus = Integer.valueOf(deployCallBack.getDeployStatus());
                    if (callBackStatus.equals(PublishStatusEnum.CODE_PUSH_FAIL.getCode()) || callBackStatus.equals(PublishStatusEnum.BUILD_FAIL.getCode())) {
                        betaDelpoyRecord.setDeployStatus(PublishStatusEnum.BETA_FAIL.getCode());
                        betaDelpoyRecord.setSubDeployStatus(callBackStatus);
                        projectTask.setTaskStatus(PublishStatusEnum.BETA_FAIL.getCode());
                        projectTask.setSubTaskStatus(callBackStatus);
                    } else {
                        betaDelpoyRecord.setDeployStatus(Integer.valueOf(deployCallBack.getDeployStatus()));
                        projectTask.setTaskStatus(Integer.valueOf(deployCallBack.getDeployStatus()));
                    }
                }
            }
            betaDelpoyRecordService.updateById(betaDelpoyRecord);
            projectTaskService.updateById(projectTask);
            BETA_STATUS_MAP.put(Integer.valueOf(deployCallBack.getRecordId()), true);
            return ResultVo.Builder.SUCC();
        } catch (Exception e) {
            logger.error("beta callBack exception , e = {}", e);
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("4000", "系统异常");
        }
    }

    @PostMapping("/betaLog")
    @ResponseBody
    public ResultVo betaLog(@RequestBody ProjectTask projectTask) {
        try {
            if (projectTask == null || projectTask.getId() <= 0) {
                logger.info("beta log fail , param = {}", projectTask);
                return ResultVo.Builder.FAIL().initErrCodeAndMsg("2000", "参数错误");
            }
            LogVo logVo = new LogVo();
            BetaDelpoyRecord betaDelpoyRecord = new BetaDelpoyRecord();
            Integer id = BETA_RECORD_MAP.get(projectTask.getId());
            if (id == null || id <= 0) {
                betaDelpoyRecord = betaDelpoyRecordService.getLastId(projectTask.getId());
                if (betaDelpoyRecord == null) {
                    logVo.setLogContent("");
                    logVo.setPublishCompleteFlag(true);
                    return ResultVo.Builder.SUCC().initSuccData(logVo);
                }
                if (betaDelpoyRecord.getId() == null) {
                    logger.info("beta log fail : 没有对应的发布记录, projectTaskId = {}", projectTask.getId());
                    return ResultVo.Builder.FAIL().initErrCodeAndMsg("2001", "没有对应的发布记录");
                }
                id = betaDelpoyRecord.getId();
                if (id == null || id <= 0) {
                    logVo.setLogContent("");
                    logVo.setPublishCompleteFlag(true);
                    return ResultVo.Builder.SUCC().initSuccData(logVo);
                }
                BETA_RECORD_MAP.put(projectTask.getId(), id);
            }
            String fileAddr = BETA_ADDRESS_MAP.get(String.valueOf(id));
            if (StringUtils.isEmpty(fileAddr)) {
                if (betaDelpoyRecord.getLogPath() == null) {
                    betaDelpoyRecord = betaDelpoyRecordService.getById(id);
                    if (betaDelpoyRecord == null) {
                        logger.info("beta log fail : 没有对应的发布记录, id = {}", id);
                        return ResultVo.Builder.FAIL().initErrCodeAndMsg("2001", "没有对应的发布记录");
                    }
                }
                fileAddr = betaDelpoyRecord.getLogPath();
                if (StringUtils.isEmpty(fileAddr) || fileAddr == "") {
                    logVo.setLogContent("");
                    logVo.setPublishCompleteFlag(true);
                    return ResultVo.Builder.SUCC().initSuccData(logVo);
                }
                BETA_ADDRESS_MAP.put(String.valueOf(id), fileAddr);
            }
            String log = FileUtil.initLog(fileAddr);
            if (BETA_RECORD_MAP.get(id) == null) {
                betaDelpoyRecord = betaDelpoyRecordService.getById(id);
                if (betaDelpoyRecord == null) {
                    logger.info("beta log fail : 没有对应的发布记录, id = {}", id);
                    return ResultVo.Builder.FAIL().initErrCodeAndMsg("2001", "没有对应的发布记录");
                }
                Integer status = betaDelpoyRecord.getDeployStatus();
                if (status.equals(PublishStatusEnum.BETA_SUCCESS.getCode()) || status.equals(PublishStatusEnum.BETA_FAIL.getCode())
                        || status.equals(PublishStatusEnum.BETA_SUSPEND.getCode())) {
                    BETA_STATUS_MAP.put(id, true);
                } else {
                    BETA_STATUS_MAP.put(id, false);
                }
            }
            logVo.setPublishCompleteFlag(BETA_STATUS_MAP.get(id));
            logVo.setLogContent(log);
            return ResultVo.Builder.SUCC().initSuccData(logVo);
        } catch (Exception e) {
            logger.error("beta log exception , e = {}", e);
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("4000", "系统异常");
        }
    }

    @PostMapping("/betaAppLog")
    @ResponseBody
    public ResultVo betaAppLog(@RequestBody BetaDelpoyRecord betaDelpoyRecord) {
        try {
            LogVo logVo = new LogVo();
            if (betaDelpoyRecord == null || betaDelpoyRecord.getId() == null || betaDelpoyRecord.getId() <= 0) {
                logger.info("beta log by appCode fail , param = {}", betaDelpoyRecord);
                return ResultVo.Builder.FAIL().initErrCodeAndMsg("2000", "参数错误");
            }
            String fileAddr = BETA_ADDRESS_MAP.get(String.valueOf(betaDelpoyRecord.getId()));
            if (StringUtils.isEmpty(fileAddr)) {
                if (betaDelpoyRecord.getLogPath() == null) {
                    betaDelpoyRecord = betaDelpoyRecordService.getById(betaDelpoyRecord.getId());
                    if (betaDelpoyRecord == null) {
                        logger.info("beta log fail : 没有对应的发布记录, id = {}", betaDelpoyRecord.getId());
                        return ResultVo.Builder.FAIL().initErrCodeAndMsg("2001", "没有对应的发布记录");
                    }
                }
                fileAddr = betaDelpoyRecord.getLogPath();
                if (StringUtils.isEmpty(fileAddr) || fileAddr == "") {
                    logVo.setLogContent("");
                    logVo.setPublishCompleteFlag(true);
                    return ResultVo.Builder.SUCC().initSuccData(logVo);
                }
                BETA_ADDRESS_MAP.put(String.valueOf(betaDelpoyRecord.getId()), fileAddr);
            }
            if (BETA_RECORD_MAP.get(betaDelpoyRecord.getId()) == null) {
                if (betaDelpoyRecord.getDeployStatus() == null) {
                    betaDelpoyRecord = betaDelpoyRecordService.getById(betaDelpoyRecord.getId());
                    if (betaDelpoyRecord == null) {
                        logger.info("beta log fail : 没有对应的发布记录, id = {}", betaDelpoyRecord.getId());
                        return ResultVo.Builder.FAIL().initErrCodeAndMsg("2001", "没有对应的发布记录");
                    }
                }
                Integer status = betaDelpoyRecord.getDeployStatus();
                if (status.equals(PublishStatusEnum.BETA_SUCCESS.getCode()) || status.equals(PublishStatusEnum.BETA_FAIL.getCode())
                        || status.equals(PublishStatusEnum.BETA_SUSPEND.getCode())) {
                    BETA_STATUS_MAP.put(betaDelpoyRecord.getId(), true);
                } else {
                    BETA_STATUS_MAP.put(betaDelpoyRecord.getId(), false);
                }
            }
            String log = FileUtil.initLog(fileAddr);
            logVo.setPublishCompleteFlag(BETA_STATUS_MAP.get(betaDelpoyRecord.getId()));
            logVo.setLogContent(log);
            return ResultVo.Builder.SUCC().initSuccData(logVo);
        } catch (Exception e) {
            logger.error("beta log by appCode exception , Exception = {}", e);
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("4000", "系统异常");
        }
    }

    @PostMapping("/betaSuspendByRecord")
    @ResponseBody
    public ResultVo betaSuspendByRecord(@RequestBody BetaDeployRecordVo betaDelpoyRecordVo) {
        try {
            logger.info("beta suspend by reocrd param , param = {}", betaDelpoyRecordVo.toString());
            if (betaDelpoyRecordVo == null || betaDelpoyRecordVo.getId() == null || betaDelpoyRecordVo.getId() <= 0) {
                logger.info("beta suspend by reocrd fail , param = {}", betaDelpoyRecordVo.toString());
                return ResultVo.Builder.FAIL().initErrCodeAndMsg("2000", "系统异常");
            }
            BetaDelpoyRecord betaDelpoyRecord = betaDelpoyRecordService.getById(betaDelpoyRecordVo.getId());
            Integer status = betaDelpoyRecord.getDeployStatus();
            if (status.equals(PublishStatusEnum.BETA_WAIT.getCode()) || status.equals(PublishStatusEnum.BETA_RELEASE.getCode())
                    || status.equals(PublishStatusEnum.BUILD_SUCCESS.getCode()) || status.equals(PublishStatusEnum.CODE_PUSH_SUCCESS.getCode())) {
                betaDelpoyRecord.setDeployStatus(PublishStatusEnum.BETA_SUSPEND.getCode());
                betaDelpoyRecordService.updateById(betaDelpoyRecord);
                ProjectTask projectTask = new ProjectTask();
                projectTask.setId(betaDelpoyRecord.getProjectTaskId());
                projectTask.setTaskStatus(PublishStatusEnum.BETA_SUSPEND.getCode());
                projectTaskService.updateById(projectTask);
                return ResultVo.Builder.SUCC().initSuccData("暂停成功");
            }
            return ResultVo.Builder.SUCC().initSuccData("操作成功，应用无需暂停");
        } catch (Exception e) {
            logger.error("beta suspend by reocrd exception , Exception = {}", e);
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("4000", "系统异常");
        }
    }

    @PostMapping("/releasePublish")
    @ResponseBody
    public ResultVo releasePublish(@RequestBody PublishParamVo publishParamVo, HttpServletRequest request) {
        try {
            if (publishParamVo == null || publishParamVo.getPublishSequeneceVos().isEmpty()
                    || publishParamVo.getPublishSequeneceVos().size() <= 0) {
                logger.info("release publish fail , param = {}", publishParamVo);
                return ResultVo.Builder.FAIL().initErrCodeAndMsg("2000", "参数错误");
            }
            for (PublishSequeneceVo publishSequeneceVo : publishParamVo.getPublishSequeneceVos()) {
                if (!releaseCheck(publishSequeneceVo.getAppCode())) {
                    logger.info("release publish 存在发布的任务，发布任务param = {}", publishSequeneceVo);
                    return ResultVo.Builder.FAIL().initErrCodeAndMsg("2002", "存在发布中的应用" + publishSequeneceVo.getAppCode());
                }
            }
            if (StringUtils.isEmpty(publishParamVo.getAccessToken())) {
                logger.error("release publish no user info , param = {}", publishParamVo.getAccessToken());
                return ResultVo.Builder.FAIL().initErrCodeAndMsg("2004", "当前用户无token已过期或无发布权限");
            }
            String userAgent = request.getHeader("user-agent");
            String ip = getHttpIp(request);
            String userName = getUser(publishParamVo.getAccessToken(), userAgent, ip);
            if (userName == null) {
                logger.error("release publish no user info , param = {}", publishParamVo.getAccessToken());
                return ResultVo.Builder.FAIL().initErrCodeAndMsg("2004", "当前用户无token已过期或无发布权限");
            }
            Collections.sort(publishParamVo.getPublishSequeneceVos(), new Comparator<PublishSequeneceVo>() {
                @Override
                public int compare(PublishSequeneceVo o1, PublishSequeneceVo o2) {
                    return o1.getSequenece() - o2.getSequenece();
                }
            });
            HashMap<String, Integer> map = new HashMap<>();
            int recordId = 0;
            for (PublishSequeneceVo publishSequeneceVo : publishParamVo.getPublishSequeneceVos()) {
                ReleaseDelpoyRecord releaseDelpoyRecord = transToReleaseDelpoyRecord(publishSequeneceVo, publishParamVo);
                releaseDelpoyRecord.setForwardRecordId(recordId);
                releaseDelpoyRecord.setDeployStatus(PublishStatusEnum.RELEASE_WAIT.getCode());
                releaseDelpoyRecord.setPublishUser(userName);
                int result = releaseDelpoyRecordService.insert(releaseDelpoyRecord);
                ProjectTask projectTask = new ProjectTask();
                projectTask.setId(publishSequeneceVo.getId());
                projectTask.setReleaseTaskStatus(PublishStatusEnum.RELEASE_WAIT.getCode());
                projectTaskService.updateById(projectTask);
                RELEASE_RECORD_MAP.put(publishSequeneceVo.getId(), releaseDelpoyRecord.getId());
                if (result <= 0) {
                    logger.info("release publish , deploy record insert fail , param = {}", publishSequeneceVo);
                    continue;
                }
                RELEASE_STATUS_MAP.put(releaseDelpoyRecord.getId(), false);
                recordId = releaseDelpoyRecord.getId();
                map.put(publishSequeneceVo.getAppCode(), recordId);
                if (publishSequeneceVo.getPublishBatchVoList().isEmpty() || publishSequeneceVo.getPublishBatchVoList().size() <= 0) {
                    logger.info("release publish , no batch , param = {}", publishSequeneceVo);
                    continue;
                }
                Collections.sort(publishSequeneceVo.getPublishBatchVoList(), new Comparator<PublishBatchVo>() {
                    @Override
                    public int compare(PublishBatchVo o1, PublishBatchVo o2) {
                        return o1.getSequenece() - o2.getSequenece();
                    }
                });
                projectTaskBatchService.deleteByProjectTaskId(publishSequeneceVo.getId());
                int batchId = 0;
                for (PublishBatchVo publishBatchVo : publishSequeneceVo.getPublishBatchVoList()) {
                    ProjectTaskBatch projectTaskBatch = new ProjectTaskBatch();
                    BeanUtils.copyProperties(publishBatchVo, projectTaskBatch);
                    projectTaskBatch.setId(null);
                    projectTaskBatch.setProjectTaskId(publishSequeneceVo.getId());
                    projectTaskBatch.setDeployStatus(PublishStatusEnum.RELEASE_WAIT.getCode());
                    projectTaskBatch.setAppIps(publishBatchVo.getServiceIps());
                    if (publishBatchVo.getWaitTime() == null) {
                        publishBatchVo.setWaitTime(0);
                    }
                    projectTaskBatch.setWaitTime(publishBatchVo.getWaitTime());
                    projectTaskBatchService.insert(projectTaskBatch);
                    ProjectTaskBatchRecord projectTaskBatchRecord = transToProjectTaskBatchRecord(publishBatchVo, recordId, publishSequeneceVo);
                    projectTaskBatchRecord.setForwardBatchId(batchId);
                    projectTaskBatchRecord.setProjectTaskBatchId(projectTaskBatch.getId());
                    projectTaskBatchRecord.setDeployStatus(PublishStatusEnum.RELEASE_WAIT.getCode());
                    int batchResult = projectTaskBatchRecordService.insert(projectTaskBatchRecord);
                    if (batchResult <= 0) {
                        logger.info("release publish , deploy batch record insert fail , param = {}", publishBatchVo);
                        continue;
                    }
                    batchId = projectTaskBatchRecord.getId();
                }
            }
            Callable releaseCallable = new Callable() {
                @Override
                public Object call() throws Exception {
                    ReleaseDelpoyRecord releaseDelpoyRecord = releaseDelpoyRecordService.getByProjectId(publishParamVo.getProjectId());
                    ProjectTaskBatchRecord projectTaskBatchRecord = projectTaskBatchRecordService.getFirstBatchByRecord(releaseDelpoyRecord.getId());
                    releaseDeploy(releaseDelpoyRecord, projectTaskBatchRecord, userName, null);
                    return null;
                }
            };
            EXECUTE_SHELL.submit(releaseCallable);
            PublishResultVo publishResultVo = new PublishResultVo();
            publishResultVo.setBuildType("release");
            publishResultVo.setAppCodeRecordMapping(map);

            try {
                logger.info("projectTasks{}" + projectTaskService.getProjectTasks(publishParamVo.getProjectId()));
                Optional<String> codeReviewStatus = HTTP_POOL_CLIENT.postJson(codeReviewStatusRemote, JSON.toJSONString(projectTaskService.getProjectTasks(publishParamVo.getProjectId())));
                logger.info("调用获取代码评审状态的返回值为{}" + codeReviewStatus.get());
            } catch (RuntimeException e) {
                logger.error("error", e);
            }

            return ResultVo.Builder.SUCC().initSuccData(publishResultVo);
        } catch (Exception e) {
            logger.error("release publish exception , e = {}", e);
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("4000", "系统异常");
        }
    }

    @PostMapping("/rollback")
    @ResponseBody
    public ResultVo rollback(@RequestBody RollBackParam rollBackParam, HttpServletRequest request) {
        try {
            if (rollBackParam == null) {
                logger.info("roll back fail :参数错误, param = {}", rollBackParam.toString());
                return ResultVo.Builder.FAIL().initErrCodeAndMsg("2000", "参数错误");
            }
            List<ProjectTask> projectTaskList = rollBackParam.getProjectTaskList();
            logger.info("roll back request , param = {}", projectTaskList.toString());
            if (projectTaskList.isEmpty() || projectTaskList.size() <= 0) {
                logger.info("roll back fail :参数错误, param = {}", projectTaskList.toString());
                return ResultVo.Builder.FAIL().initErrCodeAndMsg("2000", "参数错误");
            }
            for (ProjectTask projectTask : projectTaskList) {
                if (!releaseCheck(projectTask.getAppCode())) {
                    logger.info("回滚时存在发布的任务，发布appCode = {}", projectTask.getAppCode());
                    return ResultVo.Builder.FAIL().initErrCodeAndMsg("2002", "存在发布中的应用,请先暂停发布中的应用" + projectTask.getAppCode());
                }
            }
            if (StringUtils.isEmpty(rollBackParam.getAccessToken())) {
                logger.error("roll back no user info , param = {}", rollBackParam.getAccessToken());
                return ResultVo.Builder.FAIL().initErrCodeAndMsg("2004", "当前用户无token已过期或无发布权限");
            }
            String userAgent = request.getHeader("user-agent");
            String ip = getHttpIp(request);
            String userName = getUser(rollBackParam.getAccessToken(), userAgent, ip);
            if (userName == null) {
                logger.error("roll back no user info , param = {}", rollBackParam.getAccessToken());
                return ResultVo.Builder.FAIL().initErrCodeAndMsg("2004", "当前用户无token已过期或无发布权限");
            }
            Collections.sort(projectTaskList, new Comparator<ProjectTask>() {
                @Override
                public int compare(ProjectTask o1, ProjectTask o2) {
                    return o2.getSequenece() - o1.getSequenece();
                }
            });
            int recordId = 0;
            HashMap<String, Integer> resultMap = new HashMap<>();
            for (ProjectTask projectTask : projectTaskList) {
                ReleaseDelpoyRecord releaseDelpoyRecord = new ReleaseDelpoyRecord();
                releaseDelpoyRecord.setForwardRecordId(recordId);
                releaseDelpoyRecord.setAppBtag(projectTask.getAppRtag());
                releaseDelpoyRecord.setAppCode(projectTask.getAppCode());
                releaseDelpoyRecord.setProjectTaskId(projectTask.getId());
                releaseDelpoyRecord.setProjectId(projectTask.getProjectId());
                releaseDelpoyRecord.setPublishUser(userName);
                releaseDelpoyRecord.setDeployStatus(PublishStatusEnum.RELEASE_WAIT.getCode());
                ResultVo resultVo = getAppInfo(projectTask.getAppCode(), "prod");
                AppInfoDto appInfo = null;
                if (resultVo.getCode().equals("0") && resultVo.getRet().equals("success")) {
                    appInfo = (AppInfoDto) JSON.parseObject(String.valueOf(resultVo.getData()), AppInfoDto.class);
                } else {
                    logger.error("未查询到对应的appInfo信息，appCode = {}", projectTask.getAppCode());
                    continue;
                }
                if (appInfo.getGroupInfo().isEmpty() || appInfo.getGroupInfo().size() <= 0) {
                    logger.error("roll back fail : appCode info no groupInfo , appCode = {}", projectTask.getAppCode());
                    continue;
                }
                String ips = "";
                HashMap<String, String> map = new HashMap<>();
                for (GroupInfoDto groupInfoDto : appInfo.getGroupInfo()) {
                    String groupIps = "";
                    for (int i = 0; i < groupInfoDto.getServerInfo().size(); i++) {
                        ips += groupInfoDto.getServerInfo().get(i).getHostIp() + ",";
                        groupIps += groupInfoDto.getServerInfo().get(i).getHostIp() + ",";
                    }
                    map.put(groupInfoDto.getGroupName(), groupIps);
                }
                releaseDelpoyRecord.setServiceIps(ips);
                int result = releaseDelpoyRecordService.insert(releaseDelpoyRecord);
                if (result <= 0) {
                    continue;
                }
                recordId = releaseDelpoyRecord.getId();
                RELEASE_RECORD_MAP.put(projectTask.getId(), releaseDelpoyRecord.getId());
                RELEASE_STATUS_MAP.put(releaseDelpoyRecord.getId(), false);
                List<ProjectTaskBatchRecord> projectTaskBatchRecordList = new ArrayList<>();
                for (String key : map.keySet()) {
                    ProjectTaskBatchRecord projectTaskBatchRecord = new ProjectTaskBatchRecord();
                    projectTaskBatchRecord.setServiceIps(map.get(key));
                    projectTaskBatchRecord.setDeployRecordId(releaseDelpoyRecord.getId());
                    projectTaskBatchRecord.setProjectTaskId(releaseDelpoyRecord.getProjectTaskId());
                    projectTaskBatchRecord.setMachineCount(map.get(key).split(",").length);
                    projectTaskBatchRecord.setWaitTime(1);
                    projectTaskBatchRecord.setAppGroup(key);
                    projectTaskBatchRecord.setProjectTaskId(projectTask.getId());
                    projectTaskBatchRecord.setDeployRecordId(releaseDelpoyRecord.getId());
                    projectTaskBatchRecordList.add(projectTaskBatchRecord);
                }
                projectTaskBatchRecordService.batchInsert(projectTaskBatchRecordList);
                resultMap.put(releaseDelpoyRecord.getAppCode(), releaseDelpoyRecord.getId());
            }
            ReleaseDelpoyRecord releaseDelpoyRecord = releaseDelpoyRecordService.getByProjectId(projectTaskList.get(0).getProjectId());
            List<ProjectTaskBatchRecord> projectTaskBatchRecordList = projectTaskBatchRecordService.getByRecordId(releaseDelpoyRecord.getId());
            for (ProjectTaskBatchRecord projectTaskBatchRecord : projectTaskBatchRecordList) {
                Callable releaseCallable = new Callable() {
                    @Override
                    public Object call() throws Exception {
                        releaseDeploy(releaseDelpoyRecord, projectTaskBatchRecord, userName, null);
                        return null;
                    }
                };
                EXECUTE_SHELL.submit(releaseCallable);
            }
            PublishResultVo publishResultVo = new PublishResultVo();
            publishResultVo.setBuildType("release");
            publishResultVo.setAppCodeRecordMapping(resultMap);
            return ResultVo.Builder.SUCC().initSuccData(publishResultVo);
        } catch (Exception e) {
            logger.error("roll back exception , Exception = {}", e);
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("4000", "系统异常");
        }
    }

    @PostMapping("/rollbackByRecord")
    @ResponseBody
    public ResultVo rollbackByRecord(@RequestBody ReleaseDelpoyRecord releaseDelpoyRecord, HttpServletRequest request) {
        try {
            logger.info("rollback by record , param = {}", releaseDelpoyRecord);
            if (releaseDelpoyRecord == null || releaseDelpoyRecord.getAppCode() == null) {
                logger.info("rollback by record fail , 参数错误 , param = {}", releaseDelpoyRecord.toString());
                return ResultVo.Builder.FAIL().initErrCodeAndMsg("2000", "参数错误");
            }
            if (!releaseCheck(releaseDelpoyRecord.getAppCode())) {
                logger.info("rollback by record fail , 存在发布中的应用 , appCode = {}", releaseDelpoyRecord.getAppCode());
                return ResultVo.Builder.FAIL().initErrCodeAndMsg("2002", "存在发布中的应用");
            }
            ResultVo resultVo = getAppInfo(releaseDelpoyRecord.getAppCode(), "prod");
            AppInfoDto appInfo = null;
            if (resultVo.getCode().equals("0") && resultVo.getRet().equals("success")) {
                appInfo = (AppInfoDto) JSON.parseObject(String.valueOf(resultVo.getData()), AppInfoDto.class);
            } else {
                logger.error("未查询到对应的appInfo信息，appCode = {}", releaseDelpoyRecord.getAppCode());
                return ResultVo.Builder.FAIL().initErrCodeAndMsg("2001", "没有对应的应用信息,appCode:" + releaseDelpoyRecord.getAppCode());
            }
            if (appInfo.getGroupInfo().isEmpty() || appInfo.getGroupInfo().size() <= 0) {
                logger.error("roll back fail : appCode info no groupInfo , appCode = {}", releaseDelpoyRecord.getAppCode());
                return ResultVo.Builder.FAIL().initErrCodeAndMsg("2001", "应用没有对应的机器信息,appCode:" + releaseDelpoyRecord.getAppCode());
            }
            if (StringUtils.isEmpty(releaseDelpoyRecord.getAccessToken())) {
                logger.error("rollback by recordh no user info , param = {}", releaseDelpoyRecord.getAccessToken());
                return ResultVo.Builder.FAIL().initErrCodeAndMsg("2004", "当前用户无token已过期或无发布权限");
            }
            String userAgent = request.getHeader("user-agent");
            String ip = getHttpIp(request);
            String userName = getUser(releaseDelpoyRecord.getAccessToken(), userAgent, ip);
            if (userName == null) {
                logger.error("rollback by record no user info , param = {}", releaseDelpoyRecord.getAccessToken());
                return ResultVo.Builder.FAIL().initErrCodeAndMsg("2004", "当前用户无token已过期或无发布权限");
            }
            String ips = "";
            HashMap<String, String> map = new HashMap<>();
            for (GroupInfoDto groupInfoDto : appInfo.getGroupInfo()) {
                String groupIps = "";
                for (int i = 0; i < groupInfoDto.getServerInfo().size(); i++) {
                    ips += groupInfoDto.getServerInfo().get(i).getHostIp() + ",";
                    groupIps += groupInfoDto.getServerInfo().get(i).getHostIp() + ",";
                }
                map.put(groupInfoDto.getGroupName(), groupIps);
            }
            ReleaseDelpoyRecord releaseDelpoyRecord1 = new ReleaseDelpoyRecord();
            releaseDelpoyRecord1.setServiceIps(ips);
            releaseDelpoyRecord1.setAppBtag(releaseDelpoyRecord.getAppRtag());
            releaseDelpoyRecord1.setAppBranch(releaseDelpoyRecord.getAppRtag());
            releaseDelpoyRecord1.setAppCode(releaseDelpoyRecord.getAppCode());
            releaseDelpoyRecord1.setProjectId(releaseDelpoyRecord.getProjectId());
            releaseDelpoyRecord1.setProjectTaskId(releaseDelpoyRecord.getProjectTaskId());
            releaseDelpoyRecord1.setDeployStatus(PublishStatusEnum.RELEASE_WAIT.getCode());
            releaseDelpoyRecord1.setPublishUser(userName);
            int result = releaseDelpoyRecordService.insert(releaseDelpoyRecord1);
            if (result <= 0) {
                return ResultVo.Builder.FAIL().initErrCodeAndMsg("2003", "回滚失败");
            }
            RELEASE_STATUS_MAP.put(releaseDelpoyRecord.getId(), false);
            List<ProjectTaskBatchRecord> projectTaskBatchRecordList = new ArrayList<>();
            for (String key : map.keySet()) {
                if (map.get(key) == null) {
                    continue;
                }
                ProjectTaskBatchRecord projectTaskBatchRecord = new ProjectTaskBatchRecord();
                projectTaskBatchRecord.setServiceIps(map.get(key));
                projectTaskBatchRecord.setDeployRecordId(releaseDelpoyRecord1.getId());
                projectTaskBatchRecord.setProjectTaskId(releaseDelpoyRecord1.getProjectTaskId());
                projectTaskBatchRecord.setAppGroup(key);
                projectTaskBatchRecord.setWaitTime(1);
                projectTaskBatchRecord.setDeployStatus(PublishStatusEnum.RELEASE_WAIT.getCode());
                if (map.get(key) == null) {
                    projectTaskBatchRecord.setMachineCount(0);
                } else {
                    projectTaskBatchRecord.setMachineCount(map.get(key).split(",").length);
                }
                projectTaskBatchRecordList.add(projectTaskBatchRecord);
            }
            if (projectTaskBatchRecordList.isEmpty() || projectTaskBatchRecordList.size() <= 0) {
                return ResultVo.Builder.FAIL().initErrCodeAndMsg("2003", "回滚失败");
            }
            projectTaskBatchRecordService.batchInsert(projectTaskBatchRecordList);
            for (ProjectTaskBatchRecord projectTaskBatchRecord : projectTaskBatchRecordList) {
                Callable releaseCallable = new Callable() {
                    @Override
                    public Object call() throws Exception {
                        releaseDeploy(releaseDelpoyRecord1, projectTaskBatchRecord, userName, null);
                        return null;
                    }
                };
                EXECUTE_SHELL.submit(releaseCallable);
            }
            HashMap<String, Integer> resultMap = new HashMap<>();
            resultMap.put(releaseDelpoyRecord1.getAppCode(), releaseDelpoyRecord1.getId());
            PublishResultVo publishResultVo = new PublishResultVo();
            publishResultVo.setBuildType("release");
            publishResultVo.setAppCodeRecordMapping(resultMap);
            return ResultVo.Builder.SUCC().initSuccData(publishResultVo);
        } catch (Exception e) {
            logger.error("rollback by record exception , Exception = {}", e);
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("4000", "系统异常");
        }
    }

    @PostMapping("/releaseCreatePublish")
    @ResponseBody
    public ResultVo releaseCreatePublish(@RequestBody ProjectTask projectTask, HttpServletRequest request) {
        try {
            if (projectTask == null || projectTask.getAppCode() == null || projectTask.getAppBranch() == null
                    || projectTask.getProjectTaskBatchList() == null || projectTask.getProjectTaskBatchList().size() <= 0) {
                logger.info("release create publish fail , param = {} ", projectTask);
                return ResultVo.Builder.FAIL().initErrCodeAndMsg("2000", "参数错误");
            }
            if (!releaseCheck(projectTask.getAppCode())) {
                logger.info("存在发布的任务，发布任务param = {}", projectTask);
                return ResultVo.Builder.FAIL().initErrCodeAndMsg("2002", "存在发布中的应用" + projectTask.getAppCode());
            }
            int result = projectTaskService.insert(projectTask);
            if (result <= 0) {
                logger.info("release create publish fail , param = {} ", projectTask);
                return ResultVo.Builder.FAIL().initErrCodeAndMsg("2001", "发布任务创建失败");
            }
            for (ProjectTaskBatch projectTaskBatch : projectTask.getProjectTaskBatchList()) {
                projectTaskBatch.setProjectTaskId(projectTask.getId());
                projectTaskBatch.setDeployStatus(PublishStatusEnum.RELEASE_WAIT.getCode());
            }

            result = projectTaskBatchService.batchInsert(projectTask.getProjectTaskBatchList());
            if (result <= 0) {
                logger.info("release create publish fail , param = {} ", projectTask);
                return ResultVo.Builder.FAIL().initErrCodeAndMsg("2001", "发布批次创建失败");
            }
            if (StringUtils.isEmpty(projectTask.getAccessToken())) {
                logger.error("release create publish no user info , param = {}", projectTask.getAccessToken());
                return ResultVo.Builder.FAIL().initErrCodeAndMsg("2004", "当前用户无token已过期或无发布权限");
            }
            String userAgent = request.getHeader("user-agent");
            String ip = getHttpIp(request);
            String userName = getUser(projectTask.getAccessToken(), userAgent, ip);
            if (userName == null) {
                logger.error("release create publish no user info , param = {}", projectTask.getAccessToken());
                return ResultVo.Builder.FAIL().initErrCodeAndMsg("2004", "当前用户无token已过期或无发布权限");
            }
            ReleaseDelpoyRecord releaseDelpoyRecord = new ReleaseDelpoyRecord();
            releaseDelpoyRecord.setAppCode(projectTask.getAppCode());
            releaseDelpoyRecord.setAppBranch(projectTask.getAppBranch());
            releaseDelpoyRecord.setAppBtag(projectTask.getAppBranch());
            releaseDelpoyRecord.setProjectTaskId(projectTask.getId());
            releaseDelpoyRecord.setDeployStatus(PublishStatusEnum.RELEASE_WAIT.getCode());
            releaseDelpoyRecord.setPublishUser(userName);
            result = releaseDelpoyRecordService.insert(releaseDelpoyRecord);
            if (result <= 0) {
                logger.info("release create publish fail , param = {} ", projectTask);
                return ResultVo.Builder.FAIL().initErrCodeAndMsg("2001", "发布记录创建失败");
            }
            RELEASE_STATUS_MAP.put(releaseDelpoyRecord.getId(), false);
            Collections.sort(projectTask.getProjectTaskBatchList(), new Comparator<ProjectTaskBatch>() {
                @Override
                public int compare(ProjectTaskBatch o1, ProjectTaskBatch o2) {
                    return o1.getSequenece() - o2.getSequenece();
                }
            });
            Integer batchId = 0;
            for (ProjectTaskBatch projectTaskBatch : projectTask.getProjectTaskBatchList()) {
                ProjectTaskBatchRecord projectTaskBatchRecord = new ProjectTaskBatchRecord();
                projectTaskBatchRecord.setServiceIps(projectTaskBatch.getAppIps());
                projectTaskBatchRecord.setForwardBatchId(batchId);
                projectTaskBatchRecord.setProjectTaskId(projectTask.getId());
                projectTaskBatchRecord.setAppGroup(projectTaskBatch.getAppGroup());
                projectTaskBatchRecord.setWaitTime(projectTaskBatch.getWaitTime());
                projectTaskBatchRecord.setSequenece(projectTaskBatch.getSequenece());
                projectTaskBatchRecord.setDeployRecordId(releaseDelpoyRecord.getId());
                projectTaskBatchRecord.setProjectTaskBatchId(projectTaskBatch.getId());
                projectTaskBatchRecord.setMachineCount(projectTaskBatch.getMachineCount());
                projectTaskBatchRecord.setDeployStatus(PublishStatusEnum.RELEASE_WAIT.getCode());
                result = projectTaskBatchRecordService.insert(projectTaskBatchRecord);
                if (result <= 0) {
                    continue;
                }
                batchId = projectTaskBatchRecord.getId();
            }
            ProjectTaskBatchRecord projectTaskBatchRecord = projectTaskBatchRecordService.getFirstBatchByRecord(releaseDelpoyRecord.getId());
            Callable releaseCallable = new Callable() {
                @Override
                public Object call() throws Exception {

                    releaseDeploy(releaseDelpoyRecord, projectTaskBatchRecord, userName, null);
                    return null;
                }
            };
            EXECUTE_SHELL.submit(releaseCallable);
            HashMap<String, Integer> map = new HashMap<>();
            map.put(projectTask.getAppCode(), releaseDelpoyRecord.getId());
            PublishResultVo publishResultVo = new PublishResultVo();
            publishResultVo.setBuildType("release");
            publishResultVo.setAppCodeRecordMapping(map);
            return ResultVo.Builder.SUCC().initSuccData(publishResultVo);
        } catch (Exception e) {
            logger.error("release create publish exception , e = {}", e);
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("4000", "系统异常");
        }
    }

    @PostMapping("/suspend")
    @ResponseBody
    public ResultVo suspend(@RequestBody PublishParamVo publishParamVo) {
        try {
            logger.info("suspend param = {}", publishParamVo.toString());
            if (publishParamVo == null || publishParamVo.getProjectId() == null || publishParamVo.getProjectId() <= 0
                    || publishParamVo.getPublishSequeneceVos() == null || publishParamVo.getPublishSequeneceVos().size() <= 0) {
                logger.info("suspend fail ,param = {}", publishParamVo.toString());
                return ResultVo.Builder.FAIL().initErrCodeAndMsg("2000", "参数错误");
            }
            for (PublishSequeneceVo publishSequeneceVo : publishParamVo.getPublishSequeneceVos()) {
                ReleaseDelpoyRecord releaseDelpoyRecord = releaseDelpoyRecordService.getByLastId(publishSequeneceVo.getId());
                if (releaseDelpoyRecord == null || releaseDelpoyRecord.getDeployStatus() == null) {
                    logger.info("suspend fail , projectTask = {}", publishSequeneceVo.toString());
                    continue;
                }
                Integer status = releaseDelpoyRecord.getDeployStatus();
                if (status.equals(PublishStatusEnum.BUILD_SUCCESS.getCode()) || status.equals(PublishStatusEnum.RELEASE_RELEASE.getCode())
                        || status.equals(PublishStatusEnum.CODE_PUSH_SUCCESS.getCode()) || status.equals(PublishStatusEnum.RELEASE_WAIT.getCode())
                        || status.equals(PublishStatusEnum.INIT.getCode())) {
                    updateStatusSuspend(PublishStatusEnum.RELEASE_SUSPEND.getCode(), releaseDelpoyRecord, false);
                }
            }
            return ResultVo.Builder.SUCC().initSuccData("");
        } catch (Exception e) {
            logger.info("suspend exception , Exception = {}", e);
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("4000", "系统异常");
        }
    }

    @PostMapping("/suspendByRecord")
    @ResponseBody
    public ResultVo suspendByRecord(@RequestBody ReleaseDelpoyRecord releaseDelpoyRecord) {
        try {
            logger.info("suspend by record , param = {}", releaseDelpoyRecord.toString());
            if (releaseDelpoyRecord == null) {
                logger.info("suspend by record fail ,param = {}", releaseDelpoyRecord.toString());
                return ResultVo.Builder.FAIL().initErrCodeAndMsg("2000", "参数错误");
            }
            releaseDelpoyRecord = releaseDelpoyRecordService.getById(releaseDelpoyRecord.getId());
            if (releaseDelpoyRecord == null || releaseDelpoyRecord.getDeployStatus() == null) {
                logger.info("suspend by record fail ,param = {}", releaseDelpoyRecord.toString());
                return ResultVo.Builder.FAIL().initErrCodeAndMsg("2001", "没有找到对应的发布记录");
            }
            Integer status = releaseDelpoyRecord.getDeployStatus();
            if (status.equals(PublishStatusEnum.BUILD_SUCCESS.getCode()) || status.equals(PublishStatusEnum.RELEASE_RELEASE.getCode())
                    || status.equals(PublishStatusEnum.CODE_PUSH_SUCCESS.getCode()) || status.equals(PublishStatusEnum.RELEASE_WAIT.getCode())
                    || status.equals(PublishStatusEnum.INIT.getCode())) {
                updateStatusSuspend(PublishStatusEnum.RELEASE_SUSPEND.getCode(), releaseDelpoyRecord, false);
            }
            ReleaseDelpoyRecord releaseDelpoyRecord1 = new ReleaseDelpoyRecord();
            releaseDelpoyRecord1.setDelFlag(1);
            releaseDelpoyRecord1.setProjectTaskId(releaseDelpoyRecord.getProjectTaskId());
            releaseDelpoyRecord1.setDeployStatus(PublishStatusEnum.RELEASE_WAIT.getCode());
            List<ReleaseDelpoyRecord> releaseDelpoyRecordList = releaseDelpoyRecordService.listByCondition(releaseDelpoyRecord1);
            if (releaseDelpoyRecordList == null || releaseDelpoyRecordList.size() <= 0) {
                logger.info("suspend by record , no next publish , record = {}", releaseDelpoyRecord.getId());
                return ResultVo.Builder.SUCC().initSuccData("");
            }
            for (ReleaseDelpoyRecord delpoyRecord : releaseDelpoyRecordList) {
                if (delpoyRecord.getForwardRecordId() >= releaseDelpoyRecord.getId()) {
                    updateStatusSuspend(PublishStatusEnum.RELEASE_SUSPEND.getCode(), delpoyRecord, false);
                }
            }
            return ResultVo.Builder.SUCC().initSuccData("");
        } catch (Exception e) {
            logger.error("suspend by record exception , Exception = {}", e);
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("4000", "系统异常");
        }
    }

    @PostMapping("/releaseCallBack")
    @ResponseBody
    public ResultVo releaseCallBack(@RequestBody DeployCallBack deployCallBack) {
        try {
            logger.info("release call back param = {}", deployCallBack.toString());
            if (deployCallBack == null || StringUtils.isEmpty(deployCallBack.getRecordId()) || Integer.valueOf(deployCallBack.getRecordId()) <= 0) {
                logger.info("publish release callback fail:参数错误 , param = {}", deployCallBack.toString());
                ResultVo.Builder.FAIL().initErrCodeAndMsg("2000", "参数错误");
            }
            Integer status = Integer.valueOf(deployCallBack.getDeployStatus());
            String tag = deployCallBack.getTag();
            ReleaseDelpoyRecord releaseDelpoyRecord = releaseDelpoyRecordService.getById(Integer.valueOf(deployCallBack.getRecordId()));
            ProjectTask projectTask = new ProjectTask();
            Boolean planPublishFlag = false;
            boolean isRollback = Integer.valueOf(1).equals(releaseDelpoyRecord.getRollbackFlag());
            List<DeployPlanRecord> planRecordList = new ArrayList<>();
            if (!StringUtils.isEmpty(deployCallBack.getBatchId().trim())) {
                planPublishFlag = false;
                ProjectTaskBatchRecord projectTaskBatchRecord = projectTaskBatchRecordService.getById(Integer.valueOf(deployCallBack.getBatchId()));
                if (projectTaskBatchRecord == null) {
                    logger.error("publish release callback fail:没有对应的批次发布记录 , param = {}", deployCallBack.toString());
                    ResultVo.Builder.FAIL().initErrCodeAndMsg("2001", "没有对应的批次发布记录");
                }
                projectTaskBatchRecord.setDeployStatus(status);
                projectTaskBatchRecordService.update(projectTaskBatchRecord);
                ProjectTaskBatch projectTaskBatch = new ProjectTaskBatch();
                projectTaskBatch.setId(projectTaskBatchRecord.getProjectTaskBatchId());
                projectTaskBatch.setDeployStatus(status);
                projectTaskBatchService.updateById(projectTaskBatch);
                if (status.equals(PublishStatusEnum.RELEASE_SUCCESS.getCode())) {
                    ProjectTaskBatchRecord projectTaskBatchRecord1 = new ProjectTaskBatchRecord();
                    projectTaskBatchRecord1.setForwardBatchId(projectTaskBatchRecord.getId());
                    if (projectTaskBatchRecordService.listByCondition(projectTaskBatchRecord1) == null) {
                        releaseDelpoyRecord.setDeployStatus(status);
                        if (!StringUtils.isEmpty(tag)) {
                            releaseDelpoyRecord.setAppRtag(tag);
                        }
                        releaseDelpoyRecordService.updateById(releaseDelpoyRecord);
                        projectTask.setReleaseTaskStatus(status);
                        projectTask.setId(releaseDelpoyRecord.getProjectTaskId());
                        projectTaskService.updateById(projectTask);
                    }
                }
            }
            if (!StringUtils.isEmpty(deployCallBack.getPlanId().trim())) {
                planPublishFlag = true;
                DeployPlanRecord deployPlanRecord = deployPlanRecordService.getById(Integer.valueOf(deployCallBack.getPlanId()));
                if (deployPlanRecord == null) {
                    logger.error("publish release callback fail:没有对应的发布计划记录 , param = {}", deployCallBack.toString());
                    return ResultVo.Builder.FAIL().initErrCodeAndMsg("2002", "没有对应的发布计划记录");
                }
                DeployPlan deployPlan = deployPlanService.getById(deployPlanRecord.getPlanId());
                if (status.equals(PublishStatusEnum.BUILD_FAIL.getCode()) || status.equals(PublishStatusEnum.CODE_PUSH_FAIL.getCode()) || status.equals(PublishStatusEnum.RELEASE_FAIL.getCode())) {
                    planRecordList = deployPlanRecordService.getNextPlanRecords(deployPlanRecord);
                    deployPlanRecord.setDeployStatus(PublishStatusEnum.RELEASE_FAIL.getCode());
                    if (status.equals(PublishStatusEnum.BUILD_FAIL.getCode()) || status.equals(PublishStatusEnum.CODE_PUSH_FAIL.getCode())) {
                        deployPlanRecord.setSubDeployStatus(status);
                        deployPlan.setSubDeployStatus(status);
                    }
                    if (!isRollback && !StringUtils.isEmpty(tag)) {
                        deployPlanRecord.setAppRtag(tag);
                        deployPlan.setAppRtag(tag);
                    }
                    deployPlan.setDeployStatus(PublishStatusEnum.RELEASE_FAIL.getCode());
                    PLAN_STATUS_MAP.put(deployPlan.getId(), true);
                } else {
                    deployPlanRecord.setDeployStatus(status);
                    if (!isRollback && !StringUtils.isEmpty(tag)) {
                        deployPlanRecord.setAppRtag(tag);
                        deployPlan.setAppRtag(tag);
                    }
                }
                deployPlanRecordService.updateById(deployPlanRecord);
                if (PublishStatusEnum.RELEASE_SUCCESS.getCode().equals(status)) {
                    PlanMachineMapping planMachineMapping = new PlanMachineMapping();
                    planMachineMapping.setPlanId(deployPlan.getId());
                    planMachineMapping.setDelFlag(1);
                    List<PlanMachineMapping> planMachineMappingList = planMachineMappingService.listByCondition(planMachineMapping);
                    if (!CollectionUtils.isEmpty(planMachineMappingList)) {
                        int successCount = 0;
                        for (PlanMachineMapping machineMapping : planMachineMappingList) {
                            if (PublishStatusEnum.RELEASE_SUCCESS.getCode().equals(machineMapping.getDeployStatus())) {
                                successCount++;
                            }
                        }

                        if (successCount == planMachineMappingList.size()) {
                            deployPlan.setDeployStatus(status);
                        } else {
                            deployPlan.setDeployStatus(PublishStatusEnum.RELEASE_PART_SUCCESS.getCode());
                        }
                    }

                    List<DeployPlanRecord> deployPlanRecordList = deployPlanRecordService.getByPlanId(deployPlanRecord.getPlanId());
                    if (!CollectionUtils.isEmpty(deployPlanRecordList)) {
                        int successCount = 0;
                        for (DeployPlanRecord record : deployPlanRecordList) {
                            if (PublishStatusEnum.RELEASE_SUCCESS.getCode().equals(record.getDeployStatus()) ||
                                    PublishStatusEnum.RELEASE_FAIL.getCode().equals(record.getDeployStatus()) ||
                                    PublishStatusEnum.RELEASE_SUSPEND.getCode().equals(record.getDeployStatus())) {
                                successCount++;
                            }
                        }

                        if (successCount == deployPlanRecordList.size()) {
                            PLAN_STATUS_MAP.put(deployPlan.getId(), true);
                        }
                    }

                    logger.info("planMachineMappingList: {} \r\n deployPlanRecordList: {} \r\n PLAN_STATUS_MAP: {}", planMachineMappingList, deployPlanRecordList, PLAN_STATUS_MAP);
                } else {
                    deployPlan.setDeployStatus(status);
                }
                deployPlanService.updateById(deployPlan);
                if (status.equals(PublishStatusEnum.RELEASE_SUCCESS.getCode())) {
                    PlanMachineMapping planMachineMapping = new PlanMachineMapping();
                    planMachineMapping.setProjectTaskId(deployPlanRecord.getProjectTaskId());
                    planMachineMapping.setDelFlag(1);
                    planMachineMapping.setProjectId(releaseDelpoyRecord.getProjectId());
                    planMachineMapping.setDeployStatus(status);
                    planMachineMapping.setRollbackFlag(deployPlanRecord.getRollbackFlag());
                    List<PlanMachineMapping> planMachineMappingList = planMachineMappingService.listByCondition(planMachineMapping);
                    int machineTotal = 0;
                    ResultVo resultVo = getServerCount(deployPlanRecord.getAppCode(), deployPlanRecord.getAppGroup());
                    if (resultVo.getCode().equals("0") && resultVo.getRet().equals("success")) {
                        machineTotal = (Integer) resultVo.getData();
                    }
                    if (planMachineMappingList.size() == machineTotal) {
                        projectTask.setReleaseTaskStatus(status);
                        projectTask.setId(releaseDelpoyRecord.getProjectTaskId());
                        projectTaskService.updateById(projectTask);
                    }
                    DeployPlanRecord planRecord = new DeployPlanRecord();
                    planRecord.setDeployRecordId(deployPlanRecord.getDeployRecordId());
                    planRecord.setDelFlag(1);
                    List<DeployPlanRecord> deployPlanRecordList = deployPlanRecordService.listByCondition(planRecord);
                    if (!CollectionUtils.isEmpty(deployPlanRecordList)) {
                        int successTotal = 0;
                        for (DeployPlanRecord record : deployPlanRecordList) {
                            if (record.getDeployStatus().equals(PublishStatusEnum.RELEASE_SUCCESS.getCode())) {
                                successTotal++;
                            }
                        }
                        if (successTotal == deployPlanRecordList.size()) {
                            releaseDelpoyRecord.setDeployStatus(status);
                            RELEASE_STATUS_MAP.put(releaseDelpoyRecord.getId(), true);
                        } else {
                            releaseDelpoyRecord.setDeployStatus(PublishStatusEnum.RELEASE_PART_SUCCESS.getCode());
                        }
                    } else {
                        releaseDelpoyRecord.setDeployStatus(status);
                        RELEASE_STATUS_MAP.put(releaseDelpoyRecord.getId(), true);
                    }
                    releaseDelpoyRecordService.updateById(releaseDelpoyRecord);
                }
            }
            if (status.equals(PublishStatusEnum.CODE_PUSH_FAIL.getCode()) || status.equals(PublishStatusEnum.BUILD_FAIL.getCode())
                    || status.equals(PublishStatusEnum.RELEASE_FAIL.getCode())) {
                List<ReleaseDelpoyRecord> releaseDelpoyRecordList = null;
                updateStatusSuspend(status, releaseDelpoyRecord, planPublishFlag);
                if (CollectionUtils.isEmpty(planRecordList)) {
                    ReleaseDelpoyRecord releaseDelpoyRecord1 = new ReleaseDelpoyRecord();
                    releaseDelpoyRecord1.setDeployStatus(PublishStatusEnum.RELEASE_WAIT.getCode());
                    releaseDelpoyRecord1.setProjectId(releaseDelpoyRecord.getProjectId());
                    releaseDelpoyRecord1.setDelFlag(1);
                    releaseDelpoyRecordList = releaseDelpoyRecordService.listByCondition(releaseDelpoyRecord1);
                } else {
                    releaseDelpoyRecordList = planRecordList.stream().map(deployPlanRecord -> {
                        ReleaseDelpoyRecord delpoyRecord = new ReleaseDelpoyRecord();
                        delpoyRecord.setId(deployPlanRecord.getDeployRecordId());
                        delpoyRecord.setProjectTaskId(deployPlanRecord.getProjectTaskId());
                        PLAN_STATUS_MAP.put(deployPlanRecord.getPlanId(), true);
                        return delpoyRecord;
                    }).collect(Collectors.toList());
                }
                if (releaseDelpoyRecordList == null || releaseDelpoyRecordList.size() <= 0) {
                    return ResultVo.Builder.SUCC();
                }
                for (ReleaseDelpoyRecord releaseDelpoyRecord2 : releaseDelpoyRecordList) {
                    updateStatusSuspend(PublishStatusEnum.RELEASE_SUSPEND.getCode(), releaseDelpoyRecord2, planPublishFlag);
                }
            } else if (!status.equals(PublishStatusEnum.RELEASE_SUCCESS.getCode())) {
                if (!isRollback && !StringUtils.isEmpty(tag)) {
                    releaseDelpoyRecord.setAppRtag(tag);
                }
                releaseDelpoyRecord.setDeployStatus(status);
                releaseDelpoyRecordService.updateById(releaseDelpoyRecord);
                projectTask.setReleaseTaskStatus(status);
                projectTask.setId(releaseDelpoyRecord.getProjectTaskId());
                projectTaskService.updateById(projectTask);
            }
            return ResultVo.Builder.SUCC();
        } catch (Exception e) {
            logger.error("release callBack exception , e = {}", e);
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("4000", "系统异常");
        }
    }

    /**
     * 根据机器状态决定当前发布记录状态
     *
     * @param projectId
     * @param projectTaskId
     * @return
     */
    private Integer getReleaseRecordStatus(Integer projectId, Integer projectTaskId) {
        PlanMachineMapping condition = new PlanMachineMapping();
        condition.setProjectId(projectId);
        condition.setProjectTaskId(projectTaskId);

        List<PlanMachineMapping> planMachineMappingList = planMachineMappingService.listByCondition(condition);
        if (!CollectionUtils.isEmpty(planMachineMappingList)) {
            int failureCount = 0;
            int successCount = 0;
            int initCount = 0;
            int waitCount = 0;
            for (PlanMachineMapping planMachineMapping : planMachineMappingList) {
                Integer status = planMachineMapping.getDeployStatus();
                if (PublishStatusEnum.RELEASE_FAIL.getCode().equals(status)) {
                    failureCount++;
                    break;
                } else if (PublishStatusEnum.RELEASE_SUCCESS.getCode().equals(status)) {
                    successCount++;
                } else if (PublishStatusEnum.INIT.getCode().equals(status)) {
                    initCount++;
                } else if (PublishStatusEnum.RELEASE_WAIT.getCode().equals(status)) {
                    waitCount++;
                }
            }

            int machineCount = planMachineMappingList.size();

            if (failureCount > 0) {
                return PublishStatusEnum.RELEASE_FAIL.getCode();
            }

            if (successCount == machineCount) {
                return PublishStatusEnum.RELEASE_SUCCESS.getCode();
            }

            if (successCount > 0) {
                return PublishStatusEnum.RELEASE_PART_SUCCESS.getCode();
            }

            if (initCount == machineCount) {
                return PublishStatusEnum.INIT.getCode();
            }

            if (waitCount == machineCount) {
                return PublishStatusEnum.RELEASE_WAIT.getCode();
            }

            return PublishStatusEnum.RELEASE_RELEASE.getCode();
        }
        return 0;
    }

    @PostMapping("/batchPublish")
    @ResponseBody
    public ResultVo batchPublish(@RequestBody DeployCallBack deployCallBack) {
        try {
            logger.info("batch publish param = {}", deployCallBack.toString());
            if (deployCallBack == null || deployCallBack.getRecordId() == null || Integer.valueOf(deployCallBack.getRecordId()) <= 0) {
                logger.info("batch publish fail:参数错误 , param = {}", deployCallBack);
                return ResultVo.Builder.FAIL().initErrCodeAndMsg("2000", "参数错误");
            }
            String appCode = "";
            ReleaseDeployParam releaseDeployParam = new ReleaseDeployParam();
            ReleaseDelpoyRecord releaseDelpoyRecord = new ReleaseDelpoyRecord();
            ProjectTaskBatchRecord projectTaskBatchRecord = null;
            DeployPlanRecord deployPlanRecord = null;
            AppInfoDto appInfo = null;
            String outputToLogFilePwd = "";
            if (!StringUtils.isEmpty(deployCallBack.getBatchId().trim())) {
                releaseDelpoyRecord = releaseDelpoyRecordService.getById(Integer.valueOf(deployCallBack.getRecordId()));
                if (releaseDelpoyRecord == null) {
                    logger.info("未查询到发布记录 ，record = {}", Integer.valueOf(deployCallBack.getRecordId()));
                    return ResultVo.Builder.FAIL().initErrCodeAndMsg("2001", "无对应的appCode信息");
                }
                outputToLogFilePwd = releaseDelpoyRecord.getLogPath();
                projectTaskBatchRecord = projectTaskBatchRecordService.getByBatchId(Integer.valueOf(deployCallBack.getBatchId()));
                if (projectTaskBatchRecord == null) {
                    logger.info("batch publish no next batch , param = {}", deployCallBack);
                    releaseDelpoyRecord.setDeployStatus(PublishStatusEnum.RELEASE_SUCCESS.getCode());
                    releaseDelpoyRecordService.updateById(releaseDelpoyRecord);
                    RELEASE_STATUS_MAP.put(releaseDelpoyRecord.getId(), true);
                    ProjectTask projectTask = new ProjectTask();
                    projectTask.setId(releaseDelpoyRecord.getProjectTaskId());
                    projectTask.setReleaseTaskStatus(PublishStatusEnum.RELEASE_SUCCESS.getCode());
                    projectTaskService.updateById(projectTask);
                    Integer projectId = releaseDelpoyRecord.getProjectId();
                    releaseDelpoyRecord = releaseDelpoyRecordService.getByRecordId(Integer.valueOf(deployCallBack.getRecordId()));
                    if (releaseDelpoyRecord == null) {
                        // 发布完成后，释放所有被该项目使用的机器
                        this.releaseServerByProjectId(projectId);
                        return ResultVo.Builder.SUCC().initSuccData("没有下一发布的发布任务");
                    } else {
                        Integer status = releaseDelpoyRecord.getDeployStatus();
                        if (!status.equals(PublishStatusEnum.RELEASE_WAIT.getCode())) {
                            logger.info("next record status not wait , param = {}", releaseDelpoyRecord.getId());
                            return ResultVo.Builder.SUCC().initSuccData("没有下一发布的发布任务");
                        }
                        ResultVo result = getAppInfo(appCode, "prod");
                        if (result.getCode().equals("0") && result.getRet().equals("success")) {
                            appInfo = (AppInfoDto) JSON.parseObject(String.valueOf(result.getData()), AppInfoDto.class);
                        } else {
                            logger.info("未查询到对应的appInfo信息，appCode = {}", appCode);
                            return ResultVo.Builder.FAIL().initErrCodeAndMsg("2001", "无对应的appCode信息");
                        }
                        releaseDelpoyRecord.setDeployStatus(PublishStatusEnum.RELEASE_RELEASE.getCode());
                        releaseDelpoyRecordService.updateById(releaseDelpoyRecord);
                        projectTask.setId(releaseDelpoyRecord.getProjectTaskId());
                        projectTask.setReleaseTaskStatus(PublishStatusEnum.RELEASE_RELEASE.getCode());
                        projectTaskService.updateById(projectTask);
                        projectTaskBatchRecord = projectTaskBatchRecordService.getFirstBatchByRecord(releaseDelpoyRecord.getId());
                        outputToLogFilePwd = releaseDelpoyRecord.getLogPath();
                    }
                }
                if (projectTaskBatchRecord != null) {
                    projectTaskBatchRecord.setDeployStatus(PublishStatusEnum.RELEASE_RELEASE.getCode());
                    projectTaskBatchRecordService.update(projectTaskBatchRecord);
                    ProjectTaskBatch projectTaskBatch = new ProjectTaskBatch();
                    projectTaskBatch.setId(projectTaskBatchRecord.getProjectTaskBatchId());
                    projectTaskBatch.setDeployStatus(PublishStatusEnum.RELEASE_RELEASE.getCode());
                    projectTaskBatchService.updateById(projectTaskBatch);
                }
                if (appInfo == null) {
                    ResultVo result = getAppInfo(releaseDelpoyRecord.getAppCode(), "prod");
                    if (result.getCode().equals("0") && result.getRet().equals("success")) {
                        appInfo = (AppInfoDto) JSON.parseObject(String.valueOf(result.getData()), AppInfoDto.class);
                    } else {
                        logger.info("未查询到对应的appInfo信息，appCode = {}", appCode);
                        return ResultVo.Builder.FAIL().initErrCodeAndMsg("2001", "无对应的appCode信息");
                    }
                }
            } else if (!StringUtils.isEmpty(deployCallBack.getPlanId().trim())) {
                deployPlanRecord = deployPlanRecordService.getById(Integer.valueOf(deployCallBack.getPlanId()));
                if (deployPlanRecord == null) {
                    logger.info("batch publish , 未查询到发布计划记录 ，plan = {}", Integer.valueOf(deployCallBack.getPlanId()));
                    return ResultVo.Builder.FAIL().initErrCodeAndMsg("2001", "无对应的发布计划记录");
                }
                /*PlanMachineMapping planMachineMapping = new PlanMachineMapping();
                planMachineMapping.setDelFlag(1);
                planMachineMapping.setDeployStatus(PublishStatusEnum.RELEASE_SUCCESS.getCode());
                planMachineMapping.setProjectTaskId(deployPlanRecord.getProjectTaskId());
                if (planMachineMappingService.checkPublishComplete(planMachineMapping)) {
                    Integer status = this.getReleaseRecordStatus(deployPlanRecord.getProjectId(), deployPlanRecord.getProjectTaskId());
                    if (!PublishStatusEnum.RELEASE_SUCCESS.getCode().equals(status)) {
                        releaseDelpoyRecord.setDeployStatus(status);
                        releaseDelpoyRecord.setId(deployPlanRecord.getDeployRecordId());
                        releaseDelpoyRecordService.updateById(releaseDelpoyRecord);
                    }
                    ProjectTask projectTask = new ProjectTask();
                    projectTask.setId(deployPlanRecord.getProjectTaskId());
                    projectTask.setReleaseTaskStatus(PublishStatusEnum.RELEASE_SUCCESS.getCode());
                    projectTaskService.updateById(projectTask);
                } else {
                    planMachineMapping.setAppTag(deployCallBack.getTag());
                    planMachineMapping.setAppGroup(deployPlanRecord.getAppGroup());
                    int count = planMachineMappingService.pageTotal(planMachineMapping);
                    int total = 0;
                    ResultVo resultVo = getServerCount(deployPlanRecord.getAppCode(), deployPlanRecord.getAppGroup());
                    if (resultVo.getCode().equals("0") && resultVo.getRet().equals("success")) {
                        total = (Integer) resultVo.getData();
                    } else {
                        logger.error("batch publish get machine count 0 ,param = {}", deployPlanRecord.getAppCode(), deployPlanRecord.getAppGroup());
                    }
                    if (count == total) {
                        releaseDelpoyRecord.setDeployStatus(PublishStatusEnum.RELEASE_SUCCESS.getCode());
                        releaseDelpoyRecord.setId(deployPlanRecord.getDeployRecordId());
                        releaseDelpoyRecordService.updateById(releaseDelpoyRecord);
                    } else {
                        Integer status1 = this.getReleaseRecordStatus(releaseDelpoyRecord.getProjectId(), releaseDelpoyRecord.getProjectTaskId());
                        releaseDelpoyRecord.setDeployStatus(status1);
                    }
                }*/
                Integer projectId = deployPlanRecord.getProjectId();
                deployPlanRecord = deployPlanRecordService.getNextPlanRecord(deployPlanRecord);
                if (deployPlanRecord == null) {
                    logger.info("batch publish no next plan , param = {}", Integer.valueOf(deployCallBack.getPlanId()));
                    // 发布完成后，释放所有被该项目使用的机器
                    this.releaseServerByProjectId(projectId);
                    return ResultVo.Builder.SUCC();
                } else {
                    releaseDelpoyRecord = releaseDelpoyRecordService.getById(deployPlanRecord.getDeployRecordId());
                    outputToLogFilePwd = releaseDelpoyRecord.getLogPath();
                    ResultVo result = getAppInfo(releaseDelpoyRecord.getAppCode(), "prod");
                    if (result.getCode().equals("0") && result.getRet().equals("success")) {
                        appInfo = (AppInfoDto) JSON.parseObject(String.valueOf(result.getData()), AppInfoDto.class);
                    } else {
                        logger.info("未查询到对应的appInfo信息，appCode = {}", appCode);
                        return ResultVo.Builder.FAIL().initErrCodeAndMsg("2001", "无对应的appCode信息");
                    }
                }
            } else {
                logger.info("batch publish fail:参数错误 , param = {}", deployCallBack.toString());
                return ResultVo.Builder.FAIL().initErrCodeAndMsg("2000", "参数错误");
            }
            releaseDeployParam = transReleaseDeployParam(releaseDelpoyRecord, projectTaskBatchRecord, appInfo, releaseDelpoyRecord.getPublishUser(), deployPlanRecord);
            final ReleaseDeployParam finalReleaseDeployParam = releaseDeployParam;
            final String finalOutputToLogFilePwd = outputToLogFilePwd;
            Callable releaseCallable = new Callable() {
                @Override
                public Object call() throws Exception {
                    publishService.releasePublish(finalReleaseDeployParam, finalOutputToLogFilePwd);
                    return null;
                }
            };
            EXECUTE_SHELL.submit(releaseCallable);
            return ResultVo.Builder.SUCC();
        } catch (Exception e) {
            logger.error("batch publish exception , Exception = {}", e);
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("4000", "系统异常");
        }
    }

    @PostMapping("/ipCallBack")
    @ResponseBody
    public ResultVo ipCallBack(@RequestBody DeployCallBack deployCallBack) {
        try {
            logger.info("ip callback , param = {}", deployCallBack.toString());
            if (deployCallBack == null || StringUtils.isEmpty(deployCallBack.getRecordId().trim())
                    || Integer.valueOf(deployCallBack.getRecordId()) <= 0 || StringUtils.isEmpty(deployCallBack.getIp().trim())
                    || StringUtils.isEmpty(deployCallBack.getDeployStatus().trim()) || StringUtils.isEmpty(deployCallBack.getPlanId().trim())
                    || Integer.valueOf(deployCallBack.getPlanId()) <= 0) {
                logger.info("ip callback fail , 参数错误 , param = {}", deployCallBack.toString());
                return ResultVo.Builder.FAIL().initErrCodeAndMsg("2000", "参数错误");
            }
            DeployPlanRecord deployPlanRecord = deployPlanRecordService.getById(Integer.valueOf(deployCallBack.getPlanId()));
            if (deployPlanRecord == null) {
                logger.error("ip call back no plan record data , param = {}", deployCallBack.getRecordId());
                return ResultVo.Builder.FAIL().initErrCodeAndMsg("2002", "没有计划发布记录");
            }
            String appCode = deployPlanRecord.getAppCode();
            PlanMachineMapping planMachineMapping = new PlanMachineMapping();
            planMachineMapping.setPlanId(deployPlanRecord.getPlanId());
            planMachineMapping.setDelFlag(1);
            planMachineMapping.setDeployStatus(Integer.valueOf(deployCallBack.getDeployStatus()));
            if (!Integer.valueOf(1).equals(deployPlanRecord.getRollbackFlag())) {
                planMachineMapping.setAppTag(deployCallBack.getTag());
            }
            planMachineMapping.setServiceIps(deployCallBack.getIp());
            int result = planMachineMappingService.updateByIp(planMachineMapping);
            if (result <= 0) {
                logger.info("ip callback no update data , param = {}", planMachineMapping.toString());
                return ResultVo.Builder.SUCC();
            }
            modifyMachineStatus(appCode, deployPlanRecord.getAppGroup(), deployCallBack.getIp(), deployCallBack.getTag(), Integer.valueOf(deployCallBack.getDeployStatus()));
            if (Integer.valueOf(deployCallBack.getDeployStatus()).equals(PublishStatusEnum.RELEASE_SUCCESS.getCode())
                    && !deployPlanRecord.getDeployStatus().equals(PublishStatusEnum.RELEASE_PART_SUCCESS.getCode())) {
                deployPlanRecord.setDeployStatus(PublishStatusEnum.RELEASE_PART_SUCCESS.getCode());
                deployPlanRecordService.updateById(deployPlanRecord);
                DeployPlan deployPlan = new DeployPlan();
                deployPlan.setId(deployPlanRecord.getPlanId());
                deployPlan.setDeployStatus(PublishStatusEnum.RELEASE_PART_SUCCESS.getCode());
                deployPlanService.updateById(deployPlan);
                ReleaseDelpoyRecord releaseDelpoyRecord = new ReleaseDelpoyRecord();
                releaseDelpoyRecord.setId(deployPlanRecord.getDeployRecordId());
                releaseDelpoyRecord.setDeployStatus(PublishStatusEnum.RELEASE_PART_SUCCESS.getCode());
                releaseDelpoyRecordService.updateById(releaseDelpoyRecord);
                ProjectTask projectTask = new ProjectTask();
                projectTask.setId(deployPlanRecord.getProjectTaskId());
                projectTask.setReleaseTaskStatus(PublishStatusEnum.RELEASE_PART_SUCCESS.getCode());
                projectTaskService.updateById(projectTask);
            }
            return ResultVo.Builder.SUCC();
        } catch (Exception e) {
            logger.error("ip callback exception , Exception = {}", e);
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("4000", "系统异常");
        }
    }

    @PostMapping("/releaseLog")
    @ResponseBody
    public ResultVo releaseLog(@RequestBody ProjectTask projectTask) {
        try {
            if (projectTask == null || projectTask.getId() <= 0) {
                logger.info("release log fail , param = {}", projectTask);
                return ResultVo.Builder.FAIL().initErrCodeAndMsg("2000", "参数错误");
            }
            LogVo logVo = new LogVo();
            ReleaseDelpoyRecord releaseDelpoyRecord = new ReleaseDelpoyRecord();
            Integer id = RELEASE_RECORD_MAP.get(projectTask.getId());
            if (id == null || id <= 0) {
                releaseDelpoyRecord = releaseDelpoyRecordService.getByLastId(projectTask.getId());
                if (releaseDelpoyRecord == null) {
                    logVo.setLogContent("");
                    logVo.setPublishCompleteFlag(true);
                    logger.info("release log fail : 没有对应的发布记录, projectTaskId = {}", projectTask.getId());
                    return ResultVo.Builder.SUCC().initSuccData(logVo);
                }
                if (releaseDelpoyRecord.getId() == null) {
                    logger.info("release log fail : 没有对应的发布记录, projectTaskId = {}", projectTask.getId());
                    return ResultVo.Builder.FAIL().initErrCodeAndMsg("2001", "没有对应的发布记录");
                }
                id = releaseDelpoyRecord.getId();
                if (id == null || id <= 0) {
                    logVo.setLogContent("");
                    logVo.setPublishCompleteFlag(true);
                    return ResultVo.Builder.SUCC().initSuccData(logVo);
                }
                RELEASE_RECORD_MAP.put(projectTask.getId(), id);
            }
            String fileAddr = RELEASE_ADDRESS_MAP.get(String.valueOf(id));
            if (StringUtils.isEmpty(fileAddr) || fileAddr.trim().length() <= 0) {
                if (releaseDelpoyRecord.getLogPath() == null) {
                    releaseDelpoyRecord = releaseDelpoyRecordService.getById(id);
                    if (releaseDelpoyRecord == null) {
                        logger.info("beta log fail : 没有对应的发布记录, id = {}", id);
                        return ResultVo.Builder.FAIL().initErrCodeAndMsg("2001", "没有对应的发布记录");
                    }
                }
                fileAddr = releaseDelpoyRecord.getLogPath();
                if (StringUtils.isEmpty(fileAddr) || fileAddr.trim().length() <= 0) {
                    logVo.setLogContent("");
                    logVo.setPublishCompleteFlag(true);
                    return ResultVo.Builder.SUCC().initSuccData(logVo);
                }
                RELEASE_ADDRESS_MAP.put(String.valueOf(id), fileAddr);
            }
            String log = FileUtil.initLog(fileAddr);
            if (RELEASE_STATUS_MAP.get(id) == null) {
                releaseDelpoyRecord = releaseDelpoyRecordService.getById(id);
                if (releaseDelpoyRecord == null) {
                    logger.info("release log fail : 没有对应的发布记录, id = {}", id);
                    return ResultVo.Builder.FAIL().initErrCodeAndMsg("2001", "没有对应的发布记录");
                }
                Integer status = releaseDelpoyRecord.getDeployStatus();
                if (status.equals(PublishStatusEnum.RELEASE_SUCCESS.getCode()) || status.equals(PublishStatusEnum.RELEASE_FAIL.getCode())
                        || status.equals(PublishStatusEnum.RELEASE_SUSPEND.getCode())) {
                    RELEASE_STATUS_MAP.put(id, true);
                } else {
                    RELEASE_STATUS_MAP.put(id, false);
                }
            }
            logVo.setPublishCompleteFlag(RELEASE_STATUS_MAP.get(id));
            logVo.setLogContent(log);
            return ResultVo.Builder.SUCC().initSuccData(logVo);
        } catch (Exception e) {
            logger.error("release log exception , e = {}", e);
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("4000", "系统异常");
        }
    }

    @PostMapping("/releaseAppLog")
    @ResponseBody
    public ResultVo releaseAppLog(@RequestBody ReleaseDelpoyRecord releaseDelpoyRecord) {
        try {
            LogVo logVo = new LogVo();
            if (releaseDelpoyRecord == null || releaseDelpoyRecord.getId() == null || releaseDelpoyRecord.getId() <= 0) {
                logger.info("release log by record fail , param = {}", releaseDelpoyRecord);
                return ResultVo.Builder.FAIL().initErrCodeAndMsg("2000", "参数错误");
            }
            releaseDelpoyRecord = releaseDelpoyRecordService.getById(releaseDelpoyRecord.getId());
            if (releaseDelpoyRecord == null || releaseDelpoyRecord.getLogPath() == null
                    || releaseDelpoyRecord.getLogPath().trim().length() <= 0 || releaseDelpoyRecord.getDeployStatus() == null) {
                logger.info("release log by appCode fail : 没有对应的发布记录, id = {}", releaseDelpoyRecord.getId());
                return ResultVo.Builder.FAIL().initErrCodeAndMsg("2001", "没有对应的发布记录");
            }
            String fileAddr = RELEASE_ADDRESS_MAP.get(String.valueOf(releaseDelpoyRecord.getId()));
            if (StringUtils.isEmpty(fileAddr) || fileAddr.trim().length() <= 0) {
                fileAddr = releaseDelpoyRecord.getLogPath();
                if (StringUtils.isEmpty(fileAddr) || fileAddr.trim().length() <= 0) {
                    logVo.setLogContent("");
                    logVo.setPublishCompleteFlag(true);
                    return ResultVo.Builder.SUCC().initSuccData(logVo);
                }
                RELEASE_ADDRESS_MAP.put(String.valueOf(releaseDelpoyRecord.getId()), fileAddr);
            }
            Integer status = releaseDelpoyRecord.getDeployStatus();
            if (status.equals(PublishStatusEnum.RELEASE_SUSPEND.getCode()) || status.equals(PublishStatusEnum.RELEASE_FAIL.getCode())
                    || status.equals(PublishStatusEnum.RELEASE_SUCCESS.getCode()) || status.equals(PublishStatusEnum.BUILD_FAIL.getCode())
                    || status.equals(PublishStatusEnum.CODE_PUSH_FAIL.getCode())) {
                RELEASE_STATUS_MAP.put(releaseDelpoyRecord.getId(), true);
            } else {
                RELEASE_STATUS_MAP.put(releaseDelpoyRecord.getId(), false);
            }
            String log = FileUtil.initLog(fileAddr);
            logVo.setPublishCompleteFlag(RELEASE_STATUS_MAP.get(releaseDelpoyRecord.getId()));
            logVo.setLogContent(log);
            return ResultVo.Builder.SUCC().initSuccData(logVo);
        } catch (Exception e) {
            logger.error("release log by appCode exception , Exception = {}", e);
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("4000", "系统异常");
        }
    }

    @PostMapping("/releasePlanLog")
    @ResponseBody
    public ResultVo releasePlanLog(@RequestBody DeployPlanVo deployPlanVo) {
        try {
            logger.info("release plan log , param = {}", deployPlanVo.toString());
            if (deployPlanVo == null || deployPlanVo.getProjectTaskId() == null || deployPlanVo.getProjectTaskId() <= 0
                    || deployPlanVo.getId() == null || deployPlanVo.getId() <= 0) {
                logger.info("release plan log fail , 参数错误 , param = {}", deployPlanVo.toString());
                return ResultVo.Builder.FAIL().initErrCodeAndMsg("2000", "参数错误");
            }
            LogVo logVo = new LogVo();
            DeployPlanRecord deployPlanRecord = null;
            Integer recordId = PLAN_RECORD_MAP.get(deployPlanVo.getId());
            if (recordId == null || recordId <= 0) {
                deployPlanRecord = deployPlanRecordService.getLastByPlanId(deployPlanVo.getId());
                if (deployPlanRecord == null) {
                    logger.info("release plan log fail : 没有对应的发布计划记录, param = {}", deployPlanVo.getId());
                    logVo.setPublishCompleteFlag(true);
                    logVo.setLogContent("由于未点击该发布或重新生成了计划，暂时查询不到对应的发布日志~");
                    ResultVo resultVo = ResultVo.Builder.SUCC().initSuccData(logVo);
                    resultVo.setMessage("没有对应的发布计划记录");
                    return resultVo;
                }
                recordId = deployPlanRecord.getDeployRecordId();
                PLAN_RECORD_MAP.put(deployPlanVo.getId(), recordId);
                PLAN_PLANRECORD_MAP.put(deployPlanVo.getId(), deployPlanRecord.getId());
            }
            ReleaseDelpoyRecord releaseDelpoyRecord = new ReleaseDelpoyRecord();
            String fileAddr = RELEASE_ADDRESS_MAP.get(String.valueOf(recordId));
            if (StringUtils.isEmpty(fileAddr) || fileAddr.trim().length() <= 0) {
                releaseDelpoyRecord = releaseDelpoyRecordService.getById(recordId);
                if (releaseDelpoyRecord == null) {
                    logger.info("release plan log fail : 没有对应的发布记录, id = {}", recordId);
                    logVo.setPublishCompleteFlag(true);
                    logVo.setLogContent("由于未点击该发布或重新生成了计划，暂时查询不到对应的发布日志~");
                    ResultVo resultVo = ResultVo.Builder.SUCC().initSuccData(logVo);
                    resultVo.setMessage("没有对应的发布记录");
                    return resultVo;
                }
                fileAddr = releaseDelpoyRecord.getLogPath();
                if (StringUtils.isEmpty(fileAddr) || fileAddr.trim().length() <= 0) {
                    logVo.setLogContent("");
                    logVo.setPublishCompleteFlag(false);
                    return ResultVo.Builder.SUCC().initSuccData(logVo);
                }
                RELEASE_ADDRESS_MAP.put(String.valueOf(recordId), fileAddr);
            }
            String log = FileUtil.initLog(fileAddr);
            if (PLAN_STATUS_MAP.get(deployPlanVo.getId()) == null) {
                DeployPlan deployPlan = deployPlanService.getById(deployPlanVo.getId());
                if (deployPlan == null) {
                    logger.info("release plan log fail : 没有对应的发布计划, id = {}", recordId);
                    logVo.setPublishCompleteFlag(true);
                    logVo.setLogContent("由于未点击该发布或重新生成了计划，暂时查询不到对应的发布日志~");
                    ResultVo resultVo = ResultVo.Builder.SUCC().initSuccData(logVo);
                    resultVo.setMessage("没有对应的发布计划");
                    return resultVo;
                }
                Integer status = deployPlan.getDeployStatus();
                if (status.equals(PublishStatusEnum.RELEASE_SUCCESS.getCode()) || status.equals(PublishStatusEnum.RELEASE_FAIL.getCode())
                        || status.equals(PublishStatusEnum.RELEASE_SUSPEND.getCode())) {
                    PLAN_STATUS_MAP.put(deployPlan.getId(), true);
                } else {
                    PLAN_STATUS_MAP.put(deployPlan.getId(), false);
                }
            }
            logVo.setPublishCompleteFlag(PLAN_STATUS_MAP.get(deployPlanVo.getId()));
            logVo.setLogContent(log);
            return ResultVo.Builder.SUCC().initSuccData(logVo);
        } catch (Exception e) {
            logger.error("release plan log exception , Exception = {}", e);
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("4000", "系统异常");
        }
    }

    @PostMapping("/betaPublishProcess")
    @ResponseBody
    public ResultVo betaPublishProcess(@RequestBody ProcessRequest processRequest) {
        try {
            double process = 0;
            boolean completeFlag = true;
            HashMap<String, Integer> recordMap = processRequest.getRecordMap();
            HashMap<String, Double> processMap = new HashMap<>();
            for (String key : recordMap.keySet()) {
                if (recordMap.get(key) <= 0) {
                    logger.info("beta publish process fail : 查询条件错误 ,appCode = {}", key);
                    continue;
                }
                BetaDelpoyRecord betaDelpoyRecord = betaDelpoyRecordService.getById(recordMap.get(key));
                if (betaDelpoyRecord == null) {
                    logger.info("beta publish process fail : 没有对应的发布记录 ,appCode = {},record = {}", key, recordMap.get(key));
                    continue;
                }
                Integer deployStatus = betaDelpoyRecord.getDeployStatus();
                if (PublishStatusEnum.BETA_SUCCESS.getCode().equals(deployStatus)
                        || PublishStatusEnum.BETA_FAIL.getCode().equals(deployStatus)
                        || PublishStatusEnum.BETA_SUSPEND.getCode().equals(deployStatus)) {
                    process = 100;
                }
                processMap.put(key, process);
            }
            for (Double value : processMap.values()) {
                if (value < 100) {
                    completeFlag = false;
                    break;
                }
            }
            ProcessVo processVo = new ProcessVo();
            processVo.setCompleteFlag(completeFlag);
            processVo.setProcessMap(processMap);
            return ResultVo.Builder.SUCC().initSuccData(processVo);
        } catch (Exception e) {
            logger.error("get publish process exception , Exception = {}", e);
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("4000", "系统异常");
        }
    }

    @PostMapping("/releasePublishProcess")
    @ResponseBody
    public ResultVo releasePublishProcess(@RequestBody ProcessRequest processRequest) {
        try {
            double process = 0;
            boolean completeFlag = true;
            HashMap<String, Integer> recordMap = processRequest.getRecordMap();
            HashMap<String, Double> processMap = new HashMap<>();
            for (String key : recordMap.keySet()) {
                ReleaseDelpoyRecord releaseDelpoyRecord = releaseDelpoyRecordService.getById(recordMap.get(key));
                if (releaseDelpoyRecord == null) {
                    logger.info("release publish process fail, no record , appCode = {},record = {}", key, recordMap.get(key));
                    continue;
                }
                Integer status = releaseDelpoyRecord.getDeployStatus();
                if (status.equals(PublishStatusEnum.RELEASE_SUCCESS.getCode()) || status.equals(PublishStatusEnum.RELEASE_FAIL.getCode())
                        || status.equals(PublishStatusEnum.RELEASE_SUSPEND.getCode())) {
                    process = 100;
                } else if (status.equals(PublishStatusEnum.RELEASE_WAIT.getCode())) {
                    process = 0;
                } else if (status.equals(PublishStatusEnum.RELEASE_RELEASE.getCode())) {
                    List<ProjectTaskBatchRecord> projectTaskBatchRecordList = projectTaskBatchRecordService.getByRecordId(recordMap.get(key));
                    int size = projectTaskBatchRecordList.size();
                    int count = 0;
                    for (ProjectTaskBatchRecord projectTaskBatchRecord : projectTaskBatchRecordList) {
                        if (projectTaskBatchRecord.getDeployStatus().equals(PublishStatusEnum.RELEASE_SUSPEND.getCode())
                                || projectTaskBatchRecord.getDeployStatus().equals(PublishStatusEnum.RELEASE_FAIL.getCode())
                                || projectTaskBatchRecord.getDeployStatus().equals(PublishStatusEnum.RELEASE_SUCCESS.getCode())) {
                            count++;
                        }
                    }
                    double result = new BigDecimal((float) count / size).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();

                    process = result * 100;
                }
                processMap.put(key, process);
            }
            for (Double value : processMap.values()) {
                if (value < 100) {
                    completeFlag = false;
                    break;
                }
            }
            ProcessVo processVo = new ProcessVo();
            processVo.setCompleteFlag(completeFlag);
            processVo.setProcessMap(processMap);
            return ResultVo.Builder.SUCC().initSuccData(processVo);
        } catch (Exception e) {
            logger.error("get publish process exception , Exception = {}", e);
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("4000", "系统异常");
        }
    }

    @PostMapping("/publishByPlan")
    @ResponseBody
    public ResultVo publishByPlan(@RequestBody PublishParamVo publishParamVo, HttpServletRequest request) {
        try {
            logger.info("release publish by plan , param = {}", JSON.toJSONString(publishParamVo.toString()));
            if (publishParamVo == null || publishParamVo.getProjectId() == null || publishParamVo.getProjectId() <= 0
                    || publishParamVo.getDeployPlanVos() == null || publishParamVo.getDeployPlanVos().size() <= 0) {
                logger.info("release publish by plan fail , 参数错误 , param = {}", publishParamVo.toString());
                return ResultVo.Builder.FAIL().initErrCodeAndMsg("2000", "参数错误");
            }

            Integer rollbackFlag = publishParamVo.getDeployPlanVos().get(0).getRollbackFlag();
            boolean isRollback = Integer.valueOf(1).equals(rollbackFlag);
            HashMap<String, Integer> countMap = countAppCode(publishParamVo.getDeployPlanVos());
            for (String appCode : countMap.keySet()) {
                if (!releaseCheck(appCode)) {
                    logger.info("release publish by plan 存在发布的任务，发布任务param = {}", appCode);
                    return ResultVo.Builder.FAIL().initErrCodeAndMsg("2002", "存在发布中的应用" + appCode);
                }
            }
            if (isRollback) {
                for (DeployPlanVo deployPlanVo : publishParamVo.getDeployPlanVos()) {
                    if (StringUtils.isEmpty(deployPlanVo.getAppRtag())) {
                        logger.info("release publish by plan 回滚RTag为空，发布任务param = {}", deployPlanVo.getAppCode());
                        return ResultVo.Builder.FAIL().initErrCodeAndMsg("2002", "回滚RTag为空 " + deployPlanVo.getAppCode());
                    }
                }
            }
            if (StringUtils.isEmpty(publishParamVo.getAccessToken())) {
                logger.error("release publish by plan no user info , param = {}", publishParamVo.getAccessToken());
                return ResultVo.Builder.FAIL().initErrCodeAndMsg("2004", "当前用户无token已过期或无发布权限");
            }
            String userAgent = request.getHeader("user-agent");
            String ip1 = getHttpIp(request);
            String userName = getUser(publishParamVo.getAccessToken(), userAgent, ip1);
            if (userName == null) {
                logger.error("release publish by plan no user info , param = {}", publishParamVo.getAccessToken());
                return ResultVo.Builder.FAIL().initErrCodeAndMsg("2004", "当前用户无token已过期或无发布权限");
            }
            Collections.sort(publishParamVo.getDeployPlanVos(), new Comparator<DeployPlanVo>() {
                @Override
                public int compare(DeployPlanVo o1, DeployPlanVo o2) {
                    return o1.getSequenece() - o2.getSequenece();
                }
            });
            Integer recordForwardId = 0;
            HashMap<String, Integer> recordMap = new HashMap<>();
            Integer planForwardId = 0;

            StringBuilder builder = new StringBuilder();
            for (DeployPlanVo deployPlanVo : publishParamVo.getDeployPlanVos()) {
                Integer deployStatus = deployPlanVo.getDeployStatus();
                Integer disable = deployPlanVo.getDisable();
                if ((deployStatus != null && PublishStatusEnum.RELEASE_SUCCESS.getCode().equals(deployStatus))
                        || (disable != null && Integer.valueOf(0).equals(disable))) {
                    if (!isRollback || (disable != null && Integer.valueOf(0).equals(disable))) {
                        logger.info("release publish by plan pass , param = {}", deployPlanVo.toString());
                        if (Integer.valueOf(0).equals(disable)) {
                            builder.append("批次" + deployPlanVo.getSequenece() + "，应用 " + deployPlanVo.getAppCode() + " 已经被禁用，本次将不会进行" + (!isRollback ? "发布" : "回滚") + "。\r\n");
                        } else if (PublishStatusEnum.RELEASE_SUCCESS.getCode().equals(deployStatus)) {
                            builder.append("批次" + deployPlanVo.getSequenece() + "，应用 " + deployPlanVo.getAppCode() + " 已经发布成功，本次将不会进行发布。\r\n");
                        }
                        continue;
                    }
                }
                if (!isRollback) {
                    int successCount = 0;
                    List<PlanMachineMappingVo> planMachineMappingList = deployPlanVo.getPlanMachineMappings();
                    for (PlanMachineMappingVo machineMapping : planMachineMappingList) {
                        if (machineMapping.getDeployStatus().equals(PublishStatusEnum.RELEASE_SUCCESS.getCode())) {
                            successCount++;
                        }
                    }
                    if (planMachineMappingList.size() >= 0) {
                        if (successCount == planMachineMappingList.size()) {
                            String ips = getServerIps(planMachineMappingList);
                            builder.append("批次" + deployPlanVo.getSequenece() + "，应用 " + deployPlanVo.getAppCode() + " 机器 " + ips.toString() + " 已经发布成功，本次将不会进行" + (!isRollback ? "发布" : "回滚") + "。\r\n");
                            logger.info("release publish by plan pass , param = {}", deployPlanVo.toString());
                            continue;
                        }
                    }
                }
                ReleaseDelpoyRecord releaseDelpoyRecord = new ReleaseDelpoyRecord();
                if (recordMap.get(deployPlanVo.getAppCode()) == null) {
                    releaseDelpoyRecord.setAppBtag(deployPlanVo.getAppBtag());
                    releaseDelpoyRecord.setAppRtag(deployPlanVo.getAppRtag());
                    releaseDelpoyRecord.setAppCode(deployPlanVo.getAppCode());
                    releaseDelpoyRecord.setPublishUser(userName);
                    releaseDelpoyRecord.setForwardRecordId(recordForwardId);
                    releaseDelpoyRecord.setProjectTaskId(deployPlanVo.getProjectTaskId());
                    releaseDelpoyRecord.setProjectId(deployPlanVo.getProjectId());
                    if (isRollback) {
                        releaseDelpoyRecord.setAppBranch(deployPlanVo.getAppRtag());
                    } else {
                        releaseDelpoyRecord.setAppBranch(deployPlanVo.getAppBtag());
                    }
                    releaseDelpoyRecord.setDeployStatus(PublishStatusEnum.RELEASE_WAIT.getCode());
                    releaseDelpoyRecord.setRollbackFlag(deployPlanVo.getRollbackFlag());
                    int result = releaseDelpoyRecordService.insert(releaseDelpoyRecord);
                    if (result <= 0) {
                        builder.append("批次" + deployPlanVo.getSequenece() + "，应用 " + deployPlanVo.getAppCode() + " 添加发布记录失败，本次将不会进行" + (!isRollback ? "发布" : "回滚") + "。\r\n");
                        logger.error("release publish by plan insert release record fail , param = {}", releaseDelpoyRecord.toString());
                        continue;
                    }
                    recordForwardId = releaseDelpoyRecord.getId();
                    recordMap.put(deployPlanVo.getAppCode(), recordForwardId);
                    ProjectTask projectTask = new ProjectTask();
                    projectTask.setId(deployPlanVo.getProjectTaskId());
                    projectTask.setReleaseTaskStatus(PublishStatusEnum.RELEASE_WAIT.getCode());
                    projectTaskService.updateById(projectTask);
                }
                AppInfoDto appInfo = null;
                ResultVo resultVo1 = getAppInfo(deployPlanVo.getAppCode(), "prod");
                if (resultVo1.getCode().equals("0") && resultVo1.getRet().equals("success")) {
                    appInfo = JSON.parseObject(String.valueOf(resultVo1.getData()), AppInfoDto.class);
                } else {
                    logger.info("未查询到对应的appInfo信息，appCode = {}", deployPlanVo.getAppCode());
                    return ResultVo.Builder.FAIL().initErrCodeAndMsg("2001", "无对应的appCode信息");
                }
                StringBuilder ips = new StringBuilder();
                for (PlanMachineMappingVo planMachineMappingVo : deployPlanVo.getPlanMachineMappings()) {
                    if (isRollback || appInfo.getPkgType().equals(PackageType.STATIC.getValue()) || (!planMachineMappingVo.getDeployStatus().equals(PublishStatusEnum.RELEASE_SUCCESS.getCode()) && planMachineMappingVo.getDisable() == 1)) {
                        if (ips.length() > 0) {
                            ips.append(",");
                        }
                        ips.append(planMachineMappingVo.getServiceIps());
                        PlanMachineMapping planMachineMapping = new PlanMachineMapping();
                        planMachineMapping.setId(planMachineMappingVo.getId());
                        planMachineMapping.setDeployStatus(PublishStatusEnum.RELEASE_WAIT.getCode());
                        planMachineMapping.setAppTag("");
                        planMachineMappingService.updateById(planMachineMapping);
                    }
                }
                deployPlanVo.setServiceIps(ips.toString());
                Integer machineTotalCount = 0;
                ResultVo resultVo = getServerCount(deployPlanVo.getAppCode(), deployPlanVo.getAppGroup());
                if (resultVo.getCode().equals("0") && resultVo.getRet().equals("success")) {
                    machineTotalCount = (Integer) resultVo.getData();
                }
                List<String> ipsList = new ArrayList<>();
                if (!appInfo.getPkgType().equals(PackageType.STATIC.getValue())) {
                    ipsList = convertToPlanList(deployPlanVo.getServiceIps(), machineTotalCount, countMap.get(deployPlanVo.getAppCode()));
                } else {
                    ipsList.add(deployPlanVo.getServiceIps());
                }
                if (!ipsList.isEmpty()) {
                    for (String ip : ipsList) {
                        DeployPlanRecord deployPlanRecord = new DeployPlanRecord();
                        deployPlanRecord.setDeployStatus(PublishStatusEnum.RELEASE_WAIT.getCode());
                        deployPlanRecord.setProjectId(deployPlanVo.getProjectId());
                        deployPlanRecord.setServiceIps(ip);
                        deployPlanRecord.setAppBtag(deployPlanVo.getAppBtag());
                        deployPlanRecord.setAppRtag(deployPlanVo.getAppRtag());
                        deployPlanRecord.setAppGroup(deployPlanVo.getAppGroup());
                        deployPlanRecord.setAppCode(deployPlanVo.getAppCode());
                        deployPlanRecord.setMachineCount(ip.split(",").length);
                        deployPlanRecord.setForwardId(planForwardId);
                        deployPlanRecord.setSequenece(deployPlanVo.getSequenece());
                        deployPlanRecord.setDeployRecordId(recordMap.get(deployPlanVo.getAppCode()));
                        deployPlanRecord.setProjectTaskId(deployPlanVo.getProjectTaskId());
                        deployPlanRecord.setPlanId(deployPlanVo.getId());
                        deployPlanRecord.setRollbackFlag(deployPlanVo.getRollbackFlag());
                        int result = deployPlanRecordService.insert(deployPlanRecord);
                        if (result <= 0) {
                            builder.append("批次" + deployPlanVo.getSequenece() + "，应用 " + deployPlanVo.getAppCode() + " 添加发布计划记录失败，本次将不会进行" + (!isRollback ? "发布" : "回滚") + "。\r\n");
                            logger.error("release publish by plan insert plan record fail , param = {}", deployPlanRecord.toString());
                            continue;
                        }
                        planForwardId = deployPlanRecord.getId();
                    }
                } else {
                    DeployPlanRecord deployPlanRecord = new DeployPlanRecord();
                    deployPlanRecord.setDeployStatus(PublishStatusEnum.RELEASE_WAIT.getCode());
                    deployPlanRecord.setProjectId(deployPlanVo.getProjectId());
                    deployPlanRecord.setAppBtag(deployPlanVo.getAppBtag());
                    deployPlanRecord.setAppRtag(deployPlanVo.getAppRtag());
                    deployPlanRecord.setAppGroup(deployPlanVo.getAppGroup());
                    deployPlanRecord.setAppCode(deployPlanVo.getAppCode());
                    deployPlanRecord.setMachineCount(0);
                    deployPlanRecord.setForwardId(planForwardId);
                    deployPlanRecord.setSequenece(deployPlanVo.getSequenece());
                    deployPlanRecord.setDeployRecordId(recordMap.get(deployPlanVo.getAppCode()));
                    deployPlanRecord.setProjectTaskId(deployPlanVo.getProjectTaskId());
                    deployPlanRecord.setPlanId(deployPlanVo.getId());
                    deployPlanRecord.setRollbackFlag(deployPlanVo.getRollbackFlag());
                    int result = deployPlanRecordService.insert(deployPlanRecord);
                    if (result <= 0) {
                        builder.append("批次" + deployPlanVo.getSequenece() + "，应用 " + deployPlanVo.getAppCode() + " 添加发布计划记录失败，本次将不会进行" + (!isRollback ? "发布" : "回滚") + "。\r\n");
                        logger.error("release publish by plan insert plan record fail , param = {}", deployPlanRecord.toString());
                        continue;
                    }
                    planForwardId = deployPlanRecord.getId();
                }
                PLAN_RECORD_MAP.put(deployPlanVo.getId(), releaseDelpoyRecord.getId());
                PLAN_STATUS_MAP.put(deployPlanVo.getId(), false);
                DeployPlanRecord deployPlanRecord = new DeployPlanRecord();
                deployPlanRecord.setWaitTime(deployPlanVo.getWaitTime());
                deployPlanRecord.setId(planForwardId);
                deployPlanRecordService.updateById(deployPlanRecord);
                DeployPlan deployPlan = new DeployPlan();
                deployPlan.setId(deployPlanVo.getId());
                deployPlan.setDeployStatus(PublishStatusEnum.RELEASE_WAIT.getCode());
                deployPlanService.updateById(deployPlan);
            }
            DeployPlanRecord deployPlanRecord = new DeployPlanRecord();
            deployPlanRecord.setProjectId(publishParamVo.getProjectId());
            deployPlanRecord.setForwardId(0);
            deployPlanRecord.setDeployStatus(PublishStatusEnum.RELEASE_WAIT.getCode());
            List<DeployPlanRecord> deployPlanRecordList = deployPlanRecordService.listByCondition(deployPlanRecord);
            for (DeployPlanRecord deployPlanRecord1 : deployPlanRecordList) {
                Callable releaseCallable = new Callable() {
                    @Override
                    public Object call() throws Exception {
                        ReleaseDelpoyRecord releaseDelpoyRecord = releaseDelpoyRecordService.getById(deployPlanRecord1.getDeployRecordId());
                        releaseDeploy(releaseDelpoyRecord, null, userName, deployPlanRecord1);
                        return null;
                    }
                };
                EXECUTE_SHELL.submit(releaseCallable);
            }

            PublishPlanResultVo publishPlanResultVo = new PublishPlanResultVo();
            publishPlanResultVo.setDeployPlanRecordList(deployPlanRecordList);
            publishPlanResultVo.setPublishCount(deployPlanRecordList.size());
            publishPlanResultVo.setHasTips(builder.length() > 0 ? 1 : 0);
            publishPlanResultVo.setTips(builder.toString());
            return ResultVo.Builder.SUCC().initSuccData(publishPlanResultVo);
        } catch (Exception e) {
            logger.error("release publish by plan exception , Exception = {}", e);
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("4000", "系统异常");
        }
    }

    private String getServerIps(List<PlanMachineMappingVo> planMachineMappingList) {
        if (!CollectionUtils.isEmpty(planMachineMappingList)) {
            StringBuilder builder = new StringBuilder();
            planMachineMappingList.stream().forEach(planMachineMapping -> {
                if (builder.length() > 0) {
                    builder.append(",");
                }
                builder.append(planMachineMapping.getServiceIps());
            });
            return builder.toString();
        }
        return null;
    }

    @PostMapping("/releaseSuspendByPlan")
    @ResponseBody
    public ResultVo releaseSuspendByPlan(@RequestBody PublishParamVo publishParamVo) {
        try {
            logger.info("release publish suspend by plan , param = {}", publishParamVo.toString());
            if (publishParamVo.getProjectId() == null || publishParamVo.getProjectId() <= 0
                    || publishParamVo.getDeployPlanVos() == null || publishParamVo.getDeployPlanVos().size() <= 0) {
                logger.info("release publish suspend by plan fail , 参数错误 , param = {}", publishParamVo.toString());
                return ResultVo.Builder.FAIL().initErrCodeAndMsg("2000", "参数错误");
            }
            for (DeployPlanVo deployPlanVo : publishParamVo.getDeployPlanVos()) {
                if (deployPlanVo.getDeployStatus().equals(PublishStatusEnum.RELEASE_FAIL.getCode())
                        || deployPlanVo.getDeployStatus().equals(PublishStatusEnum.RELEASE_SUCCESS.getCode())
                        || deployPlanVo.getDeployStatus().equals(PublishStatusEnum.RELEASE_SUSPEND.getCode())) {
                    logger.info("release publish suspend by plan is final status , no suspend , param = {}", deployPlanVo.getAppCode());
                    continue;
                }
                DeployPlan deployPlan = new DeployPlan();
                deployPlan.setProjectTaskId(deployPlanVo.getProjectTaskId());
                deployPlan.setDelFlag(1);
                deployPlanService.updateStatusToSuspend(deployPlan);
                PLAN_STATUS_MAP.put(deployPlan.getId(), true);
                DeployPlanRecord deployPlanRecord = new DeployPlanRecord();
                deployPlanRecord = deployPlanRecordService.getLastByPlanId(deployPlanVo.getId());
                if (deployPlanRecord == null) {
                    logger.info("release publish suspend by plan no plan record data , param = {}", deployPlanVo.toString());
                    continue;
                }
                ReleaseDelpoyRecord releaseDelpoyRecord = releaseDelpoyRecordService.getById(deployPlanRecord.getDeployRecordId());
                if (releaseDelpoyRecord == null || releaseDelpoyRecord.getDeployStatus() == null) {
                    logger.info("release publish suspend by plan no record data , param = {}", deployPlanVo.toString());
                    continue;
                }
                Integer status = deployPlanRecord.getDeployStatus();
                DeployPlanRecord planRecord = new DeployPlanRecord();
                planRecord.setPlanId(deployPlanVo.getId());
                planRecord.setDelFlag(1);
                deployPlanRecordService.updateStatusToSuspend(planRecord);
                if (status.equals(PublishStatusEnum.BUILD_SUCCESS.getCode()) || status.equals(PublishStatusEnum.RELEASE_RELEASE.getCode())
                        || status.equals(PublishStatusEnum.CODE_PUSH_SUCCESS.getCode()) || status.equals(PublishStatusEnum.RELEASE_WAIT.getCode())
                        || status.equals(PublishStatusEnum.INIT.getCode()) || status.equals(PublishStatusEnum.RELEASE_PART_SUCCESS.getCode())) {
                    updateStatusSuspend(PublishStatusEnum.RELEASE_SUSPEND.getCode(), releaseDelpoyRecord, true);
                }
            }
            return ResultVo.Builder.SUCC().initSuccData("暂停成功");
        } catch (Exception e) {
            logger.error("release publish suspend by plan exception, Exception = {}", e);
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("4000", "系统异常");
        }
    }

    @PostMapping("/releaseProcessByPlan")
    @ResponseBody
    public ResultVo releaseProcessByPlan(@RequestBody PublishParamVo publishParamVo) {
        try {
            if (publishParamVo.getProjectId() == null || publishParamVo.getProjectId() <= 0
                    || publishParamVo.getDeployPlanVos() == null || publishParamVo.getDeployPlanVos().size() <= 0) {
                logger.info("release publish process by plan fail , 参数错误 , param = {}", publishParamVo.toString());
                return ResultVo.Builder.FAIL().initErrCodeAndMsg("2000", "参数错误");
            }
            boolean completeFlag = true;
            List<DeployPlanVo> deployPlanVoList = new ArrayList<>();
            for (DeployPlanVo deployPlanVo : publishParamVo.getDeployPlanVos()) {
                DeployPlan deployPlan = deployPlanService.getById(deployPlanVo.getId());
                if (deployPlan == null) {
                    logger.error("elease publish process by plan no plan , param = {}", deployPlanVo);
                    continue;
                }
                deployPlanVo = convertTo(deployPlan);
                deployPlanVoList.add(deployPlanVo);
                if (deployPlanVo.getDeployStatus().equals(PublishStatusEnum.BUILD_SUCCESS.getCode())
                        || deployPlanVo.getDeployStatus().equals(PublishStatusEnum.CODE_PUSH_SUCCESS.getCode())
                        || deployPlanVo.getDeployStatus().equals(PublishStatusEnum.RELEASE_RELEASE.getCode())
                        || deployPlanVo.getDeployStatus().equals(PublishStatusEnum.RELEASE_WAIT.getCode())) {
                    completeFlag = false;
                } else if (deployPlanVo.getDeployStatus().equals(PublishStatusEnum.RELEASE_PART_SUCCESS.getCode())) {
                    List<DeployPlanRecord> deployPlanRecordList = deployPlanRecordService.getByPlanId(deployPlanVo.getId());
                    if (!CollectionUtils.isEmpty(deployPlanRecordList)) {
                        int successCount = 0;
                        for (DeployPlanRecord record : deployPlanRecordList) {
                            if (PublishStatusEnum.RELEASE_SUCCESS.getCode().equals(record.getDeployStatus()) ||
                                    PublishStatusEnum.RELEASE_FAIL.getCode().equals(record.getDeployStatus()) ||
                                    PublishStatusEnum.RELEASE_SUSPEND.getCode().equals(record.getDeployStatus())) {
                                successCount++;
                            }
                        }
                        if (successCount != deployPlanRecordList.size()) {
                            completeFlag = false;
                        }
                    }
                }
            }
            publishParamVo.setDeployPlanVos(deployPlanVoList);
            ProcessVo processVo = new ProcessVo();
            processVo.setCompleteFlag(completeFlag);
            processVo.setDeployPlanVoList(publishParamVo.getDeployPlanVos());
            return ResultVo.Builder.SUCC().initSuccData(processVo);
        } catch (Exception e) {
            logger.error("release publish process by plan exception , Exception = {}", e);
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("4000", "系统异常");
        }
    }

    private BetaDelpoyRecord trans(PublishSequeneceVo publishSequeneceVo, PublishParamVo publishParamVo) {
        BetaDelpoyRecord betaDelpoyRecord = new BetaDelpoyRecord();
        betaDelpoyRecord.setProjectId(publishParamVo.getProjectId());
        betaDelpoyRecord.setAppCode(publishSequeneceVo.getAppCode());
        betaDelpoyRecord.setAppBranch(publishSequeneceVo.getAppBranch());
        betaDelpoyRecord.setAppBtag(publishSequeneceVo.getAppBtag());
        Boolean isStressDeploy = publishParamVo.getIsStressDeploy();
        boolean isStress = isStressDeploy != null && isStressDeploy.booleanValue();
        betaDelpoyRecord.setServiceIps(isStress ? publishSequeneceVo.getStressIps() : publishSequeneceVo.getServiceIps());
        betaDelpoyRecord.setProjectTaskId(publishSequeneceVo.getId());
        betaDelpoyRecord.setDockerEnv(publishSequeneceVo.getDockerEnv());
        betaDelpoyRecord.setBizLine(publishSequeneceVo.getBizLine());
        //docker发布数据库置为1，否则置为0

        if (publishSequeneceVo.getStressDeploy() != null && publishSequeneceVo.getStressDeploy()) {
            betaDelpoyRecord.setIsDockerDeploy(0);
        } else if (publishSequeneceVo.getIsDockerDeploy() != null && publishSequeneceVo.getIsDockerDeploy()) {
            betaDelpoyRecord.setIsDockerDeploy(1);
        } else {
            betaDelpoyRecord.setIsDockerDeploy(0);
        }
        betaDelpoyRecord.setProfile(publishSequeneceVo.getProfile());
        if (publishSequeneceVo.getIsDeploy()) {
            betaDelpoyRecord.setIsDeploy(0);
        } else {
            betaDelpoyRecord.setIsDeploy(1);
        }
        return betaDelpoyRecord;
    }

    private ReleaseDelpoyRecord transToReleaseDelpoyRecord(PublishSequeneceVo publishSequeneceVo, PublishParamVo publishParamVo) {
        ReleaseDelpoyRecord releaseDelpoyRecord = new ReleaseDelpoyRecord();
        releaseDelpoyRecord.setAppCode(publishSequeneceVo.getAppCode());
        releaseDelpoyRecord.setProjectId(publishParamVo.getProjectId());
        releaseDelpoyRecord.setAppBranch(publishSequeneceVo.getAppBranch());
        releaseDelpoyRecord.setServiceIps(publishSequeneceVo.getServiceIps());//改
        releaseDelpoyRecord.setProjectTaskId(publishSequeneceVo.getId());
        releaseDelpoyRecord.setAppBtag(publishSequeneceVo.getAppBtag());//pass
        return releaseDelpoyRecord;
    }

    private ProjectTaskBatchRecord transToProjectTaskBatchRecord(PublishBatchVo publishBatchVo, Integer recordId, PublishSequeneceVo publishSequeneceVo) {
        ProjectTaskBatchRecord projectTaskBatchRecord = new ProjectTaskBatchRecord();
        projectTaskBatchRecord.setProjectTaskId(publishSequeneceVo.getId());
        projectTaskBatchRecord.setSequenece(publishBatchVo.getSequenece());
        projectTaskBatchRecord.setServiceIps(publishBatchVo.getServiceIps());
        projectTaskBatchRecord.setDeployRecordId(recordId);
        projectTaskBatchRecord.setWaitTime(publishBatchVo.getWaitTime());
        projectTaskBatchRecord.setMachineCount(publishBatchVo.getMachineCount());
        projectTaskBatchRecord.setAppGroup(publishBatchVo.getAppGroup());
        return projectTaskBatchRecord;
    }

    private String getBetaPublishScript(String packageType, Integer isDeploy, Boolean isDockerDeploy) {
        JSONObject json = JSON.parseObject(betaScript);
        if (isDockerDeploy) {
            json = JSON.parseObject(k8sBetaScript);
        }
        String script = "";
        if (isDeploy == 0) {
            script = json.get("JARAPI").toString();
            return script;
        }
        if (PackageType.GO.getValue().equals(packageType)) {
            script = json.get("GO").toString();
        } else if (PackageType.JAR.getValue().equals(packageType)) {
            script = json.get("JAR").toString();
        } else if (PackageType.JETTY.getValue().equals(packageType)) {
            script = json.get("JETTY").toString();
        } else if (PackageType.PYTHON.getValue().equals(packageType)) {
            script = json.get("PYTHON").toString();
        } else if (PackageType.STATIC.getValue().equals(packageType)) {
            script = json.get("STATIC").toString();
        } else if (PackageType.TOMCAT.getValue().equals(packageType)) {
            script = json.get("TOMCAT").toString();
        } else if (PackageType.JARAPI.getValue().equals(packageType)) {
            script = json.get("JARAPI").toString();
        } else if (PackageType.WMS_CONF.getValue().equals(packageType)) {
            script = json.get("WMS_CONF").toString();
        } else if (PackageType.WMS_WWWROOT.getValue().equals(packageType)) {
            script = json.get("WMS_WWWROOT").toString();
        } else if (PackageType.SPRING_GZ.getValue().equals(packageType)) {
            script = json.get("SPRING-GZ").toString();
        } else if (PackageType.JAR_MAIN.getValue().equals(packageType)) {
            script = json.get("JAR-MAIN").toString();
        } else {
            script = json.get("JAR").toString();
        }
        return script;
    }

    private String getRelasePublishScript(String packageType) {
        JSONObject json = JSON.parseObject(releaseScript);
        String script = "";
        if (PackageType.GO.getValue().equals(packageType)) {
            script = json.get("GO").toString();
        } else if (PackageType.JAR.getValue().equals(packageType)) {
            script = json.get("JAR").toString();
        } else if (PackageType.JETTY.getValue().equals(packageType)) {
            script = json.get("JETTY").toString();
        } else if (PackageType.PYTHON.getValue().equals(packageType)) {
            script = json.get("PYTHON").toString();
        } else if (PackageType.STATIC.getValue().equals(packageType)) {
            script = json.get("STATIC").toString();
        } else if (PackageType.TOMCAT.getValue().equals(packageType)) {
            script = json.get("TOMCAT").toString();
        } else if (PackageType.JARAPI.getValue().equals(packageType)) {
            script = json.get("JARAPI").toString();
        } else if (PackageType.WMS_CONF.getValue().equals(packageType)) {
            script = json.get("WMS_CONF").toString();
        } else if (PackageType.WMS_WWWROOT.getValue().equals(packageType)) {
            script = json.get("WMS_WWWROOT").toString();
        } else if (PackageType.SPRING_GZ.getValue().equals(packageType)) {
            script = json.get("SPRING-GZ").toString();
        } else if (PackageType.JAR_MAIN.getValue().equals(packageType)) {
            script = json.get("JAR-MAIN").toString();
        } else {
            script = json.get("JAR").toString();
        }
        return script;
    }

    private String getMavenPath(Integer nexusTag, Boolean betaFlag) {
        JSONObject object = JSON.parseObject(mavenPath);
        String mavenPath = "";
        if (betaFlag) {
            mavenPath = object.get("beta_mryx").toString();
        } else {
            mavenPath = object.get("release_mryx").toString();
        }
        if (nexusTag != null) {
            if (nexusTag.equals(NexusTag.mryx.getCode())) {
                if (betaFlag) {
                    mavenPath = object.get("beta_mryx").toString();
                } else {
                    mavenPath = object.get("release_mryx").toString();
                }
            } else if (nexusTag.equals(NexusTag.missfresh.getCode())) {
                if (betaFlag) {
                    mavenPath = object.get("beta_missfresh").toString();
                } else {
                    mavenPath = object.get("release_missfresh").toString();
                }
            } else if (NexusTag.officialnpm.getCode().equals(nexusTag)) {
                mavenPath = NexusTag.officialnpm.getValue();
            } else if (NexusTag.taobaonpm.getCode().equals(nexusTag)) {
                mavenPath = NexusTag.taobaonpm.getValue();
            } else if (NexusTag.wuliunpm.getCode().equals(nexusTag)) {
                mavenPath = NexusTag.wuliunpm.getValue();
            }
        }
        return mavenPath;
    }

    private BetaDeployParam transToBetaDeployParam(BetaDelpoyRecord betaDelpoyRecord, AppInfoDto appInfo, String userName) {
        BetaDeployParam betaDeployParam = new BetaDeployParam();
        betaDeployParam.setAppCode(betaDelpoyRecord.getAppCode());
        betaDeployParam.setAppName(appInfo.getPkgName());
        betaDeployParam.setRecord(betaDelpoyRecord.getId());
        betaDeployParam.setBuildBranch(betaDelpoyRecord.getAppBranch().trim());
        betaDeployParam.setBuildPath(appInfo.getDeployPath());
        if (betaDelpoyRecord.getProfile() == null || betaDelpoyRecord.getProfile() == "") {
            betaDeployParam.setBuildType("beta");
        } else {
            betaDeployParam.setBuildType(betaDelpoyRecord.getProfile());
        }
        betaDeployParam.setDeployPath(appInfo.getDeployPath());
        betaDeployParam.setGitAddress(appInfo.getGit());
        betaDeployParam.setHealthCheck(appInfo.getHealthcheck());
        betaDeployParam.setJobName("");//delete
        if (appInfo.getDeployParameters() != null) {
            betaDeployParam.setMvnOps(appInfo.getDeployParameters());
        }
        betaDeployParam.setJarName(appInfo.getPkgName());
        betaDeployParam.setProjectPom("pom.xml");
        betaDeployParam.setServerList(betaDelpoyRecord.getServiceIps() == null ? null : betaDelpoyRecord.getServiceIps().trim());
        betaDeployParam.setDockerEnv(betaDelpoyRecord.getDockerEnv());
        betaDeployParam.setBizLine(betaDelpoyRecord.getBizLine());
        if (betaDelpoyRecord.getIsDockerDeploy() == 1) {
            betaDeployParam.setDockerDeploy(true);
            betaDeployParam.setScript(getBetaPublishScript(appInfo.getPkgType(), betaDelpoyRecord.getIsDeploy(), true));
        } else {
            betaDeployParam.setDockerDeploy(false);
            betaDeployParam.setScript(getBetaPublishScript(appInfo.getPkgType(), betaDelpoyRecord.getIsDeploy(), false));
        }
        betaDeployParam.setUser(userName);
        betaDeployParam.setWorkspace("");//待删
        betaDeployParam.setAppType(appInfo.getPkgType());
        betaDeployParam.setProjectTaskId(betaDelpoyRecord.getProjectTaskId());
        betaDeployParam.setMavenPath(getMavenPath(appInfo.getNexusTag(), true));
        if (appInfo.getVmOption() != null) {
            betaDeployParam.setStartOps(appInfo.getVmOption().replaceAll(" ", "@"));
        }
        //增加包部署路径和容器路径（tomcat/jetty）
        if (appInfo.getPkgPath() != null && appInfo.getPkgPath() != "") {
            betaDeployParam.setPkgPath(appInfo.getPkgPath());
        }
        if (appInfo.getContainerPath() != null && appInfo.getContainerPath() != "") {
            betaDeployParam.setContainerPath(appInfo.getContainerPath());
        }
        if (appInfo.getStartShellPath() != null && appInfo.getStartShellPath() != "") {
            betaDeployParam.setStartShellPath(appInfo.getStartShellPath());
        }
        if (appInfo.getStopShellPath() != null && appInfo.getStopShellPath() != "") {
            betaDeployParam.setStopShellPath(appInfo.getStopShellPath());
        }
        if (appInfo.getStartLogPath() != null && appInfo.getStartLogPath() != "") {
            betaDeployParam.setStartLogPath(appInfo.getStartLogPath());
        }
        if (appInfo.getStartSuccFlag() != null && appInfo.getStartSuccFlag() != "") {
            betaDeployParam.setStartSuccFlag(appInfo.getStartSuccFlag());
        }
        logger.info("betaDeployParam = {}", betaDeployParam.toString());
        return betaDeployParam;
    }

    private ReleaseDeployParam transReleaseDeployParam(ReleaseDelpoyRecord releaseDelpoyRecord, ProjectTaskBatchRecord projectTaskBatchRecord, AppInfoDto appInfoDto, String userName, DeployPlanRecord deployPlanRecord) {
        ReleaseDeployParam releaseDeployParam = new ReleaseDeployParam();
        releaseDeployParam.setAppCode(releaseDelpoyRecord.getAppCode());
        releaseDeployParam.setHealthCheck(appInfoDto.getHealthcheck());
        releaseDeployParam.setAppType(appInfoDto.getPkgType());
        releaseDeployParam.setJarName(appInfoDto.getPkgName());
        releaseDeployParam.setJobName("");
        releaseDeployParam.setRecord(releaseDelpoyRecord.getId());
        releaseDeployParam.setWorkspace("");
        releaseDeployParam.setUser(userName);
        if (projectTaskBatchRecord == null && deployPlanRecord == null) {
            releaseDeployParam.setBatch(0);
            releaseDeployParam.setServerList(releaseDelpoyRecord.getServiceIps());//改成应用信息里的ip
            releaseDeployParam.setAppGroup(appInfoDto.getGroupInfo().get(0).getGroupName().trim());//gai
            releaseDeployParam.setWaitTime(60);
        } else if (projectTaskBatchRecord != null && deployPlanRecord == null) {
            releaseDeployParam.setBatch(projectTaskBatchRecord.getId());
            releaseDeployParam.setServerList(projectTaskBatchRecord.getServiceIps());
            releaseDeployParam.setAppGroup(projectTaskBatchRecord.getAppGroup().trim());
            releaseDeployParam.setWaitTime(projectTaskBatchRecord.getWaitTime() * 60);
        }
        if (projectTaskBatchRecord == null && deployPlanRecord != null) {
            releaseDeployParam.setPlan(deployPlanRecord.getId());
            releaseDeployParam.setServerList(deployPlanRecord.getServiceIps());
            releaseDeployParam.setAppGroup(deployPlanRecord.getAppGroup().trim());
            releaseDeployParam.setWaitTime(deployPlanRecord.getWaitTime());
        }
        releaseDeployParam.setScript(getRelasePublishScript(appInfoDto.getPkgType()));
        releaseDeployParam.setAppName(appInfoDto.getPkgName());
        releaseDeployParam.setProjectPom("pom.xml");
        if (appInfoDto.getDeployParameters() != null) {
            releaseDeployParam.setMvnOps(appInfoDto.getDeployParameters());
        }
        Map<String, String> vmOps = new HashMap<>();
        List<String> ips = new ArrayList<>();
        /**
         * 设置启动参数
         * 先取IP的启动参数 如果没有取应用的启动参数 如果没有 设置为null 走默认值
         */
        if (appInfoDto.getGroupInfo() != null && !appInfoDto.getGroupInfo().isEmpty()) {
            for (GroupInfoDto groupInfoDto : appInfoDto.getGroupInfo()) {
                if (groupInfoDto == null || groupInfoDto.getServerInfo() == null || groupInfoDto.getServerInfo().isEmpty()) {
                    continue;
                }
                for (ServerResourceDto serverResourceDto : groupInfoDto.getServerInfo()) {
                    if (serverResourceDto == null || serverResourceDto.getHostIp() == null || "".equals(serverResourceDto.getHostIp())) {
                        continue;
                    }
                    if (serverResourceDto.getVmOption() == null || "".equals(serverResourceDto.getVmOption())) {
                        ips.add(serverResourceDto.getHostIp());
                    } else {
                        vmOps.put(serverResourceDto.getHostIp(), serverResourceDto.getVmOption());
                    }
                }
            }
        }
        //如果有IP没有设置启动脚本，取应用的启动脚本
        if (ips != null && !ips.isEmpty() && appInfoDto.getVmOption() != null && !"".equals(appInfoDto.getVmOption())) {
            for (String ip : ips) {
                if (ip == null || "".equals(ip)) {
                    continue;
                }
                vmOps.put(ip, appInfoDto.getVmOption());
            }
        }
        logger.info("appInfoDto.getVmOption() = {},ips = {},vmOps = {}", appInfoDto.getVmOption(), ips, vmOps);
        //TODO 替换成@，后面可以优化
        if (vmOps != null && !vmOps.isEmpty()) {
            releaseDeployParam.setStartOps(JSON.toJSONString(vmOps).replaceAll(" ", "@"));
        }
        releaseDeployParam.setGitAddress(appInfoDto.getGit());
        releaseDeployParam.setDeployPath(appInfoDto.getDeployPath());
        releaseDeployParam.setBuildPath(appInfoDto.getDeployPath());
        releaseDeployParam.setProjectTaskId(releaseDelpoyRecord.getProjectTaskId());
        if (Integer.valueOf(1).equals(releaseDelpoyRecord.getRollbackFlag())) {
            releaseDeployParam.setBuildBranch(releaseDelpoyRecord.getAppRtag());
        } else {
            releaseDeployParam.setBuildBranch(releaseDelpoyRecord.getAppBtag());
        }
        releaseDeployParam.setMavenPath(getMavenPath(appInfoDto.getNexusTag(), false));
        if (releaseDeployParam.getAppType() == null) {
            releaseDeployParam.setBuildType("release");//待改
        } else if (releaseDeployParam.getAppType().equals(PackageType.GO.getValue())) {
            releaseDeployParam.setBuildType("prod");//待改
        } else {
            releaseDeployParam.setBuildType("release");//待改
        }
        //增加包部署路径和容器路径（tomcat/jetty）
        if (appInfoDto.getPkgPath() != null && appInfoDto.getPkgPath() != "") {
            releaseDeployParam.setPkgPath(appInfoDto.getPkgPath());
        }
        if (appInfoDto.getContainerPath() != null && appInfoDto.getContainerPath() != "") {
            releaseDeployParam.setContainerPath(appInfoDto.getContainerPath());
        }
        if (appInfoDto.getStartShellPath() != null && appInfoDto.getStartShellPath() != "") {
            releaseDeployParam.setStartShellPath(appInfoDto.getStartShellPath());
        }
        if (appInfoDto.getStopShellPath() != null && appInfoDto.getStopShellPath() != "") {
            releaseDeployParam.setStopShellPath(appInfoDto.getStopShellPath());
        }
        if (appInfoDto.getStartLogPath() != null && appInfoDto.getStartLogPath() != "") {
            releaseDeployParam.setStartLogPath(appInfoDto.getStartLogPath());
        }
        if (appInfoDto.getStartSuccFlag() != null && appInfoDto.getStartSuccFlag() != "") {
            releaseDeployParam.setStartSuccFlag(appInfoDto.getStartSuccFlag());
        }
        logger.info("releaseDeployParam = {}", releaseDeployParam.toString());
        return releaseDeployParam;
    }

    private ResultVo getAppInfo(String appCode, String env) {
        Map<String, Object> map = new HashMap<>();
        map.put("appCode", appCode);
        map.put("envCode", env);
        Optional<String> optional = HTTP_POOL_CLIENT.postJson(appDetailRemote, JSONObject.toJSONString(map));
        String response = optional.isPresent() ? optional.get() : "";
        logger.info("调用应用中心，返回的结果是：{}", response);
        return (ResultVo) JSON.parseObject(response, ResultVo.class);
    }

    private String getHttpIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (!StringUtils.isEmpty(ip) && !"unKnown".equalsIgnoreCase(ip)) {
            //多次反向代理后会有多个ip值，第一个ip才是真实ip
            int index = ip.indexOf(",");
            if (index != -1) {
                ip = ip.substring(0, index);
            }
        } else {
            ip = request.getHeader("X-Real-IP");
            if (StringUtils.isEmpty(ip) || "unKnown".equalsIgnoreCase(ip)) {
                ip = request.getRemoteAddr();
            }
        }
        return ip;
    }

    private String getUser(String accessToken, String userAgent, String ip) {
        Map<String, Object> map = new HashMap<>();
        map.put("accessToken", accessToken);
        map.put("userAgent", userAgent);
        map.put("ip", ip);
        Optional<String> optional = HTTP_POOL_CLIENT.postJson(dealAccesstockenUrl, JSONObject.toJSONString(map));
        String response = optional.isPresent() ? optional.get() : "";
        ResultVo resultVo = (ResultVo) JSON.parseObject(response, ResultVo.class);
        OauthUser oauthUser = new OauthUser();
        if (resultVo.getCode().equals("0") && resultVo.getRet().equals("success")) {
            oauthUser = (OauthUser) JSON.parseObject(String.valueOf(resultVo.getData()), OauthUser.class);
            if (oauthUser == null || (oauthUser.getUseable() != null && oauthUser.getUseable() != 1)) {
                logger.info("获取用户信息已失效，accessToken = {} ,userAgent = {}, ip = {}", accessToken, userAgent, ip);
                return null;
            }
            return oauthUser.getAccount();
        } else {
            logger.info("获取用户信息失败");
            return null;
        }
    }

    private Object deploy(BetaDelpoyRecord betaDelpoyRecord, AppInfoDto appInfo, String userName) {

        BetaDeployParam betaDeployParam = transToBetaDeployParam(betaDelpoyRecord, appInfo, userName);
        logger.info("发布的参数是：{}", JSON.toJSONString(betaDeployParam));
        String logPath = betaLogPath(betaDeployParam);
        //修改发布&记录表发布状态
        betaDelpoyRecord.setLogPath(logPath);
        betaDelpoyRecord.setDeployStatus(PublishStatusEnum.BETA_RELEASE.getCode());
        ProjectTask projectTask = new ProjectTask();
        projectTask.setId(betaDelpoyRecord.getProjectTaskId());
        projectTask.setTaskStatus(PublishStatusEnum.BETA_RELEASE.getCode());
        projectTaskService.updateById(projectTask);
        betaDelpoyRecordService.updateById(betaDelpoyRecord);
        publishService.betaPublish(betaDeployParam, logPath);
        return null;
    }

    private Object releaseDeploy(ReleaseDelpoyRecord releaseDelpoyRecord, ProjectTaskBatchRecord projectTaskBatchRecord, String userName, DeployPlanRecord deployPlanRecord) {
        ResultVo result = getAppInfo(releaseDelpoyRecord.getAppCode(), "prod");
        AppInfoDto appInfo = null;
        if (result.getCode().equals("0") && result.getRet().equals("success")) {
            appInfo = (AppInfoDto) JSON.parseObject(String.valueOf(result.getData()), AppInfoDto.class);
        } else {
            logger.info("未查询到对应的appInfo信息，appCode = {}", releaseDelpoyRecord.getAppCode());
            return null;
        }
        ReleaseDeployParam releaseDeployParam = transReleaseDeployParam(releaseDelpoyRecord, projectTaskBatchRecord, appInfo, userName, deployPlanRecord);
        String logPath = releaseLogPath(releaseDeployParam);
        //修改发布&记录表发布状态
        if (projectTaskBatchRecord != null) {
            projectTaskBatchRecord.setDeployStatus(PublishStatusEnum.RELEASE_RELEASE.getCode());
            projectTaskBatchRecordService.update(projectTaskBatchRecord);
            ProjectTaskBatch projectTaskBatch = new ProjectTaskBatch();
            projectTaskBatch.setId(projectTaskBatchRecord.getProjectTaskBatchId());
            projectTaskBatch.setDeployStatus(PublishStatusEnum.RELEASE_RELEASE.getCode());
            projectTaskBatchService.updateById(projectTaskBatch);
        }
        if (deployPlanRecord != null) {
            deployPlanRecord.setDeployStatus(PublishStatusEnum.RELEASE_RELEASE.getCode());
            deployPlanRecordService.updateById(deployPlanRecord);
            DeployPlan deployPlan = new DeployPlan();
            deployPlan.setDeployStatus(PublishStatusEnum.RELEASE_RELEASE.getCode());
            deployPlan.setId(deployPlanRecord.getPlanId());
            deployPlanService.updateById(deployPlan);
        }
        releaseDelpoyRecord.setLogPath(logPath);
        releaseDelpoyRecord.setDeployStatus(PublishStatusEnum.RELEASE_RELEASE.getCode());
        ProjectTask projectTask = new ProjectTask();
        projectTask.setId(releaseDelpoyRecord.getProjectTaskId());
        projectTask.setReleaseTaskStatus(PublishStatusEnum.RELEASE_RELEASE.getCode());
        projectTaskService.updateById(projectTask);
        releaseDelpoyRecordService.updateById(releaseDelpoyRecord);
        publishService.releasePublish(releaseDeployParam, null);
        return null;
    }

    private boolean betaCheck(String appCode, String ips) {
        List<BetaDelpoyRecord> betaDelpoyRecordList = betaDelpoyRecordService.listByCodeStatus(appCode);
        String[] ipList = ips.split(",");
        for (String ip : ipList) {
            for (BetaDelpoyRecord betaDelpoyRecord : betaDelpoyRecordList) {
                if (betaDelpoyRecord.getServiceIps().contains(ip)) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean releaseCheck(String appCode) {
        List<ReleaseDelpoyRecord> releaseDelpoyRecordList = releaseDelpoyRecordService.listByCodeStatus(appCode);
        if (!releaseDelpoyRecordList.isEmpty()) {
            return false;
        }
        return true;
    }

    private String betaLogPath(BetaDeployParam betaDeployParam) {
        Date now = new Date();
        //可以方便地修改日期格式
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HHmmss");
        String datetimeOfNow = dateFormat.format(now);
        return betaDeployShellFilePwd + betaDeployParam.getRecord() + "-" + datetimeOfNow + "-out.log";
    }

    private String releaseLogPath(ReleaseDeployParam releaseDeployParam) {
        Date now = new Date();
        //可以方便地修改日期格式
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HHmmss");
        String datetimeOfNow = dateFormat.format(now);
        return releaseDeployShellFilePwd + releaseDeployParam.getRecord() + "-" + datetimeOfNow + "-out.log";
    }

    private void updateStatusSuspend(Integer status, ReleaseDelpoyRecord releaseDelpoyRecord, boolean planPublish) {
        ProjectTask projectTask = new ProjectTask();
        projectTask.setId(releaseDelpoyRecord.getProjectTaskId());
        if (status.equals(PublishStatusEnum.BUILD_FAIL.getCode()) || status.equals(PublishStatusEnum.CODE_PUSH_FAIL.getCode())) {
            releaseDelpoyRecord.setDeployStatus(PublishStatusEnum.RELEASE_FAIL.getCode());
            releaseDelpoyRecord.setSubDeployStatus(status);
            projectTask.setReleaseTaskStatus(PublishStatusEnum.RELEASE_FAIL.getCode());
            projectTask.setSubReleaseTaskStatus(status);
        } else {
            releaseDelpoyRecord.setDeployStatus(status);
            projectTask.setReleaseTaskStatus(status);
        }
        releaseDelpoyRecordService.updateById(releaseDelpoyRecord);
        projectTaskService.updateById(projectTask);
        if (planPublish) {
            DeployPlanRecord deployPlanRecord = new DeployPlanRecord();
            deployPlanRecord.setDeployRecordId(releaseDelpoyRecord.getId());
            deployPlanRecord.setDelFlag(1);
            deployPlanRecord.setDeployStatus(PublishStatusEnum.RELEASE_WAIT.getCode());
            deployPlanRecordService.updateStatusToSuspend(deployPlanRecord);
            DeployPlan deployPlan = new DeployPlan();
            deployPlan.setProjectTaskId(releaseDelpoyRecord.getProjectTaskId());
            deployPlan.setDeployStatus(PublishStatusEnum.RELEASE_WAIT.getCode());
            deployPlan.setDelFlag(1);
            deployPlanService.updateStatusToSuspend(deployPlan);
        } else {
            ProjectTaskBatchRecord projectTaskBatchRecord = new ProjectTaskBatchRecord();
            projectTaskBatchRecord.setDeployRecordId(releaseDelpoyRecord.getId());
            projectTaskBatchRecord.setDeployStatus(PublishStatusEnum.RELEASE_WAIT.getCode());
            projectTaskBatchRecord.setDelFlag(1);
            projectTaskBatchRecordService.updateStatusToSuspend(projectTaskBatchRecord);
            ProjectTaskBatch projectTaskBatch = new ProjectTaskBatch();
            projectTaskBatch.setProjectTaskId(releaseDelpoyRecord.getProjectTaskId());
            projectTaskBatch.setDeployStatus(PublishStatusEnum.RELEASE_WAIT.getCode());
            projectTaskBatch.setDelFlag(1);
            projectTaskBatchService.updateStatusToSuspend(projectTaskBatch);
        }

    }

    private HashMap<String, Integer> countAppCode(List<DeployPlanVo> deployPlanVoList) {
        HashMap<String, Integer> countMap = new HashMap<>();
        for (DeployPlanVo deployPlanVo : deployPlanVoList) {
            if (countMap.get(deployPlanVo.getAppCode()) == null) {
                countMap.put(deployPlanVo.getAppCode(), 1);
            } else {
                int count = countMap.get(deployPlanVo.getAppCode()) + 1;
                countMap.put(deployPlanVo.getAppCode(), count);
            }
        }
        return countMap;
    }

    private List<String> convertToPlanList(String ips, int total, int count) {
        List<String> list = new ArrayList<>();
        String[] ipsArray = ips.split(",");
        if (count <= 1) {
            for (int i = 0; i < ipsArray.length; i++) {
                list.add(ipsArray[i]);
            }
        } else {
            if (ipsArray.length > 2 && ipsArray.length > (int) Math.floor(total / 2)) {
                String ip = "";
                String ip1 = "";
                for (int i = 0; i < (int) Math.floor(ipsArray.length / 2); i++) {
                    ip += ipsArray[i] + ",";
                }
                for (int i = (int) Math.floor(ipsArray.length / 2); i < ipsArray.length; i++) {
                    ip1 += ipsArray[i] + ",";
                }
                list.add(ip);
                list.add(ip1);
            } else {
                list.add(ips);
            }
        }
        return list;
    }

    private DeployPlanVo convertTo(DeployPlan deployPlan) {
        DeployPlanVo deployPlanVo = new DeployPlanVo();
        BeanUtils.copyProperties(deployPlan, deployPlanVo);
        if (Integer.valueOf(1).equals(deployPlan.getRollbackFlag())) {
            deployPlanVo.setDeployStatusDesc(PublishStatusEnum.convertRelease2RollbackDesc(deployPlan.getDeployStatus()));
        } else {
            deployPlanVo.setDeployStatusDesc(PublishStatusEnum.getValueByCode(deployPlan.getDeployStatus()));
        }
        PlanMachineMapping condition = new PlanMachineMapping();
        condition.setPlanId(deployPlan.getId());
        condition.setDelFlag(1);
        condition.setRollbackFlag(deployPlan.getRollbackFlag());
        List<PlanMachineMapping> planMachineMappingList = planMachineMappingService.listByCondition(condition);
        if (planMachineMappingList == null || planMachineMappingList.size() <= 0) {
            logger.info("deployPlan no planMachineMapping , param = {}", deployPlan.toString());
            deployPlanVo.setPlanMachineMappings(new ArrayList<>());
            return deployPlanVo;
        }
        List<PlanMachineMappingVo> planMachineMappingVoList = planMachineMappingList.stream().map(planMachineMapping -> {
            PlanMachineMappingVo planMachineMappingVo = covertTo(planMachineMapping);
            return planMachineMappingVo;
        }).collect(Collectors.toList());
        deployPlanVo.setPlanMachineMappings(planMachineMappingVoList);
        return deployPlanVo;
    }

    private PlanMachineMappingVo covertTo(PlanMachineMapping planMachineMapping) {
        PlanMachineMappingVo planMachineMappingVo = new PlanMachineMappingVo();
        BeanUtils.copyProperties(planMachineMapping, planMachineMappingVo);
        if (Integer.valueOf(1).equals(planMachineMapping.getRollbackFlag())) {
            planMachineMappingVo.setDeployStatusDesc(PublishStatusEnum.convertRelease2RollbackDesc(planMachineMapping.getDeployStatus()));
        } else {
            planMachineMappingVo.setDeployStatusDesc(PublishStatusEnum.getValueByCode(planMachineMapping.getDeployStatus()));
        }
        return planMachineMappingVo;
    }

    private ResultVo modifyMachineStatus(String appCode, String groupName, String ip, String tag, Integer status) {
        Map<String, Object> map = new HashMap<>();
        map.put("appCode", appCode);
        map.put("groupName", groupName);
        map.put("hostIp", ip);
        map.put("tag", tag);
        map.put("status", status);
        Optional<String> optional = HTTP_POOL_CLIENT.postJson(modifyMachineRemote, JSONObject.toJSONString(map));
        String response = optional.isPresent() ? optional.get() : "";
        return (ResultVo) JSON.parseObject(response, ResultVo.class);
    }

    private ResultVo getServerCount(String appCode, String groupName) {
        Map<String, Object> map = new HashMap<>();
        map.put("appCode", appCode);
        map.put("groupName", groupName);
        Optional<String> optional = HTTP_POOL_CLIENT.postJson(getServerCountRemote, JSONObject.toJSONString(map));
        String response = optional.isPresent() ? optional.get() : "";
        return (ResultVo) JSON.parseObject(response, ResultVo.class);
    }

    /*以下为测试http接口*/
    @PostMapping("/publishTest")
    @ResponseBody
    public ResultVo publishTest(@RequestBody BetaDeployParam betaDeployParam) {
        try {
            String logOath = betaLogPath(betaDeployParam);
            publishService.betaPublish(betaDeployParam, logOath);
            return ResultVo.Builder.SUCC();
        } catch (Exception e) {
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("4000", "系统异常");
        }
    }

    @PostMapping("/deployTest")
    @ResponseBody
    public ResultVo deployTest(@RequestBody BetaDelpoyRecord betaDelpoyRecord) {
        try {
            deploy(betaDelpoyRecord, null, "dinglu");
            return ResultVo.Builder.SUCC();
        } catch (Exception e) {
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("4000", "系统异常");
        }
    }

    @PostMapping("/releasePublishTest")
    @ResponseBody
    public ResultVo releasePublishTest() {
        ReleaseDelpoyRecord releaseDelpoyRecord = releaseDelpoyRecordService.getByProjectId(6);
        ProjectTaskBatchRecord projectTaskBatchRecord = projectTaskBatchRecordService.getFirstBatchByRecord(19);
        releaseDeploy(releaseDelpoyRecord, projectTaskBatchRecord, "dinglu", null);
        return ResultVo.Builder.SUCC();
    }

    /**
     * 保险校验接口
     *
     * @param publishParam 发布计划列表
     * @return
     */
    @PostMapping("/check")
    @ResponseBody
    public ResultVo check(@RequestBody DeployPlanParam publishParam) {
        try {
            logger.debug("check deploy plan , param = {}", publishParam);

            List<DeployPlan> deployPlanList = publishParam.getStepList();
            if (CollectionUtils.isEmpty(deployPlanList)) {
                logger.error("check deploy plan fail , 参数错误 ， param = {}", publishParam);
                return ResultVo.Builder.FAIL().initErrCodeAndMsg("2000", "参数错误，发布计划列表为空");
            }

            StringBuilder buffer = new StringBuilder();
            int sequence = 0;
            for (DeployPlan deployPlan : deployPlanList) {
                Integer rollbackFlag = deployPlan.getRollbackFlag();
                boolean isRollback = Integer.valueOf(1).equals(rollbackFlag);
                // 批次号
                sequence++;
                int seq = deployPlan.getSequenece();
                if (seq == 0) {
                    seq = sequence;
                }
                // 1. 验证批次中机器使用数量是否小于存活机器数量的一半，如果不小于（批次使用机器数*2 >= 机器总数量）触发保险

                List<PlanMachineMapping> planMachineMappingList = deployPlan.getPlanMachineMappings();
                if (CollectionUtils.isEmpty(planMachineMappingList)) {
                    logger.error("deploy plan machine mapping is not fount in sequence {} , 参数错误 , deployPlan = {}", seq, deployPlan.toString());
                    return ResultVo.Builder.FAIL().initErrCodeAndMsg("2000", "参数错误，第" + seq + "批次机器分配列表为空。");
                }

                StringBuilder stepBuffer = new StringBuilder();

                StringBuilder checkMachineCountBuffer = this.checkMachineCount(deployPlan.getAppCode(), deployPlan.getAppGroup(), planMachineMappingList);
                if (checkMachineCountBuffer != null) {
                    stepBuffer.append(checkMachineCountBuffer);
                }

                // 2. 验证应用的btag和发布应用的btag是否一致
                ProjectTask projectTask = projectTaskService.getById(deployPlan.getProjectTaskId());
                if (projectTask == null) {
                    logger.info("应用记录不存在");
                    // 无法找到应用
                } else if (projectTask.getDelFlag() == 0) {
                    logger.info("应用已经被删除");
                    // 应用已经被删除
                } else if (!isRollback) {
                    String appBtag = projectTask.getAppBtag();
                    String planBtag = deployPlan.getAppBtag();
                    if (planBtag != null) {
                        if (!deployPlan.getAppBtag().equals(appBtag)) {
                            // error
                            stepBuffer.append("应用的发布Btag ").append(appBtag);
                            stepBuffer.append(" 与当前发布的Btag ").append(planBtag);
                            stepBuffer.append(" 不一致，代码可能有修改。\\r\\n");
                        } else if (PublishStatusEnum.RELEASE_SUCCESS.getCode().equals(deployPlan.getDeployStatus())) {
                            stepBuffer.append("该批次已经发布成功，且BTag没有发生变化，不会再次发布。\r\n");
                        }
                    }
                }
                if (stepBuffer.length() > 0) {
                    buffer.append("第").append(seq).append("批次，");
                    buffer.append(" 应用 ").append(deployPlan.getAppCode());
                    buffer.append(" 在分组 ").append(deployPlan.getAppGroup());
                    buffer.append(" 中存在风险：\\r\\n");
                    buffer.append(stepBuffer);
                    buffer.append("\\r\\n");
                }
            }

            if (buffer.length() > 0) {
                buffer.insert(0, "校验发布计划时发现以下风险：\\r\\n");
            }
            return ResultVo.Builder.SUCC().initSuccData(buffer.toString());
        } catch (Exception e) {
            logger.error("save deploy plan exception , Exception = {}", e);
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("4000", "系统异常");
        }
    }

    /**
     * 验证 一个应用单批次发布机器的数量不能超过该应用总机器数的50%
     *
     * @param appCode                应用App Code
     * @param appGroup               分组名称
     * @param planMachineMappingList 该批次中指派的机器列表
     * @return
     */
    private StringBuilder checkMachineCount(String appCode, String appGroup, List<PlanMachineMapping> planMachineMappingList) {
        int stepMachineCount = planMachineMappingList.size();
        // 只有单批次机器数量大于2时，才进行机器数量验证
        if (stepMachineCount > 2) {
            int total = this.getProjectTaskServerCount(appCode, appGroup);
            if (stepMachineCount * 2 >= total) {
                // error
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("该批次发布的机器数量为：").append(stepMachineCount).append("，");
                stringBuilder.append("该应用机器的总数量为：").append(total).append("。");

                StringBuilder ips = new StringBuilder();
                planMachineMappingList.stream().forEach(planMachineMapping -> {
                    String ip = planMachineMapping.getServiceIps();
                    if (ips.length() > 0) {
                        ips.append(",");
                    }
                    ips.append(ip);
                });

                List<String> ipList = this.convertToPlanList(ips.toString(), total, 2);
                if (!ipList.isEmpty()) {
                    stringBuilder.append("单批发布机器数量必须小于总机器数量的50%，建议按照下列IP组合进行批次拆分：\\r\\n");
                    ipList.stream().forEach(ret -> {
                        stringBuilder.append(ret).append("\\r\\n");
                    });
                }
                return stringBuilder;
            }
        }
        return null;
    }

    /**
     * 根据app code和分组信息获当前应用在该分组下的机器数量
     *
     * @param appCode   app code
     * @param groupName 分组信息
     * @return 当前应用在该分组中机器的余量
     */
    private int getProjectTaskServerCount(String appCode, String groupName) {
        try {
            Map<String, String> json = new HashMap<>();
            json.put("appCode", appCode);
            json.put("groupName", groupName);
            Optional<String> option = HTTP_POOL_CLIENT.postJson(this.appServerGetServerCountRemote, JSON.toJSONString(json));
            if (option != null) {
                try {
                    ResultVo resultVo = JSON.parseObject(option.get(), ResultVo.class);
                    return (int) resultVo.getData();
                } catch (Exception e) {
                    logger.error("接收参数错误: {}", e);
                    return 0;
                }
            }
        } catch (Exception e) {

        }
        return 0;
    }


    public static void main(String[] args) throws UnsupportedEncodingException {
    }

    /**
     * 检查指定IP在该应用下是否可用
     * TODO 添加批量验证接口
     *
     * @param projectId 项目ID
     * @param ips       IP地址集合，多值用逗号分割
     * @return 如果存在不能使用的IP，返回错误信息；如果IP全部可用，返回空字符串
     */
    private String betaCheckIps(Integer projectId, String ips) {
        String[] ipList = ips.split(",");
        StringBuilder builder = new StringBuilder();

        Map<String, Object> json = new HashMap<>();
        json.put("projectId", projectId);
        for (String ip : ipList) {
            json.put("ip", ip);
            Optional<String> option = HTTP_POOL_CLIENT.postJson(this.checkIpCanUse, JSON.toJSONString(json));
            if (option != null) {
                try {
                    ResultVo resultVo = JSON.parseObject(option.get(), ResultVo.class);
                    if (!"0".equals(resultVo.getCode())) {
                        builder.append("服务器 ").append(ip).append(" 检查失败， ").append(resultVo.getMessage());
                    } else {
                        Map<String, Object> map = (Map<String, Object>) JSON.parseObject((String) resultVo.getData());
                        Integer canUse = (Integer) map.get("canUse");
                        if (!Integer.valueOf(1).equals(canUse)) {
                            String projectName = (String) map.get("projectName");
                            builder.append("服务器 ").append(ip).append(" 被项目 ").append(projectName).append(" 占用");
                        }
                    }
                } catch (Exception e) {
                    logger.error("检查服务器占用异常: ", e);
                }
            }
        }
        return builder.toString();
    }

    /**
     * 锁定指定服务器
     *
     * @param ip        服务器IP
     * @param projectId 项目ID
     */
    private boolean lockServer(String ip, Integer projectId) {
        Map<String, Object> json = new HashMap<>();
        json.put("ip", ip);
        json.put("projectId", projectId);
        try {
            Optional<String> option = HTTP_POOL_CLIENT.postJson(this.lockServer, JSON.toJSONString(json));
            if (option.isPresent()) {
                ResultVo resultVo = JSON.parseObject(option.get(), ResultVo.class);
                if (!"0".equals(resultVo.getCode())) {
                    logger.error("锁定服务器异常， {}", resultVo.getMessage());
                    return false;
                }
            }
        } catch (Exception e) {
            logger.error("锁定服务器异常", e);
        }
        return false;
    }

    private void releaseServer(String ip) {
        Map<String, Object> json = new HashMap<>();
        json.put("ip", ip);
        Optional<String> option = HTTP_POOL_CLIENT.postJson(this.releaseServer, JSON.toJSONString(json));
    }

    /**
     * 根据项目ID解锁服务器
     *
     * @param projectId
     */
    private void releaseServerByProjectId(Integer projectId) {
        Map<String, Object> json = new HashMap<>();
        json.put("projectId", projectId);
        HTTP_POOL_CLIENT.postJson(this.releaseServerByProjectId, JSON.toJSONString(json));
    }
}
