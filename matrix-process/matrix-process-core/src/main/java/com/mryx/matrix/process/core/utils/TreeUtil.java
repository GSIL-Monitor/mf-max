package com.mryx.matrix.process.core.utils;

import com.mryx.matrix.process.dto.AppDto;
import com.mryx.matrix.process.dto.AppListsDto;
import com.mryx.matrix.process.dto.DeptAppTree;
import com.mryx.matrix.process.dto.DeptDto;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import java.util.List;

/**
 * @author pengcheng
 * @description 遍历部门树工具类
 * @email pengcheng@missfresh.cn
 * @date 2018-10-20 18:45
 **/
public class TreeUtil {

    public static void merge(DeptAppTree tree, List<AppListsDto> appListsDtos, String filter) {
        List<DeptAppTree> children = tree.getChildren();
        List<AppDto> appDtoList = tree.getApps();
        Long deptId = tree.getDeptId();
        Long parentId = tree.getParentId();
        String deptName = null;
        DeptDto deptDto = tree.getDeptDto();
        if (deptDto != null) {
            deptName = deptDto.getName();
        }
        for (AppDto appDto : appDtoList) {
            AppListsDto appListsDto = new AppListsDto();
            String appCode = appDto.getAppCode();
            if (StringUtils.isNotBlank(filter)) {
                if (!appCode.contains(filter)) {
                    continue;
                }
            }
            appListsDto.setAppCode(appCode);
            appListsDto.setAppName(appDto.getAppName());
            appListsDto.setDeployParameters(appDto.getDeployParameters());
            appListsDto.setGit(appDto.getGit());
            appListsDto.setDeployPath(appDto.getDeployPath());
            appListsDto.setPkgName(appDto.getPkgName());
            appListsDto.setHealthcheck(appDto.getHealthcheck());
            appListsDto.setPort(appDto.getPort());
            appListsDto.setName(appDto.getName());
            appListsDto.setDeptName(deptName);
            appListsDto.setGroupInfo(appDto.getGroupInfo());
            appListsDto.setDeptId(deptId);
            appListsDto.setPkgType(appDto.getPkgType());
            appListsDto.setParentId(parentId);
            appListsDtos.add(appListsDto);
        }
        if (CollectionUtils.isEmpty(children)) {
            return;
        }

        for (DeptAppTree child : children) {
            merge(child, appListsDtos, filter);
        }
    }
}
