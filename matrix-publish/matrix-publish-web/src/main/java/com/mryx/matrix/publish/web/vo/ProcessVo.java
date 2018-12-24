package com.mryx.matrix.publish.web.vo;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author dinglu
 * @date 2018/9/27
 */
public class ProcessVo implements Serializable{

    private static final long serialVersionUID = -762678427818560317L;
    private Boolean completeFlag;

    private HashMap<String,Double> processMap;

    private List<DeployPlanVo> deployPlanVoList;

    public Boolean getCompleteFlag() {
        return completeFlag;
    }

    public void setCompleteFlag(Boolean completeFlag) {
        this.completeFlag = completeFlag;
    }

    public HashMap<String, Double> getProcessMap() {
        return processMap;
    }

    public void setProcessMap(HashMap<String, Double> processMap) {
        this.processMap = processMap;
    }

    public List<DeployPlanVo> getDeployPlanVoList() {
        return deployPlanVoList;
    }

    public void setDeployPlanVoList(List<DeployPlanVo> deployPlanVoList) {
        this.deployPlanVoList = deployPlanVoList;
    }

    @Override
    public String toString() {
        return "ProcessVo{" +
                "completeFlag=" + completeFlag +
                ", processMap=" + processMap +
                ", deployPlanVoList=" + deployPlanVoList +
                '}';
    }
}
