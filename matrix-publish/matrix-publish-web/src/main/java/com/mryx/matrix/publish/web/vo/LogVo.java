package com.mryx.matrix.publish.web.vo;

import java.io.Serializable;

/**
 *
 * @author dinglu
 * @date 2018/9/25
 */
public class LogVo implements Serializable {
    private static final long serialVersionUID = -7054644762073059996L;

    private boolean publishCompleteFlag;

    private String logContent;

    public boolean isPublishCompleteFlag() {
        return publishCompleteFlag;
    }

    public void setPublishCompleteFlag(boolean publishCompleteFlag) {
        this.publishCompleteFlag = publishCompleteFlag;
    }

    public String getLogContent() {
        return logContent;
    }

    public void setLogContent(String logContent) {
        this.logContent = logContent;
    }

    @Override
    public String toString() {
        return "LogVo{" +
                "publishCompleteFlag=" + publishCompleteFlag +
                ", logContent='" + logContent + '\'' +
                '}';
    }
}
