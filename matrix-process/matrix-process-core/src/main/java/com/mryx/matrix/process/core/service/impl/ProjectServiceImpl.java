package com.mryx.matrix.process.core.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.mryx.common.utils.HttpClientUtil;
import com.mryx.common.utils.HttpPoolClient;
import com.mryx.common.utils.StringUtils;
import com.mryx.matrix.codeanalyzer.domain.CodeScanResult;
import com.mryx.matrix.common.domain.ResultVo;
import com.mryx.matrix.process.core.dao.ProjectDao;
import com.mryx.matrix.process.core.service.*;
import com.mryx.matrix.process.core.utils.GitlabUtil;
import com.mryx.matrix.process.core.utils.TreeUtil;
import com.mryx.matrix.process.domain.Issue;
import com.mryx.matrix.process.domain.Project;
import com.mryx.matrix.process.domain.ProjectRecord;
import com.mryx.matrix.process.domain.User;
import com.mryx.matrix.process.dto.*;
import com.mryx.matrix.process.enums.ProjectTypeEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

//import com.mryx.matrix.publish.domain.ProjectTask;


/**
 * 应用发布工单表
 *
 * @author zhaoxl
 * @email zhaoxl02@missfresh.cn
 * @date 2018-09-04 16:21
 **/
@Service("projectService")
public class ProjectServiceImpl implements ProjectService {

    private static Logger logger = LoggerFactory.getLogger(ProjectServiceImpl.class);

    private static final String FEATURE = "feature_";
    private static final String HOTFIX = "hotfix_";

    private static final HttpPoolClient HTTP_POOL_CLIENT = HttpClientUtil.create(2000, 2000, 5, 5, 1, 500);

    @Resource
    private ProjectDao projectDao;

    @Resource
    private GmsAccessTokenService gmsAccessTokenService;

    @Resource
    private ProjectRecordService projectRecordService;

    @Resource
    private UserService userService;

    @Resource
    private CodeScanResultService codeScanResultService;

    @Resource
    private CodeReviewResultService codeReviewResultService;

    @Value("${appDetail_remote}")
    private String appDetailRemote;

    @Value("${appTree_remote}")
    private String appTreeRemote;

    @Value("${taskSave_remote}")
    private String taskSaveRemote;

    @Value("${taskUpdate_remote}")
    private String taskUpdateRemote;

    @Value("${taskGetById_remote}")
    private String taskGetByIdRemote;

    @Value("${taskGetByproId_remote}")
    private String taskGetByproIdRemote;

    @Value("${code_scan_remote}")
    private String codeScanRemote;

    @Value("${codeReviewCreate_remote}")
    private String codeReviewCreateRemote;

    @Value("${app_remote}")
    private String appRemote;

    @Value("${getIssueList}")
    private String getIssueList;

