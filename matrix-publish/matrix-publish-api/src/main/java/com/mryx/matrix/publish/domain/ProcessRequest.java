package com.mryx.matrix.publish.domain;

import com.mryx.matrix.common.domain.Base;

import java.io.Serializable;
import java.util.HashMap;

/**
 *
 * @author dinglu
 * @date 2018/9/27
 */
public class ProcessRequest extends Base implements Serializable {

    private static final long serialVersionUID = -781465814858953059L;
    private HashMap<String,Integer> recordMap;

    public HashMap<String, Integer> getRecordMap() {
        return recordMap;
    }

    public void setRecordMap(HashMap<String, Integer> recordMap) {
        this.recordMap = recordMap;
    }

    @Override
    public String toString() {
        return "ProcessRequest{" +
                "recordMap=" + recordMap +
                '}';
    }
}
