package com.mryx.matrix.process.web.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.mryx.common.utils.HttpPoolClient;
import com.mryx.common.utils.StringUtils;
import com.mryx.grampus.ccs.domain.OauthUser;
import com.mryx.matrix.common.domain.ResultVo;
import com.mryx.matrix.process.core.ccs.CcsService;
import com.mryx.matrix.process.domain.Project;
import com.mryx.matrix.process.dto.DeptAppTree;
import com.mryx.matrix.process.dto.DeptDto;
import com.mryx.matrix.process.web.vo.BusinessLineVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * @author pengcheng
 * @description
 * @email pengcheng@missfresh.cn
 * @date 2018-11-05 16:06
 **/

@RestController
@RequestMapping("/api/process")
@Slf4j
public class BusinessLineController {

    @Autowired
    private CcsService ccsService;

    @Value("${firstDept_remote}")
    private String listAllFirstDept;

    @Value("${appTree_remote}")
    private String appTreeRemote;

    @Value("${searchUser_remote}")
    private String searchUserRemote = "http://10.7.8.10:8888/openapi/app/searchUser";

    private final HttpPoolClient HTTP_POOL_CLIENT = com.mryx.common.utils.HttpClientUtil.create(2000, 2000, 5, 5, 1, 500);

    @RequestMapping(value = "/getBusinessLine", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    public ResultVo getBusinessLines(@RequestBody Project project, HttpServletRequest request) {
        try {
            String accessToken = project.getAccessToken();
            String email = this.getUserEmail(accessToken, request);
            List<BusinessLineVO> businessLineDescList = new ArrayList<>();
            Set<String> set = new HashSet<>();
            if (!StringUtils.isEmpty(email)) {
                List<BusinessLineVO> userBusinessLineList = this.getUserBusinessLines(email);
                for (BusinessLineVO businessLineVO : userBusinessLineList) {
                    set.add(businessLineVO.getBusinessLineDesc());
                    businessLineDescList.add(businessLineVO);
                }
            }
            List<BusinessLineVO> commons = this.getCommonBusinessLines();
            for (BusinessLineVO businessLineVO : commons) {
                if (set.contains(businessLineVO.getBusinessLineDesc())) {
                    continue;
                }
                businessLineDescList.add(businessLineVO);
            }
            return ResultVo.Builder.SUCC().initSuccData(businessLineDescList);
        } catch (Exception e) {
            log.error("获取业务线异常", e);
            return ResultVo.Builder.FAIL().initErrCodeAndMsg("4000", "系统错误");
        }
    }

    /**
     * 根据用户邮箱地址获取业务线列表
     *
     * @param email 邮箱地址
     * @return 业务线列表
     */
    private List<BusinessLineVO> getUserBusinessLines(String email) {
        List<BusinessLineVO> businessLineDescList = new ArrayList<>();

        Map mapInfo = new HashMap<>();
        mapInfo.put("email", email);
        Optional<String> optional = HTTP_POOL_CLIENT.postJson(searchUserRemote, JSONObject.toJSONString(mapInfo));
        String response = optional.get();
        ResultVo<JSONArray> resultVo = JSON.parseObject(response, ResultVo.class);
        String code = resultVo.getCode();
        if (!"0".equals(code)) {
            log.error("获取用户业务线异常 ， 接口错误={}", resultVo.getMessage());
            return businessLineDescList;
        }
        Set<String> set = new HashSet();
        JSONArray data = resultVo.getData();

        for (int index = 0; index < data.size(); index++) {
            JSONObject jsonObject = (JSONObject) data.get(index);
            JSONArray deptDtoList = (JSONArray) jsonObject.get("deptDtoList");
            StringBuilder builder = new StringBuilder();
            int size = deptDtoList.size();
            for (int i = size - 1; i >= 0 && (size - i) < 3; i--) {
                JSONObject o = (JSONObject) deptDtoList.get(i);
                String name = (String) o.get("name");
                if (builder.length() > 0) {
                    builder.insert(0, name + "/");
                } else {
                    builder.append(name);
                }
            }
            String businessLineDesc = builder.toString();
            if (!StringUtils.isEmpty(businessLineDesc) && !set.contains(businessLineDesc)) {
                set.add(businessLineDesc);
                BusinessLineVO businessLineVO = new BusinessLineVO();
                businessLineVO.setBusinessLineDesc(businessLineDesc);
                businessLineDescList.add(businessLineVO);
            }
        }
        return businessLineDescList;
    }

    public static void main(String[] args) {
        List<BusinessLineVO> list = new BusinessLineController().getUserBusinessLines("shica");
        System.out.println(list);
    }

    /**
     * 获取通用业务线列表
     *
     * @return
     */
    private List<BusinessLineVO> getCommonBusinessLines() {
        Optional<String> optional = HTTP_POOL_CLIENT.postJson(listAllFirstDept, "{}");
        String response = optional.isPresent() ? optional.get() : "";
        ResultVo resultVo = JSON.parseObject(response, ResultVo.class);

        List<BusinessLineVO> businessLineDescList = new ArrayList<>();

        JSONArray departmentArray = (JSONArray) resultVo.getData();
        departmentArray.stream().forEach(department -> {
            DeptDto firstDeptDto = JSON.toJavaObject(JSON.parseObject(department.toString()), DeptDto.class);

            Long deptId = firstDeptDto.getDeptId();

            Map mapInfo = new HashMap<>();
            mapInfo.put("deptId", deptId);
            Optional<String> deptList = HTTP_POOL_CLIENT.postJson(appTreeRemote, JSONObject.toJSONString(mapInfo));

            JSONObject jsonObject = (JSONObject) JSONObject.parse(deptList.get().toString());

            DeptAppTree deptAppTree = JSON.parseObject(jsonObject.get("data").toString(), DeptAppTree.class);

            findLeaf(deptAppTree, null, businessLineDescList);
        });

        return businessLineDescList;
    }

    /**
     * 查找业务线叶子节点
     *
     * @param deptAppTree
     * @param parentDto
     * @param businessLineVOList
     */
    private void findLeaf(DeptAppTree deptAppTree, DeptDto parentDto, final List<BusinessLineVO> businessLineVOList) {
        DeptDto deptDto = deptAppTree.getDeptDto();
        if (deptAppTree.isHasChildren()) {
            deptAppTree.getChildren().stream().forEach(child -> {
                findLeaf(child, deptDto, businessLineVOList);
            });
        } else {
            String detpName = deptDto.getName();
            String parentName = parentDto == null ? "" : parentDto.getName();
            BusinessLineVO businessLineVO = new BusinessLineVO();
            String businessLineDesc;
            if (!StringUtils.isEmpty(parentName)) {
                businessLineDesc = parentName + "/" + detpName;
            } else {
                businessLineDesc = detpName;
            }
            businessLineVO.setBusinessLineDesc(businessLineDesc);
            businessLineVOList.add(businessLineVO);
        }
    }

    private String getUserEmail(String accessToken, HttpServletRequest request) {
        OauthUser user = ccsService.getUserByToken(accessToken, request);
        return user.getEmail();
    }
}
