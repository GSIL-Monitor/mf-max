package com.mryx.matrix.publish.enums;

/**
 * Created by dinglu on 2018/11/5.
 */
public enum NexusTag {

    mryx(0,"主商城"),
    missfresh(1,"便利购"),
    /**
     * 官方NPM镜像
     */
    officialnpm(2,"http://registry.npmjs.org"),
    /**
     * 淘宝NPM镜像
     */
    taobaonpm(3,"https://registry.npm.taobao.org"),
    /**
     * 物流NPM镜像
     */
    wuliunpm(4,"http://10.2.4.224:4873");

    private Integer code;
    private String value;

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    NexusTag(Integer code, String value) {
        this.code = code;
        this.value = value;
    }


}