    @Override
    public ProjectDTO getById(Integer id) {
        Project project = projectDao.getById(id);

        List<ProjectTaskDto> allprojectTasks = getTasksByProjectId(id);
//		List<ProjectTask> allprojectTasks = projectTaskService.getByPrpjectId(id);
        List<ProjectTaskDto> projectTasks = new ArrayList<>();
        for (ProjectTaskDto projectTask : allprojectTasks) {
            int delFlag = projectTask.getDelFlag();
            if (delFlag != 0) {
                ProjectTaskDto projectTaskDtoDtO = new ProjectTaskDto();
                Map<String, Object> mapInfo = new HashMap();
                String appCode = projectTask.getAppCode();
                mapInfo.put("appCode", appCode);
                mapInfo.put("envCode", "prod");
                Optional<String> optional = HTTP_POOL_CLIENT.postJson(appDetailRemote, JSONObject.toJSONString(mapInfo));
                if (optional.isPresent()) {
                    JSONObject apps = (JSONObject) JSONObject.parse(optional.get());
                    if (apps.get("data") != "" && apps.get("data") != null) {
                        AppInfoDto appInfoDto = JSON.parseObject(apps.get("data").toString(), AppInfoDto.class);
                        List<GroupInfoDto> groupInfos = appInfoDto.getGroupInfo();

                        BeanUtils.copyProperties(projectTask, projectTaskDtoDtO);
                        projectTaskDtoDtO.setGroupInfo(groupInfos);
                        if (appInfoDto.getGit() != null || appInfoDto.getGit() != "") {
                            projectTaskDtoDtO.setAppGitAddress(appInfoDto.getGit());
                        } else {
                            projectTaskDtoDtO.setAppGitAddress("");
                        }
                        projectTaskDtoDtO.setAppInfoDto(appInfoDto);
                    }
                }

                try {
                    mapInfo.put("envCode", "stress");
                    optional = HTTP_POOL_CLIENT.postJson(appDetailRemote, JSONObject.toJSONString(mapInfo));
                    if (optional.isPresent()) {
                        List<String> stressIpList = new ArrayList<>();
                        JSONObject apps = (JSONObject) JSONObject.parse(optional.get());
                        if (apps.get("data") != "" && apps.get("data") != null) {
                            AppInfoDto appInfoDto = JSON.parseObject(apps.get("data").toString(), AppInfoDto.class);
                            List<GroupInfoDto> groupInfos = appInfoDto.getGroupInfo();
                            if (!CollectionUtils.isEmpty(groupInfos)) {
                                groupInfos.stream().forEach(groupInfoDto -> {
                                    List<ServerResourceDto> serverInfos = groupInfoDto.getServerInfo();
                                    if (!CollectionUtils.isEmpty(serverInfos)) {
                                        serverInfos.stream().forEach(serverResourceDto -> {
                                            stressIpList.add(serverResourceDto.getHostIp());
                                        });
                                    }
                                });
                            }
                        }
                        projectTaskDtoDtO.setStressIpList(stressIpList);
                    }
                } catch (Exception e) {
                    logger.error("获取压测IP错误: " + e.getMessage());
                }
                /*Map<String, Object> mapInfo2 = new HashMap();
                mapInfo2.put("appCode", appCode);
                mapInfo2 = SignUtils.addSignParam(mapInfo2);
                Optional optional2 = HTTP_POOL_CLIENT.postJson(appRemote, JSONObject.toJSONString(mapInfo2));
                if (optional2.isPresent()) {
                    JSONObject apps = (JSONObject) JSONObject.parse(optional2.get().toString());
                    if ("fail".equals(apps.get("ret"))) {
                        logger.error("调用失败: {}", appRemote + JSON.toJSONString(optional));
                    }
                    if (apps.get("data") == "" || apps.get("data") == null) {
                        logger.error("data为空: {}");
                    }
                    JSONObject appdatas = (JSONObject) JSONObject.parse(apps.get("data").toString());
                    String rows = appdatas.getString("rows");
                    JSONArray jsonArray = JSONArray.parseArray(rows);
                    for (Object object : jsonArray.toArray()) {
                        Map m = (Map) object;
                        if (m.get("git") != null) {
                            String gitAddress = m.get("git").toString();
                            projectTaskDtoDtO.setAppGitAddress(gitAddress);
                        }
                    }
                }*/

                getCodeScanResult(projectTask, projectTaskDtoDtO);

                getCodeReviewResult(projectTask, projectTaskDtoDtO);
                projectTasks.add(projectTaskDtoDtO);
                logger.info("projectTaskDtoDtO {}", projectTaskDtoDtO.toString());
            }
        }
        ProjectDTO projectDto = new ProjectDTO();
        logger.info("project 对象内容为：{}", project.toString());
        BeanUtils.copyProperties(project, projectDto);
        projectDto.setProjectTasks(projectTasks);
        projectDto.setIsAudit(project.getAuditStatus());
        projectDto.setBusinessLines(project.getBusinessLines());
        Issue issue = new Issue();
        issue.setProjectId(id);
        try {
            Optional<String> optional = HTTP_POOL_CLIENT.postJson(getIssueList, JSONObject.toJSONString(issue));
            if (optional.isPresent()) {
                JSONObject jsonObject = (JSONObject) JSONObject.parse(optional.get());
                List<Issue> issueList = JSONArray.parseArray(jsonObject.get("data").toString(), Issue.class);
                projectDto.setIssueList(issueList);
            }
        } catch (Exception e) {
            logger.error("获取需求失败", e);
        }

        return projectDto;
    }

