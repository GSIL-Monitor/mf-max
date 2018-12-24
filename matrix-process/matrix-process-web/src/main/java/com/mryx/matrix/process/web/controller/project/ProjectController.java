package com.mryx.matrix.process.web.controller.project;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.mryx.common.utils.HttpClientUtil;
import com.mryx.common.utils.HttpPoolClient;
import com.mryx.common.utils.SignUtils;
import com.mryx.grampus.ccs.domain.OauthUser;
import com.mryx.matrix.common.domain.ResultVo;
import com.mryx.matrix.process.core.annotation.CcsPermission;
import com.mryx.matrix.process.core.ccs.CcsService;
import com.mryx.matrix.process.core.service.*;
import com.mryx.matrix.process.core.utils.ChineseCharacterUtil;
import com.mryx.matrix.process.domain.*;
import com.mryx.matrix.process.dto.ProjectTaskDto;
import com.mryx.matrix.process.dto.AppInfoDto;
import com.mryx.matrix.process.dto.AppListsDto;
import com.mryx.matrix.process.dto.ProjectDTO;
import com.mryx.matrix.process.enums.DelFlagEnum;
import com.mryx.matrix.process.enums.ProjectPriorityEnum;
import com.mryx.matrix.process.enums.ProjectProcessStatusEnum;
import com.mryx.matrix.process.enums.ProjectTypeEnum;
import com.mryx.matrix.process.web.param.BaseParam;
import com.mryx.matrix.process.web.param.GetDeptAppTreeByDeptIdParam;
import com.mryx.matrix.process.web.vo.*;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 项目Controller
 *
 * @author supeng
 * @date 2018/08/30
 */
