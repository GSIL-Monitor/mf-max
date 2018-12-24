package com.mryx.matrix.publish.domain;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;

/**
 *
 * @author dinglu
 * @date 2018/9/12
 */
public class BetaDeployParam extends CommonDeployParam implements Serializable{
    private static final long serialVersionUID = -9137070396645203089L;

    /*发布记录ID*/
    private int record;

    /**应用所属业务 blg，missfresh，mryt**/
    private String bizLine;

    /**docker环境**/
    private String dockerEnv;

    /**是否Dock部署**/
    private Boolean isDockerDeploy;

    public int getRecord() {
        return record;
    }

    public void setRecord(int record) {
        this.record = record;
    }

    public Boolean getDockerDeploy() {
        return isDockerDeploy;
    }

    public void setDockerDeploy(Boolean dockerDeploy) {
        isDockerDeploy = dockerDeploy;
    }

    public String getBizLine() {
        return bizLine;
    }

    public void setBizLine(String bizLine) {
        this.bizLine = bizLine;
    }
    public String getDockerEnv() {
        return dockerEnv;
    }

    public void setDockerEnv(String dockerEnv) {
        this.dockerEnv = dockerEnv;
    }

    @Override
    public String toString() {
        return "BetaDeployParam{" +
                "record=" + record + super.toString()+
                '}';
    }

    public static void main(String[] args) throws UnsupportedEncodingException {
        BetaDeployParam betaDeployParam = new BetaDeployParam();
        betaDeployParam.setRecord(5);
        betaDeployParam.setAppCode("grampus-crm");
        betaDeployParam.setHealthCheck("18097/api/crm/common/healthcheck");
        betaDeployParam.setUser("dinglu");
        betaDeployParam.setScript("/data/jenkins/workspace/ops-scripts/tmp_beta_prod_build.sh");
        betaDeployParam.setServerList("10.7.2.16");
        betaDeployParam.setProjectPom("pom.xml");//后期可不用
        betaDeployParam.setMvnOps("");
        betaDeployParam.setJarName("grampus-crm.jar");
        betaDeployParam.setGitAddress("git@git.missfresh.cn:grampus/grampus-crm.git");
        betaDeployParam.setDeployPath("/grampus-crm-web/target/");
        betaDeployParam.setBuildType("beta");
        betaDeployParam.setBuildPath("");//可不用
        betaDeployParam.setWorkspace("");//可不用
        betaDeployParam.setBuildBranch("f_20180912_traceSource");
        betaDeployParam.setAppName("crm");//可不用
        betaDeployParam.setJobName("");//可不用
        betaDeployParam.setDockerDeploy(false);

        System.out.println(betaDeployParam.toString());
    }
}