    private void getCodeReviewResult(ProjectTaskDto projectTask, ProjectTaskDto projectTaskDtoDtO) {
        try {
            String codeReviewLinkUrl = codeReviewResultService.getCodeReviewLinkUrl(projectTask.getId());
            logger.info("codeReviewLinkUrl {}", codeReviewLinkUrl);
            if (codeReviewLinkUrl != null && codeReviewLinkUrl != "") {
                projectTaskDtoDtO.setCodeReviewResultUrl(codeReviewLinkUrl);
            }
        } catch (NullPointerException e) {
            logger.error("项目应用代码评审结果为空 {}", e);
        }
    }

    private void getCodeScanResult(ProjectTaskDto projectTask, ProjectTaskDto projectTaskDtoDtO) {
        try {
            //TODO 拼数据 codeScan codeReview
            Map<String, String> codeScanResult = codeScanResultService.getCodeScanResult(projectTask.getId());
            if (codeScanResult == null) {
                logger.error("没有获取到本次代码扫描结果");
            } else {
                //TODO 有值才返回 先判断有没有值
                String codeScanResultDesc = "正在进行代码扫描，请稍等!";
                CodeScanResultDataDto codeScanResultCurrKpi = new CodeScanResultDataDto();
                CodeScanResultDataDto codeScanResultPrevKpi = new CodeScanResultDataDto();
                codeScanResultCurrKpi.setBlocker(Integer.valueOf(codeScanResult.get("blocker")));
                codeScanResultCurrKpi.setCritical(Integer.valueOf(codeScanResult.get("critical")));
                codeScanResultCurrKpi.setMajor(Integer.valueOf(codeScanResult.get("major")));
                codeScanResultCurrKpi.setMinor(Integer.valueOf(codeScanResult.get("minor")));
                codeScanResultCurrKpi.setInfo(Integer.valueOf(codeScanResult.get("info")));
                if ("0".equals(String.valueOf(codeScanResult.get("status")))) {
                    logger.info("代码扫描成功");
                    Map<String, String> masterCodeScanResult = codeScanResultService.getMasterCodeScanResult(projectTask.getId());
                    if (masterCodeScanResult == null) {
                        codeScanResultDesc = "应用未发布过,代码扫描成功";
                    } else {
                        codeScanResultDesc = "代码扫描成功";
                        codeScanResultPrevKpi.setBlocker(Integer.valueOf(masterCodeScanResult.get("blocker")));
                        codeScanResultPrevKpi.setCritical(Integer.valueOf(masterCodeScanResult.get("critical")));
                        codeScanResultPrevKpi.setMajor(Integer.valueOf(masterCodeScanResult.get("major")));
                        codeScanResultPrevKpi.setMinor(Integer.valueOf(masterCodeScanResult.get("minor")));
                        codeScanResultPrevKpi.setInfo(Integer.valueOf(masterCodeScanResult.get("info")));
                    }
                    projectTaskDtoDtO.setCodeScanResultUrl(codeScanResult.get("code_scan_result_url"));
                } else if ("1".equals(String.valueOf(codeScanResult.get("status")))) {
                    logger.info("代码扫描失败");
                    codeScanResultDesc = "代码扫描失败";
                    //TODO 测试代码Review
                } else {
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(new Date());
                    long nowTime = calendar.getTimeInMillis();
                    logger.info("nowTime {}", nowTime);
                    Object codeScanTime = codeScanResult.get("code_scan_time");
                    String codeTime = codeScanTime.toString();
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    Date createScanTime = null;
                    try {
                        createScanTime = sdf.parse(codeTime);
                    } catch (ParseException e) {
                        logger.error("时间转换异常");
                    }
                    calendar.setTime(createScanTime);
                    long createTime = calendar.getTimeInMillis();
                    logger.info("codeScanTime {}", createTime);
                    if (nowTime - createTime > 1200000) {
                        logger.warn("代码扫描超时");
                        //TODO 失败
                        CodeScanResult scanResult = new CodeScanResult();
                        Object pId = codeScanResult.get("id");
                        scanResult.setId(Integer.valueOf(pId.toString()));
                        scanResult.setStatus(0);
                        int flag = codeScanResultService.updateCodeScanResultStatus(scanResult);
                        logger.info("codescan >20分钟 updateCodeScanResultStatus flag = {}", flag);
                        if (flag > 0) {
                            codeScanResultDesc = "代码扫描失败";
                        }
                    }
                }
                projectTaskDtoDtO.setCodeScanResultDesc(codeScanResultDesc);
                projectTaskDtoDtO.setCodeScanResultPrevKpi(codeScanResultPrevKpi);
                projectTaskDtoDtO.setCodeScanResultCurrKpi(codeScanResultCurrKpi);
                logger.info("codeScanResultPrevKpi is {},codeScanResultCurrKpi is {}", codeScanResultPrevKpi, codeScanResultCurrKpi);
            }
        } catch (Exception e) {
            logger.error("项目应用代码扫描结果为空,{}", e);
        }
    }

    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public int insert(Project project) {
        project.setBusinessLinesDb(project.getBusinessLines());
        int i = projectDao.insert(project);
        List<ProjectTaskDto> projectTasks = project.getProjectTasks();
        if (null != projectTasks) {
            for (ProjectTaskDto projectTask : projectTasks) {
                //根据 app code 查询所有信息，创建分支，存入分支信息
                projectTask.setProjectId(project.getId());
                Date now = new Date();
                projectTask.setCreateTime(now);
                i = createAppBranch(project, projectTask, "create");
                if (i == 0) {
                    throw new RuntimeException("应用创建失败");
                }
            }
        }
        return i;
    }

