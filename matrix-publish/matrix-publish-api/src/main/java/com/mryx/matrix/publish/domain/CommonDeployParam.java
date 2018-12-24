package com.mryx.matrix.publish.domain;

import com.mryx.matrix.common.domain.Base;
import lombok.Data;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;

/**
 *
 * @author dinglu
 * @date 2018/9/12
 */
@Data
public class CommonDeployParam extends Base implements Serializable{

    private static final long serialVersionUID = -7055423588524452446L;

    /*
        *    发布脚本公共参数：
        *    工作区：WORKSPACE
        *    发布用户：BUILD_USER_ID
        *    发布类型（dev/beta/prod）：build_type
        *    部署机器：SERVER_LIST
        *    部署分支：build_branch
        **/
    private String workspace;   //暂时没用到
    private String user;
    private String buildType;
    private String serverList;
    private String buildBranch;

    /*
    *   GIT信息：
    *   应用名称：APP_CODE
    *   工程地址：GIT_ADDRESS
    * */
    private String appCode;
    private String gitAddress;

    /*
    * static工程发布参数
    * build_path
    *JOB_NAME
    * */
    private String buildPath;
    private String jobName;

    /*
    * go工程发布参数
    * APP_NAME
    * */
    private String appName;

    /*
    * 其他工程发布参数
    * mvn_ops
    * PROJECT_POM
    * deploy_path
    * jar_name
    * */
    private String mvnOps;
    private String projectPom;
    private String deployPath;
    private String jarName;
    private String healthCheck;

    /*启动参数*/
    private String startOps;

    /*0主商城 1便利购*/
    private String mavenPath;

    /*
    * 发布脚本*/
    private String script;

    /*应用类型*/
    private String appType;

    private Integer projectTaskId;

    private String pkgPath;

    private String containerPath;

    private String startShellPath;

    private String stopShellPath;

    private String startLogPath;

    private String startSuccFlag;

    @Override
    public String toString() {
        return "CommonDeployParam{" +
                "workspace='" + workspace + '\'' +
                ", user='" + user + '\'' +
                ", buildType='" + buildType + '\'' +
                ", serverList='" + serverList + '\'' +
                ", buildBranch='" + buildBranch + '\'' +
                ", appCode='" + appCode + '\'' +
                ", gitAddress='" + gitAddress + '\'' +
                ", buildPath='" + buildPath + '\'' +
                ", jobName='" + jobName + '\'' +
                ", appName='" + appName + '\'' +
                ", mvnOps='" + mvnOps + '\'' +
                ", projectPom='" + projectPom + '\'' +
                ", deployPath='" + deployPath + '\'' +
                ", jarName='" + jarName + '\'' +
                ", healthCheck='" + healthCheck + '\'' +
                ", startOps='" + startOps + '\'' +
                ", mavenPath='" + mavenPath + '\'' +
                ", script='" + script + '\'' +
                ", appType='" + appType + '\'' +
                ", projectTaskId=" + projectTaskId +
                ", pkgPath='" + pkgPath + '\'' +
                ", containerPath='" + containerPath + '\'' +
                ", startShellPath='" + startShellPath + '\'' +
                ", stopShellPath='" + stopShellPath + '\'' +
                ", startLogPath='" + startLogPath + '\'' +
                ", startSuccFlag='" + startSuccFlag + '\'' +
                '}';
    }
}
