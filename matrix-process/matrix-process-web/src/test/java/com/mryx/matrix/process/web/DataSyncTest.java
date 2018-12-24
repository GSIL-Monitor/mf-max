package com.mryx.matrix.process.web;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.mryx.common.utils.HttpPoolClient;
import com.mryx.common.utils.StringUtils;
import com.mryx.matrix.common.domain.ResultVo;
import com.mryx.matrix.common.dto.AppInfoDto;
import com.mryx.matrix.common.dto.DeptDto;
import com.mryx.matrix.process.core.utils.TreeUtil;
import com.mryx.matrix.process.domain.App;
import com.mryx.matrix.process.domain.AppDeployConfig;
import com.mryx.matrix.process.dto.AppListsDto;
import com.mryx.matrix.process.dto.DeptAppTree;
import lombok.extern.slf4j.Slf4j;
import springfox.documentation.spring.web.json.Json;

import java.util.*;

/**
 * @author pengcheng
 * @description 应用中心信息同步
 * @email pengcheng@missfresh.cn
 * @date 2018-10-20 16:44
 **/
@Slf4j
public class DataSyncTest {


    private final HttpPoolClient HTTP_POOL_CLIENT = com.mryx.common.utils.HttpClientUtil.create(2000, 2000, 5, 5, 1, 500);

    private final String listAllFirstDept = "http://appcenter.missfresh.cn/openapi/app/listAllFirstDept";
    private final String appTreeRemote = "http://appcenter.missfresh.cn/openapi/app/getDeptAppTree";
    private final String getAppInfoByAppCode = "http://appcenter.missfresh.cn/openapi/app/getAppInfoByAppCode";


    enum EvnCode {
        DEV("dev"),
        BETA("beta"),
        PROD("prod");

        EvnCode(String desc) {
            this.desc = desc;
        }

        private String desc;

        public String getDesc() {
            return desc;
        }

    }

    private void test() {

        Optional<String> optional = HTTP_POOL_CLIENT.postJson(listAllFirstDept, "{}");
        String response = optional.isPresent() ? optional.get() : "";
        ResultVo resultVo = JSON.parseObject(response, ResultVo.class);

        List<Object> deptDtoList = (List<Object>) resultVo.getData();
        log.info("first department info {}", deptDtoList.toString());

        List<App> appList = new ArrayList<>();
        List<AppDeployConfig> appDeployConfigList = new ArrayList<>();
        for (Object deptDto : deptDtoList) {

            DeptDto dept = JSON.toJavaObject((JSON) deptDto, DeptDto.class);
            String deptId = dept.getDeptId().toString();
            String deptName = dept.getName();
            log.info("deptId: {}, deptName: {}", deptId, deptName);

            if (StringUtils.isEmpty(deptId)) {
                log.info("{}");
                break;
            }

            Map mapInfo = new HashMap<>();
            mapInfo.put("deptId", deptId);
            Optional<String> appInfoList = HTTP_POOL_CLIENT.postJson(appTreeRemote, JSONObject.toJSONString(mapInfo));
            List<AppListsDto> appCodeAndNameDtoList = new ArrayList<>();

            JSONObject apps = (JSONObject) JSONObject.parse(appInfoList.get().toString());
            ResultVo vo = JSON.toJavaObject(apps, ResultVo.class);
            if ("fail".equals(vo.getRet())) {
                log.error("{}", vo.getMessage());
                log.info("map : {}", mapInfo);
                break;
            }
            DeptAppTree tree = (DeptAppTree) JSON.parseObject(apps.get("data").toString(), DeptAppTree.class);

            TreeUtil.merge(tree, appCodeAndNameDtoList, null);

            Map mapInfo2 = new HashMap();
            appCodeAndNameDtoList.stream().forEach(app -> {
                log.info("");
                log.info("app name: {}, app code: {}, deptId: {}", app.getAppName(), app.getAppCode(), app.getDeptId());
                App a = new App();
                a.setAppName(app.getName());
                a.setDeptId(Long.valueOf(app.getDeptId()).intValue());
                a.setAppCode(app.getAppCode());
                appList.add(a);

                mapInfo2.put("appCode", app.getAppCode());
                for (EvnCode evnCode : EvnCode.values()) {
                    mapInfo2.put("envCode", evnCode.getDesc());
                    Optional<String> appListDetail = HTTP_POOL_CLIENT.postJson(getAppInfoByAppCode, JSONObject.toJSONString(mapInfo2));

                    ResultVo vo2 = JSON.toJavaObject(JSON.parseObject(appListDetail.get()), ResultVo.class);

                    AppInfoDto appInfoDto = (AppInfoDto) JSON.toJavaObject((JSON) vo2.getData(), AppInfoDto.class);
                    log.info("envCod: {}, detail: {}", evnCode.getDesc(), appInfoDto.toString());

                    AppDeployConfig appDeployConfig = new AppDeployConfig();
                    appDeployConfig.setAppCode(appInfoDto.getAppCode());
                    appDeployConfig.setAppEnv(evnCode.getDesc());
                    appDeployConfig.setDeployParameters(appInfoDto.getDeployParameters());
                    appDeployConfig.setDeployPath(appInfoDto.getDeployPath());
                    appDeployConfig.setGit(appInfoDto.getGit());
                    appDeployConfig.setGmtModified(new Date());
                    appDeployConfig.setHealthcheck(appInfoDto.getHealthcheck());
                    appDeployConfig.setPkgName(appInfoDto.getPkgName());
                    appDeployConfig.setPkgType(appInfoDto.getPkgType());
                    appDeployConfig.setPort(appInfoDto.getPort());

                    appDeployConfigList.add(appDeployConfig);
                }
            });


            log.info("app count: {}", appCodeAndNameDtoList.size());
            log.info("");

        }
        log.info("app total count: {}", appList.size());
        log.info("app config total count: {}", appDeployConfigList.size());
    }

    public static void main(String[] args) {

        new DataSyncTest().test();
    }
}