    @Override
    public int updateById(Project project) {
        dealProjectTime(project);

        project.setBusinessLinesDb(project.getBusinessLines());

        int i = projectDao.updateById(project);
        i = createProjectRecord(project, "");

        List<ProjectTaskDto> projectTasks = project.getProjectTasks();

        List<ProjectTaskDto> oldprojectTasks = getTasksByProjectId(project.getId());

//		List<ProjectTask> oldprojectTasks=projectTaskService.getByPrpjectId(project.getId());

        Map<Integer, ProjectTaskDto> tempmap = new HashMap();
        for (ProjectTaskDto projectTask : oldprojectTasks) {
            tempmap.put(projectTask.getId(), projectTask);
        }
        List<Integer> tasksIds = new ArrayList();
        for (ProjectTaskDto projectTask : oldprojectTasks) {
            if (null != projectTask) {
                Integer workerid = projectTask.getId();
                tasksIds.add(workerid);
            }
        }
        List<Integer> removeTaskIds = new ArrayList();

        if (null != projectTasks) {
            for (ProjectTaskDto projectTask : projectTasks) {
                //原有数据更新
                if (projectTask.getId() != null && projectTask.getProjectId() != null) {
                    ProjectTaskDto oldprojectTask = getTasksByTaskId(projectTask);
//					ProjectTask oldprojectTask=projectTaskService.getById(projectTask.getId());
                    //应用名称发生变化，只重新创建分支
                    if (!oldprojectTask.getAppCode().equals(projectTask.getAppCode()) || !(oldprojectTask.getAppBranch().equals(projectTask.getAppBranch()))) {
                        i = createAppBranch(project, projectTask, "appUpdate");
                        if (i == 0) {
                            return i;
                        }
                    }
                    String oldRTag = oldprojectTask.getAppRtag();
                    String newRTag = projectTask.getAppRtag();

                    if (newRTag != null && !newRTag.equals(oldRTag)) {
                        String gitAddress = projectTask.getAppGitAddress();
                        boolean exist = GitlabUtil.tagExist(gitAddress, newRTag, true);
                        if (!exist) {
                            throw new RuntimeException("保存失败：RTag 不存在。");
                        }

                    }

                    //对应用名称和应用负责人进行更新
                    i = updateTask(projectTask);
//					i = projectTaskService.updateById(projectTask);
                    //removeTaskIds存储所有被删除了的数据
                    removeTaskIds.add(projectTask.getId());
                }
                //新增数据保存
                if (projectTask.getId() == null) {
                    projectTask.setProjectId(project.getId());
                    Date now = new Date();
                    projectTask.setCreateTime(now);
                    //根据appcode查询所有信息，创建分支，新增分支信息，project不会重新插入信息，
                    i = createAppBranch(project, projectTask, "createApp");
                    if (i == 0) {
                        return i;
                    }
                }
            }
            tasksIds.removeAll(removeTaskIds);
            for (Integer removeWorkId : tasksIds) {
                if (tempmap.containsKey(removeWorkId)) {
                    //已删除数据进行处理
                    ProjectTaskDto projectTask = tempmap.get(removeWorkId);
                    projectTask.setId(removeWorkId);
                    projectTask.setDelFlag(0);
                    i = updateTask(projectTask);
//					i = projectTaskService.updateById(projectTask);

                }
            }

        }
        return i;
    }