@RestController
@RequestMapping("/api/project/")
public class ProjectController {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProjectController.class);
    private static final HttpPoolClient HTTP_POOL_CLIENT = HttpClientUtil.create(2000, 2000, 5, 5, 1, 500);

    @Autowired
    private UserLdapService userLdapService;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private ProjectRecordService projectRecordService;

    @Autowired
    private CcsService ccsService;

    @Autowired
    private UserService userService;

    @Value("${app_remote}")
    private String appRemote;

    @Value("${person_remote}")
    private String personRemote;

    @Value("${firstDept_remote}")
    private String firstDeptRemote;

    @Value("${appDetail_remote}")
    private String appDetailRemote;

    @Value("${code_scan_remote}")
    private String codeScanRemote;

    @Value("${codeReviewCreate_remote}")
    private String codeReviewCreateRemote;

    @Value("${getMyproject_remote}")
    private String getMyproject_remote;

    @Value("${associateIssue}")
    private String associateIssue;

    /*项目列表*/
    @CcsPermission("matrix:project:getProjectList")
    @RequestMapping(value = "/getProjectList", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    public ResultVo getProjectList(@RequestBody Project projectParam, HttpServletRequest request) {
        LOGGER.info("项目列表请求参数: {}", JSON.toJSONString(projectParam));
        projectParam.setDelFlag(DelFlagEnum.DEL_NO.getCode());
        try {
            String accessToken = projectParam.getAccessToken();
            OauthUser user = getUser(accessToken, request);

            String userName = user.getName();
            String userAccount = user.getAccount();
            projectParam.setQueryUserName(userName);
            projectParam.setQueryUserAccount(userAccount);

            if (Boolean.TRUE.equals(projectParam.getIsMine())) {
                Map<String, Object> map = new HashMap<>();
                map.put("publishUser", userAccount);
                Optional<String> ret = HTTP_POOL_CLIENT.postJson(getMyproject_remote, JSON.toJSONString(map));
                JSONObject object = (JSONObject) JSONObject.parse(ret.get().toString());
                String projectIds = (String) object.get("data");
                if (projectIds != null) {
                    List<String> idList = new ArrayList<>();
                    String[] projectIdArray = projectIds.split(",");
                    for (String id : projectIdArray) {
                        idList.add(id);
                    }
                    projectParam.setQueryProjectIds(idList);
                }
            }

            Integer total = projectService.pageTotal(projectParam);
            Pagination<ProjectVO> pagination = new Pagination<>();
            pagination.setPageSize(projectParam.getPageSize());
            pagination.setTotalPageForTotalSize(total);

            if (total == 0) {
                pagination.setDataList(new ArrayList<>());
                return ResultVo.Builder.SUCC().initSuccData(pagination);
            }
            List<Project> list = projectService.listByCondition(projectParam);
            List<ProjectVO> projectVOList = list.stream().map(dto -> {
                ProjectVO projectVO = new ProjectVO();
                BeanUtils.copyProperties(dto, projectVO);
                //状态名称
                projectVO.setProjectStatusDesc(ProjectProcessStatusEnum.getName(dto.getProjectStatus()));
                //需求类型
                projectVO.setProjectTypeDesc(ProjectTypeEnum.getName(dto.getProjectType()));
                //需求类型
                projectVO.setProjectPriorityDesc(ProjectPriorityEnum.getName(dto.getProjectPriority()));

                projectVO.setBusinessLines(dto.getBusinessLines());
                return projectVO;
            }).collect(Collectors.toList());

            pagination.setDataList(projectVOList);


            return ResultVo.Builder.SUCC().initSuccData(pagination);
        } catch (Exception e) {
            LOGGER.error("更新异常", e);
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("1", "更新发生异常");
        }
    }

    /*项目添加*/
    //TODO 拆
    @CcsPermission("matrix:project:saveProject")
    @RequestMapping(value = "/saveProject", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    public ResultVo saveProject(@RequestBody Project project, HttpServletRequest request) {
        LOGGER.info("saveProject: {}", JSON.toJSONString(project));

        List<ProjectTaskDto> projectTasks = project.getProjectTasks();
        //参数校验
//        if (StringUtils.isBlank(project.getDevOwner())
//                || StringUtils.isBlank(project.getPmOwner())
//                || StringUtils.isBlank(project.getQaOwner())
//                || null == project.getProjectType()
//        ) {
//            return ResultVo.Builder.FAIL().initErrCodeAndMsg("1", "参数异常");
//        }
        if (CollectionUtils.isNotEmpty(projectTasks)) {
            for (ProjectTaskDto projectTask : projectTasks) {
                if (StringUtils.isBlank(projectTask.getAppCode())) {
                    return ResultVo.Builder.FAIL().initErrCodeAndMsg("1", "参数异常，appCode属性为空");
                }
            }
        }

        List<Issue> issueList = project.getIssueList();
        String accessToken = project.getAccessToken();
        OauthUser oauthUser = this.getUser(accessToken, request);
        String userName = oauthUser == null ? "" : oauthUser.getAccount();
        ResultVo vo = ResultVo.Builder.SUCC();
        if (null == project.getId()) {
            try {
                if (!ProjectTypeEnum.ER.getCode().equals(project.getProjectType()) && CollectionUtils.isEmpty(issueList)) {
                    return ResultVo.Builder.FAIL().initErrCodeAndMsg("2", "创建失败, 发布任务需要关联需求");
                }
                project.setCreateUser(userName);
                int result = projectService.insert(project);
                if (result > 0) {
                    vo.initSuccDataAndMsg("0", "更新成功");
                    vo.setData(project);
                    LOGGER.info("project is : {}", project);
                } else {
                    return returnSaveFailure(projectTasks, "创建项目失败");
                }
            } catch (Exception e) {
                LOGGER.info("accessing to saveProject: e={}", e);
                return returnSaveFailure(projectTasks, e.getMessage());
            }
        } else {
            try {
                project.setUpdateUser(userName);
                int result = projectService.updateById(project);
                LOGGER.info("result {}" + result);
                if (result > 0) {
                    //TODO 调用的接口需要有返回值
                    //提测阶段调用代码扫描和创建代码评审的接口，提测阶段的项目状态为2
                    if (ProjectProcessStatusEnum.COMMIT_TEST.getCode().equals(project.getProjectStatus())) {
                        Optional<String> codeScanResult = null;
                        try {
                            codeScanResult = HTTP_POOL_CLIENT.postJson(codeScanRemote, JSONObject.toJSONString(projectTasks));
                        } catch (Exception e) {
                            LOGGER.error("error ", e);
                        }
                        try {
                            Optional<String> optional = HTTP_POOL_CLIENT.postJson(codeReviewCreateRemote, JSON.toJSONString(projectTasks));
                            if (optional.isPresent()) {
                                JSONObject jsonObject = JSONObject.parseObject(optional.get());
                            }
                            LOGGER.info("调用创建代码评审接口的返回值为{}" + optional.get());
                        } catch (Exception e) {
                            LOGGER.error("error", e);
                        }
                    }
                    vo.initSuccDataAndMsg("0", "更新成功");
                    vo.setData(project);
                    LOGGER.info("project is : {}", project);
                } else {
                    return returnSaveFailure(projectTasks, "");
                }
            } catch (Exception e) {
                LOGGER.info("accessing to saveProject: e={}", e);
                return returnSaveFailure(projectTasks, e.getMessage());
            }
        }

        Map<String, Object> map = new HashMap<>();
        map.put("id", project.getId());
        if (!CollectionUtils.isEmpty(issueList)) {

            Set<String> ids = new HashSet<>();
            List<Issue> deduplication = new ArrayList<>();
            for (Issue issue : issueList) {
                String issueId = issue.getJiraIssueId();
                if (ids.add(issueId)) {
                    deduplication.add(issue);
                }
            }

            map.put("issueList", deduplication);
        } else {
            map.put("issueList", new ArrayList<Issue>());
        }
        try {
            HTTP_POOL_CLIENT.postJson(associateIssue, JSON.toJSONString(map));
        } catch (Exception e) {
            LOGGER.info("associateIssue failure.", e);
            return returnSaveFailure(projectTasks, e.getMessage());
        }

        return vo;
    }

    /**
     * 验证应用分支是否合法，通过判断返回值是否为空来区分验证是否合法，如果不为空，说明不合法
     *
     * @param projectTaskList 应用列表
     * @return 如果验证通过，返回null；如果有分支验证不合法，返回ResultVo对象
     */
    private ResultVo verifyBranch(List<ProjectTaskDto> projectTaskList) {
        if (CollectionUtils.isNotEmpty(projectTaskList)) {
            for (ProjectTaskDto projectTask : projectTaskList) {
                String appBranch = projectTask.getAppBranch();
                String appCode = projectTask.getAppCode();
                if (StringUtils.isEmpty(appBranch)) {
                    return ResultVo.Builder.FAIL().initErrCodeAndMsg("1", appCode + "应用分支创建失败");
                }
            }
        }
        return null;
    }

    private ResultVo returnSaveFailure(List<ProjectTaskDto> projectTaskList, String exception) {
        ResultVo fail = verifyBranch(projectTaskList);
        if (fail != null) {
            return fail;
        } else {
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("1", "创建失败 " + exception);
        }
    }

    @CcsPermission("matrix:project:createCodeScan")
    @PostMapping(value = "createCodeScan", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResultVo createCodeScan(@RequestBody Project project) {
        return projectService.createCodeScan(project);
    }

    @CcsPermission("matrix:project:createCodeReview")
    @PostMapping(value = "createCodeReview", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResultVo createCodeReview(@RequestBody Project project) {
        return projectService.createCodeReview(project);
    }

    /*项目编辑*/
    @CcsPermission("matrix:project:updateProject")
    @RequestMapping(value = "/updateProject", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    public ResultVo updateProject(@RequestBody Project project) {
        try {
            int result = projectService.updateById(project);
            if (result > 0) {
                return ResultVo.Builder.SUCC().initSuccDataAndMsg("0", "更新成功");
            } else {
                List<ProjectTaskDto> allprojectTasks = project.getProjectTasks();
                if (null != allprojectTasks) {
                    for (ProjectTaskDto projectTask : allprojectTasks) {
                        String appBranch = projectTask.getAppBranch();
                        String appCode = projectTask.getAppCode();
                        if (appBranch == "" || null == appBranch) {
                            return ResultVo.Builder.FAIL().initErrCodeAndMsg("1", appCode + "应用分支创建失败");
                        }

                    }

                }
                return ResultVo.Builder.FAIL().initErrCodeAndMsg("1", "创建失败");
            }
        } catch (Exception e) {
            LOGGER.error("更新异常", e);
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("1", "更新发生异常");
        }
    }

    /**
     * 查看项目进度
     *
     * @param project
     * @return
     */
    @CcsPermission("matrix:project:getProject")
    @RequestMapping(value = "/getProject", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    public ResultVo getProject(@RequestBody Project project) {
        try {
            Integer id = project.getId();
            ProjectDTO projectResult = projectService.getById(id);
            LOGGER.info("getProject projectResult = {}", projectResult);
            return ResultVo.Builder.SUCC().initSuccData(projectResult);
        } catch (Exception e) {
            LOGGER.error("更新异常", e);
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("1", "更新发生异常");
        }
    }

    /*查看项目记录*/
    @CcsPermission("matrix:project:getProjectRecord")
    @RequestMapping(value = "/getProjectRecord", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    public ResultVo getProjectRecord(@RequestBody ProjectRecord projectRecord) {
        try {
            Integer total = projectRecordService.pageTotal(projectRecord);
            Pagination<ProjectRecord> pagination = new Pagination<>();
            pagination.setPageSize(projectRecord.getPageSize());
            pagination.setTotalPageForTotalSize(total);
            if (total == 0) {
                pagination.setDataList(new ArrayList<>());
                return ResultVo.Builder.SUCC().initSuccData(pagination);
            }
            List<ProjectRecord> projectResult = projectRecordService.listByCondition(projectRecord);
            pagination.setDataList(projectResult);
            return ResultVo.Builder.FAIL().initSuccData(pagination);
        } catch (Exception e) {
            LOGGER.error("更新异常", e);
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("1", "更新发生异常");
        }
    }

    /*应用名称列表*/
    @CcsPermission("matrix:project:getAppCode")
    @RequestMapping(value = "/getAppCode", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    public ResultVo getAppcode(@RequestBody Map<String, String> requestBodyParams) {
        try {
            String appCode = requestBodyParams.get("appCode");
            Map<String, Object> mapInfo = new HashMap();
            mapInfo.put("appCode", appCode);
            mapInfo = SignUtils.addSignParam(mapInfo);

            Optional appList = HTTP_POOL_CLIENT.postJson(appRemote, JSONObject.toJSONString(mapInfo));
            if (appList.isPresent()) {
                JSONObject apps = (JSONObject) JSONObject.parse(appList.get().toString());
//            JSONObject appdatas = (JSONObject) JSONObject.parse(apps.get("data").toString());
                return ResultVo.Builder.SUCC().initSuccData(apps.get("data"));

            }
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("1", "未查询到应用信息");
        } catch (Exception e) {
            LOGGER.error("更新异常", e);
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("1", "更新发生异常");
        }

    }

    @CcsPermission("matrix:project:getAppCodeList")
    @ApiOperation(value = "根据部门id查询应用树", notes = "")
    @RequestMapping(value = "/getAppCodeList", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    public ResultVo getAppCodeList(@RequestBody GetDeptAppTreeByDeptIdParam requestBodyParams) {
        try {
            Long deptId = requestBodyParams.getDeptId();
            String appCode = requestBodyParams.getAppCode();
            List<AppListsDto> appListsDtos = projectService.dealAppcodeInfo(deptId, appCode);
            Pagination<AppListsDto> pagination = new Pagination<>();
            pagination.setPageSize(requestBodyParams.getPageSize());
            pagination.setTotalPageForTotalSize(appListsDtos.size());
            pagination.setDataList(appListsDtos);
            if (appListsDtos.size() < 0) {
                return ResultVo.Builder.FAIL().initErrCodeAndMsg("0", "未查询到应用信息");
            }
            return ResultVo.Builder.SUCC().initSuccData(pagination);
        } catch (Exception e) {
            LOGGER.error("更新异常", e);
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("1", "更新发生异常");
        }
    }

    @CcsPermission("matrix:project:getAppCodeDetail")
    @ApiOperation(value = "根据appCode查询应用详细信息", notes = "")
    @RequestMapping(value = "/getAppCodeDetail", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    public ResultVo getAppCodeDetail(@RequestBody Map<String, String> requestBodyParams) {
        try {
            String appCode = requestBodyParams.get("appCode");
            Map<String, Object> mapInfo = new HashMap();
            mapInfo.put("appCode", appCode);
            mapInfo.put("envCode", "prod");
            AppInfoDto appInfoDto = projectService.getAppDetailInfo(mapInfo);
            if (appInfoDto != null) {
                return ResultVo.Builder.SUCC().initSuccData(appInfoDto);

            }
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("0", "未查询到应用信息");
        } catch (Exception e) {
            LOGGER.error("更新异常", e);
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("1", "更新发生异常");
        }
    }

    @CcsPermission("matrix:project:getAllFirstDept")
    @ApiOperation(value = "查询所有一级部门列表", notes = "")
    @RequestMapping(value = "/getAllFirstDept", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    public ResultVo getAllFirstDept(@RequestBody BaseParam baseParam) {
        try {
            Map<String, Object> mapInfo = new HashMap();
            mapInfo = SignUtils.addSignParam(mapInfo);
            Optional<String> appList = HTTP_POOL_CLIENT.postJson(firstDeptRemote, JSONObject.toJSONString(mapInfo));
            if (appList.isPresent()) {
                JSONObject apps = (JSONObject) JSONObject.parse(appList.get().toString());
                return ResultVo.Builder.SUCC().initSuccData(apps.get("data"));

            }
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("0", "未查询到部门信息");
        } catch (Exception e) {
            LOGGER.error("更新异常", e);
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("1", "更新发生异常");
        }

    }

    /*人员列表*/
    @CcsPermission("matrix:project:getPerson")
    @RequestMapping(value = "/getPerson", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    public ResultVo getPerson(@RequestBody Map<String, String> requestBodyParams) {
        try {
            String personKey = requestBodyParams.get("personKey");
            Map<String, Object> mapInfo = new HashMap();
            mapInfo.put("name", personKey);
            mapInfo = SignUtils.addSignParam(mapInfo);

            Optional appList = HTTP_POOL_CLIENT.postJson(personRemote, JSONObject.toJSONString(mapInfo));
            if (appList.isPresent()) {
                JSONObject apps = (JSONObject) JSONObject.parse(appList.get().toString());
                return ResultVo.Builder.SUCC().initSuccData(apps.get("data"));

            }
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("1", "未查询到人员信息");
        } catch (Exception e) {
            LOGGER.error("更新异常", e);
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("1", "更新发生异常");
        }

    }

    @CcsPermission("matrix:project:getUser")
    @RequestMapping(value = "/getUser", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    public ResultVo getUser(@RequestBody UserLdap user) {
        String userName = user.getUserName();
        LOGGER.info("{}", userName);
        String fullName = ChineseCharacterUtil.getLowerCase(userName, true);
        LOGGER.info("{}", fullName);
        UserLdap rl = new UserLdap();
        rl.setUserName(fullName);
        List<String> users = userLdapService.getUserName(rl);
        LOGGER.info("{}", users);
        if (!users.isEmpty()) {
            return ResultVo.Builder.SUCC().initSuccData(users.get(0));
        } else {
            String lastName = ChineseCharacterUtil.getXing(userName);
            String firstName = ChineseCharacterUtil.getMingZi(userName);
            fullName = ChineseCharacterUtil.getLowerCase(lastName, true) + ChineseCharacterUtil.getLowerCase(firstName, false);
            LOGGER.info("{}", fullName);
            rl.setUserName(fullName);
            users = userLdapService.getUserName(rl);
            LOGGER.info("{}", users);
            if (!users.isEmpty()) {
                return ResultVo.Builder.SUCC().initSuccData(users.get(0));
            }
        }
        return ResultVo.Builder.FAIL().initErrCodeAndMsg("1", "查无此人");
    }

    @CcsPermission("matrix:project:listAuditor")
    @RequestMapping(value = "/listAuditor", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    public ResultVo listAuditor(@RequestBody AuditDTO auditDTO, HttpServletRequest request) {
        try {
            Integer projectId = auditDTO.getProjectId();
            if (projectId == null || projectId <= 0) {
                return ResultVo.Builder.SUCC().initErrCodeAndMsg("1000", "项目ID不合法");
            }

            List<Auditor> auditorList = this.listAuditor(projectId);
            AuditorsVO auditorsVO = new AuditorsVO();
            auditorsVO.setAuditors(auditorList);
            return ResultVo.Builder.SUCC().initSuccData(auditorsVO);
        } catch (Exception e) {
            return ResultVo.Builder.SUCC().initErrCodeAndMsg("2000", "获取审核人失败");
        }
    }

    @RequestMapping(value = "/auditCheck", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    public ResultVo auditCheck(@RequestBody AuditDTO auditDTO, HttpServletRequest request) {
        try {
            Integer projectId = auditDTO.getProjectId();
            if (projectId == null || projectId <= 0) {
                return ResultVo.Builder.SUCC().initErrCodeAndMsg("1000", "项目ID不合法");
            }

            List<Auditor> auditorList = this.listAuditor(projectId);


            String accessToken = auditDTO.getAccessToken();
            OauthUser oauthUser = this.getUser(accessToken, request);

            if (StringUtils.isEmpty(oauthUser.getEmail()) && StringUtils.isEmpty(oauthUser.getMobile())) {
                return ResultVo.Builder.FAIL().initErrCodeAndMsg("3000", "无法获取当前用户信息");
            }

            boolean canAudit = false;
            for (Auditor auditor : auditorList) {
                String email = oauthUser.getEmail();
                if (email != null) {
                    if (email.equals(auditor.getEmail())) {
                        canAudit = true;
                        break;
                    }
                }
                String mobile = oauthUser.getMobile();
                if (mobile != null) {
                    if (mobile.equals(auditor.getTelephone())) {
                        canAudit = true;
                        break;
                    }
                }
            }

            if (canAudit) {
                return ResultVo.Builder.SUCC().initSuccDataAndMsg("0", "审核成功");
            } else {
                return ResultVo.Builder.FAIL().initErrCodeAndMsg("1000", "很抱歉，您还没有审批该项目的权限，请找相关审批人进行审批");
            }
        } catch (
                Exception e) {
            return ResultVo.Builder.SUCC().initErrCodeAndMsg("4000", "系统错误");
        }

    }

    @RequestMapping(value = "/audit", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    public ResultVo audit(@RequestBody AuditDTO auditDTO, HttpServletRequest request) {
        try {
            Integer projectId = auditDTO.getProjectId();
            if (projectId == null || projectId <= 0) {
                return ResultVo.Builder.SUCC().initErrCodeAndMsg("1000", "项目ID不合法");
            }

            Project originProject = projectService.getProjectById(projectId);
            if (originProject == null) {
                return ResultVo.Builder.FAIL().initErrCodeAndMsg("2000", "无法找到项目记录");
            }

            String accessToken = auditDTO.getAccessToken();
            OauthUser oauthUser = this.getUser(accessToken, request);
            String email = oauthUser.getEmail();

            if (StringUtils.isEmpty(email)) {
                return ResultVo.Builder.FAIL().initErrCodeAndMsg("3000", "无法获取当前用户信息");
            }

            originProject.setAuditor(email);
            originProject.setAuditStatus(1);
            projectService.updateById(originProject);
            return ResultVo.Builder.SUCC().initErrCodeAndMsg("0", "通过审核成功");
        } catch (Exception e) {
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("4000", "系统异常");
        }
    }

    private OauthUser getUser(String accessToken, HttpServletRequest request) {
        return ccsService.getUserByToken(accessToken, request);
    }

    private List<Auditor> listAuditor(Integer projectId) throws IOException {

        Resource resource = new ClassPathResource("auditors.json");
        BufferedReader br = new BufferedReader(new InputStreamReader(resource.getInputStream()));

        StringBuilder builder = new StringBuilder();
        String str;
        while ((str = br.readLine()) != null) {
            builder.append(str);
        }

        String json = builder.toString();
        AuditorList auditorListData = JSON.toJavaObject(JSON.parseObject(json), AuditorList.class);
        List<Auditor> superAuditors = auditorListData.getSuperAuditors();

        List<Auditor> auditorList = new ArrayList<>();
        superAuditors.stream().forEach(auditor -> {
            auditorList.add(auditor);
        });

        List<ProjectAuditorMappings> projectList = auditorListData.getProjects();

        for (ProjectAuditorMappings projectAuditorMappings : projectList) {
            Integer pId = projectAuditorMappings.getProjectId();
            if (projectId.equals(pId)) {
                List<Auditor> list = projectAuditorMappings.getAuditors();
                if (!CollectionUtils.isEmpty(list)) {
                    auditorList.addAll(list);
                }
                break;
            }
        }
        return auditorList;
    }

}