package com.mryx.matrix.publish.core.service;

import com.mryx.matrix.publish.domain.BetaDeployParam;
import com.mryx.matrix.publish.domain.ReleaseDeployParam;

import java.util.List;

/**
 * Created by dinglu on 2018/9/12.
 */
public interface PublishService {

    String betaPublish(BetaDeployParam betaDeployParam , String outputToLogFilePwd);

    String releasePublish(ReleaseDeployParam releaseDeployParam, String outputToLogFilePwd);

    String log(Integer id , String fileAddr, boolean init);

    /**
     * 摘dubbo
     * @param ips
     * @return
     */
    boolean removeDubbo(List<String> ips);
}