    @Override
    public int pageTotal(Project project) {
        return projectDao.pageTotal(project);
    }

    @Override
    public List<Project> listPage(Project project) {
        return projectDao.listPage(project);
    }

    @Override
    public Integer countProjectDTOTotal(ProjectDTO projectdto) {
        return projectDao.countProjectDTOTotal(projectdto);
    }

    ;

    @Override
    public List<ProjectDTO> listProjectDTOPage(ProjectDTO projectdto) {
        return projectDao.listProjectDTOPage(projectdto);
    }

    ;

    @Override
    public List<Project> listByCondition(Project project) {
        return projectDao.listByCondition(project);
    }

    @Override
    public List<AppListsDto> dealAppcodeInfo(Long deptId, String appCode) {
        Map<String, Object> mapInfo = new HashMap();
        mapInfo.put("deptId", deptId);
        Optional<String> appList = HTTP_POOL_CLIENT.postJson(appTreeRemote, JSONObject.toJSONString(mapInfo));
        List<AppListsDto> appCodeAndNameDtoList = new ArrayList<>();

        if (appList.isPresent()) {
            JSONObject apps = (JSONObject) JSONObject.parse(appList.get().toString());
            DeptAppTree tree = (DeptAppTree) JSON.parseObject(apps.get("data").toString(), DeptAppTree.class);
            TreeUtil.merge(tree, appCodeAndNameDtoList, appCode);
        }
        return appCodeAndNameDtoList;
    }

    @Override
    public AppInfoDto getAppDetailInfo(Map<String, Object> mapInfo) {
        Optional<String> optional = HTTP_POOL_CLIENT.postJson(appDetailRemote, JSONObject.toJSONString(mapInfo));
        AppInfoDto appInfoDto = null;
        if (optional.isPresent()) {
            JSONObject apps = (JSONObject) JSONObject.parse(optional.get().toString());
            if (apps.get("data") != "" && apps.get("data") != null) {
                appInfoDto = (AppInfoDto) JSON.parseObject(apps.get("data").toString(), AppInfoDto.class);
            }
        }
        return appInfoDto;

    }

    @Override
    public Project getProjectById(Integer id) {
        return projectDao.getById(id);
    }

    @Override
    public ResultVo createCodeScan(Project project) {
        logger.info("createCodeScan project = {}", project);
        if (project == null || project.getProjectTasks() == null || project.getProjectTasks().isEmpty()) {
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("1", "参数错误");
        }
        try {
            Optional<String> codeScanResult = HTTP_POOL_CLIENT.postJson(codeScanRemote, JSONObject.toJSONString(project.getProjectTasks()));
            logger.info("调用创建代码扫描接口的返回值为{}" + codeScanResult.get());
            if (codeScanResult.isPresent()) {
                JSONObject jsonObject = JSONObject.parseObject(codeScanResult.get());
                if (jsonObject != null && "0".equals(jsonObject.getString("code"))) {
                    return ResultVo.Builder.SUCC();
                }
            }
        } catch (Exception e) {
            logger.error("error ", e);
        }
        return ResultVo.Builder.FAIL().initErrCodeAndMsg("1", "创建代码扫描任务失败！");
    }

    @Override
    public ResultVo createCodeReview(Project project) {
        logger.info("createCodeReview project = {}", project);
        if (project == null || project.getProjectTasks() == null || project.getProjectTasks().isEmpty()) {
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("1", "参数错误");
        }
        try {
            Optional<String> codeScanResult = HTTP_POOL_CLIENT.postJson(codeReviewCreateRemote, JSONObject.toJSONString(project.getProjectTasks()));
            logger.info("调用创建代码审核接口的返回值为{}" + codeScanResult.get());
            if (codeScanResult.isPresent()) {
                JSONObject jsonObject = JSONObject.parseObject(codeScanResult.get());
                if (jsonObject != null && "0".equals(jsonObject.getString("code"))) {
                    return ResultVo.Builder.SUCC();
                }
            }
        } catch (Exception e) {
            logger.error("error ", e);
        }
        return ResultVo.Builder.FAIL().initErrCodeAndMsg("1", "创建代码审核任务失败！");
    }

