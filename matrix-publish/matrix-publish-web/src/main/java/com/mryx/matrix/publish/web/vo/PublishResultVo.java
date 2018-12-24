package com.mryx.matrix.publish.web.vo;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

/**
 * @author dinglu
 * @date 2018/9/25
 */
public class PublishResultVo implements Serializable {

    private static final long serialVersionUID = -8392620269979378026L;

    private String buildType;

    private HashMap<String, Integer> appCodeRecordMapping;

    private String tips;

    public String getBuildType() {
        return buildType;
    }

    public void setBuildType(String buildType) {
        this.buildType = buildType;
    }

    public HashMap<String, Integer> getAppCodeRecordMapping() {
        return appCodeRecordMapping;
    }

    public void setAppCodeRecordMapping(HashMap<String, Integer> appCodeRecordMapping) {
        this.appCodeRecordMapping = appCodeRecordMapping;
    }

    public String getTips() {
        return tips;
    }

    public void setTips(String tips) {
        this.tips = tips;
    }

    @Override
    public String toString() {
        return "PublishResultVo{" +
                "buildType='" + buildType + '\'' +
                ", appCodeRecordMapping=" + appCodeRecordMapping +
                ", tips='" + tips + '\'' +
                '}';
    }
}
