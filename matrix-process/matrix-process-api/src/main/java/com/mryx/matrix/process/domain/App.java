package com.mryx.matrix.process.domain;

import com.mryx.matrix.common.domain.Base;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;


/**
 * @author pengcheng
 * @email pengcheng@missfresh.cn
 * @date 2018-10-19 16:32
 **/
@Data
public class App extends Base implements Serializable {

    private static final long serialVersionUID = 2687202792456001569L;

    /**
     * 主键id
     **/
    private Integer id;

    /**
     * 所属部门ID,组织
     **/
    private Integer deptId;

    /**
     * 应用代号,全局唯一
     **/
    private String appCode;

    /**
     * 应用名称
     **/
    private String appName;

    /**
     * 修改时间
     **/
    private Date gmtModified;

    /**
     * 删除标识，1:正常|0:删除
     */
    private int delFlag;

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return this.id;
    }

    public void setDeptId(Integer deptId) {
        this.deptId = deptId;
    }

    public Integer getDeptId() {
        return this.deptId;
    }

    public void setAppCode(String appCode) {
        this.appCode = appCode;
    }

    public String getAppCode() {
        return this.appCode;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getAppName() {
        return this.appName;
    }

    public void setGmtModified(Date gmtModified) {
        this.gmtModified = gmtModified;
    }

    public Date getGmtModified() {
        return this.gmtModified;
    }

    public int getDelFlag() {
        return delFlag;
    }

    public void setDelFlag(int delFlag) {
        this.delFlag = delFlag;
    }

    @Override
    public String toString() {
        return "App [ id= " + id +
                ",deptId= " + deptId +
                ",appCode= " + appCode +
                ",appName= " + appName +
                ",gmtModified= " + gmtModified + "]";
    }
}