    private int createProjectRecord(Project project, String flag) {
        String accessToken = project.getAccessToken();
        Map<String, Object> userMap = gmsAccessTokenService.verifyAccessTokenByUserId(accessToken);
        logger.info("verifyAccessTokenByUserId.retMap={}", userMap);
        String userId = userMap.get("userId").toString();
        User user = userService.getById(Integer.valueOf(userId));
        String userName = "";
        if (user != null) {
            userName = user.getUserName();
        }
        ProjectRecord projectRecord = new ProjectRecord();
        if ("creat".equals(flag)) {
            projectRecord.setCreateUser(userName);
        } else {
            projectRecord.setUpdateUser(userName);
        }
        projectRecord.setProjectId(project.getId());
        projectRecord.setProjectStatus(project.getProjectStatus());
        Date now = new Date();
        projectRecord.setCreateTime(now);
        projectRecord.setUpdateTime(now);
        projectRecord.setProjectName(project.getProjectName());
        int i = projectRecordService.insert(projectRecord);
        return i;
    }

    private void dealProjectTime(Project project) {
        Integer projectStatus = project.getProjectStatus();
        Date now = new Date();
        if (1 == projectStatus) {
            project.setActualStartTime(now);
        }
        if (2 == projectStatus) {
            project.setActualTestTime(now);
        }
        if (3 == projectStatus) {
            project.setActualPublishTime(now);
        }

    }

    private int createAppBranch(Project project, ProjectTaskDto projectTask, String flag) {
        Map<String, Object> mapInfo = new HashMap();
        mapInfo.put("appCode", projectTask.getAppCode());
        mapInfo.put("envCode", "prod");

        Optional optional = HTTP_POOL_CLIENT.postJson(appDetailRemote, JSONObject.toJSONString(mapInfo));
//        Optional<String> optional = HTTP_POOL_CLIENT.postJson(appDetailRemote, JSONObject.toJSONString(mapInfo));
        if (optional.isPresent()) {
            JSONObject apps = (JSONObject) JSONObject.parse(optional.get().toString());
            if ("fail".equals(apps.get("ret"))) {
                logger.error("调用失败: {}", appDetailRemote + JSON.toJSONString(optional));
            }
            if (apps.get("data") == null || "".equals(apps.get("data"))) {
                logger.error("data为空: {}");
            }
            AppInfoDto appInfoDto = (AppInfoDto) JSON.parseObject(apps.get("data").toString(), AppInfoDto.class);

            String gitAddress = "";
            if (appInfoDto == null) {
                logger.info("不存在appCode,appCode={}", projectTask.getAppCode());
                return 0;
            }
            if (appInfoDto.getGit() != null && appInfoDto.getGit() != "") {
                gitAddress = appInfoDto.getGit();
            }
            if (gitAddress.equals("")) {
                logger.error("git地址不存在: {}");
                return 0;
            }

            String branchName = projectTask.getAppBranch();
            if (StringUtils.isEmpty(branchName)) {
                return 0;
            }

            // 如果分支后缀已经存在，则使用当前分支，不再创建新分支；否则根据规则，将当前分支名称作为后缀，创建新分支
            if (!GitlabUtil.exist(gitAddress, branchName)) {
                String branchNameFormat = generateBranch(project.getProjectType(), project.getId(), branchName);
                branchName = GitlabUtil.createBranch(gitAddress, branchNameFormat, "master");
            }
            if (StringUtils.isEmpty(branchName)) {
                return 0;
            }
            int i = 0;
            projectTask.setAppBranch(branchName);
            if ("create".equals(flag)) {
                Integer projectId = project.getId();
                projectTask.setProjectId(projectId);
                i = createProjectRecord(project, "create");
                Optional<String> optional2 = HTTP_POOL_CLIENT.postJson(taskSaveRemote, JSONObject.toJSONString(projectTask));
                JSONObject saveResults = (JSONObject) JSONObject.parse(optional2.get().toString());
                if ("fail".equals(saveResults.get("ret"))) {
                    logger.error("调用失败: {}", taskSaveRemote + JSON.toJSONString(optional2));
                    return 0;
                }
//				i=projectTaskService.insert(projectTask);
            } else if ("createApp".equals(flag)) {
                Integer projectId = project.getId();
                projectTask.setProjectId(projectId);
                Optional<String> optional2 = HTTP_POOL_CLIENT.postJson(taskSaveRemote, JSONObject.toJSONString(projectTask));
                JSONObject saveResults = (JSONObject) JSONObject.parse(optional2.get().toString());
                if ("fail".equals(saveResults.get("ret"))) {
                    logger.error("调用失败: {}", taskSaveRemote + JSON.toJSONString(optional2));
                    return 0;
                }
                i = 1;
            } else if ("appUpdate".equals(flag)) {
                i = 1;
            }

            return i;
        }
        return 0;
    }

