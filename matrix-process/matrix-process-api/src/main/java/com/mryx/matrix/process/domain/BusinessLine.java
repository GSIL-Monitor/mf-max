package com.mryx.matrix.process.domain;

import java.io.Serializable;

/**
 * @author pengcheng
 * @description
 * @email pengcheng@missfresh.cn
 * @date 2018-11-05 21:41
 **/
public class BusinessLine implements Serializable {
    private String businessLineDesc;

    public String getBusinessLineDesc() {
        return businessLineDesc;
    }

    public void setBusinessLineDesc(String businessLineDesc) {
        this.businessLineDesc = businessLineDesc;
    }

    @Override
    public String toString() {
        return "BusinessLine{" +
                "businessLineDesc='" + businessLineDesc + '\'' +
                '}';
    }
}
