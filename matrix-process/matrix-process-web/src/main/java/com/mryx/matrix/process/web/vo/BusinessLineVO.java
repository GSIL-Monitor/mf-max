package com.mryx.matrix.process.web.vo;

import java.io.Serializable;
import java.util.List;

/**
 * @author pengcheng
 * @description
 * @email pengcheng@missfresh.cn
 * @date 2018-11-05 17:30
 **/
public class BusinessLineVO implements Serializable {

    private String businessLineDesc;


    public String getBusinessLineDesc() {
        return businessLineDesc;
    }

    public void setBusinessLineDesc(String businessLineDesc) {
        this.businessLineDesc = businessLineDesc;
    }

    @Override
    public String toString() {
        return "BusinessLineVO{" +
                "businessLineDesc='" + businessLineDesc + '\'' +
                '}';
    }
}
