package com.mryx.matrix.publish.web.vo;

import com.mryx.matrix.common.domain.Base;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author dinglu
 * @date 2018/9/10
 */
@Data
public class PublishParamVo extends Base implements Serializable {

    private static final long serialVersionUID = -6080382206389455363L;

    /**
     * 项目ID
     **/
    private Integer projectId;

    private List<PublishSequeneceVo> publishSequeneceVos;

    private List<DeployPlanVo> deployPlanVos;

    /**
     * 是否Dock部署
     **/
    private Boolean isDockerDeploy;

    /**
     * 是否压测部署
     */
    private Boolean isStressDeploy;

    @Override
    public String toString() {
        return "PublishParamVo{" +
                "projectId=" + projectId +
                ", publishSequeneceVos=" + publishSequeneceVos +
                ", deployPlanVos=" + deployPlanVos +
                ", isDockerDeploy=" + isDockerDeploy +
                ", isStressDeplo=" + isStressDeploy +
                '}';
    }

}