    /**
     * 生成分支
     * <p>
     * 功能分支
     * 格式：feature_taskid_20180928_description
     * feature_TaskID_日期_用户名_功能描述
     * 修复分支
     * 格式：hotfix_taskid_20180928_description
     * hotfix_TaskID_日期_用户名_功能描述
     *
     * @param projectType
     * @param projectTaskId
     * @param branchDescription
     * @return
     */
    private String generateBranch(Integer projectType, Integer projectTaskId, String branchDescription) {
        StringBuffer branchNameBuffer = new StringBuffer();
        if (ProjectTypeEnum.OB.getCode().equals(projectType)) {
            branchNameBuffer.append(HOTFIX);
        } else {
            branchNameBuffer.append(FEATURE);
        }
        branchDescription.replaceAll("\\s*", "");
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        Date date = new Date();
        String temp = dateFormat.format(date);
        branchNameBuffer.append(projectTaskId).append("_").append(temp).append("_").append(branchDescription);
        return branchNameBuffer.toString();

    }

    private List<ProjectTaskDto> getTasksByProjectId(int id) {
        ProjectTaskDto projectTaskParam = new ProjectTaskDto();
        projectTaskParam.setProjectId(id);
        Optional<String> optional2 = HTTP_POOL_CLIENT.postJson(taskGetByproIdRemote, JSONObject.toJSONString(projectTaskParam));
        List<ProjectTaskDto> allprojectTasks = new ArrayList<>();
        if (optional2.isPresent()) {
            JSONObject saveResults = (JSONObject) JSONObject.parse(optional2.get().toString());
            if ("fail".equals(saveResults.get("ret"))) {
                logger.error("调用失败: {}", taskGetByproIdRemote + JSON.toJSONString(optional2));
            } else {
                JSONObject tasks = (JSONObject) JSONObject.parse(optional2.get().toString());
                if (tasks.get("data") != "" && tasks.get("data") != null) {
                    allprojectTasks = (List<ProjectTaskDto>) tasks.get("data");
                    allprojectTasks = (List<ProjectTaskDto>) JSON.parseArray(tasks.get("data").toString(), ProjectTaskDto.class);
                    logger.info("allprojectTasks: {}", JSON.toJSONString(allprojectTasks));
                }
            }
        }
        return allprojectTasks;
    }

    private ProjectTaskDto getTasksByTaskId(ProjectTaskDto projectTask) {
        Optional<String> optional2 = HTTP_POOL_CLIENT.postJson(taskGetByIdRemote, JSONObject.toJSONString(projectTask));
        ProjectTaskDto newprojectTask = new ProjectTaskDto();
        if (optional2.isPresent()) {
            JSONObject saveResults = (JSONObject) JSONObject.parse(optional2.get().toString());
            if ("fail".equals(saveResults.get("ret"))) {
                logger.error("调用失败: {}", taskGetByproIdRemote + JSON.toJSONString(optional2));
            } else {
                JSONObject tasks = (JSONObject) JSONObject.parse(optional2.get().toString());
                newprojectTask = (ProjectTaskDto) JSON.parseObject(tasks.get("data").toString(), ProjectTaskDto.class);
            }
        }

        return newprojectTask;

    }

    private int updateTask(ProjectTaskDto projectTask) {
        Optional<String> optional2 = HTTP_POOL_CLIENT.postJson(taskUpdateRemote, JSONObject.toJSONString(projectTask));
        int i = 0;
        if (optional2.isPresent()) {
            JSONObject saveResults = (JSONObject) JSONObject.parse(optional2.get().toString());
            if ("fail".equals(saveResults.get("ret"))) {
                logger.error("调用失败: {}", taskGetByproIdRemote + JSON.toJSONString(optional2));
            } else {
                i = 1;
            }
        }

        return 1;

    }


}
