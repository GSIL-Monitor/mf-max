package com.mryx.matrix.common.enums;

/**
 * 包类型/项目类型
 *
 * @author supeng
 * @date 2018/09/13
 */
public enum PackageType {

    JAR("jar","jar包"),JETTY("jetty","war包jetty"),TOMCAT("tomcat","war包tomcat"),GO("go","golang项目"),PYTHON("python","python项目"),STATIC("static","前端项目"),JARAPI("jar-api","api项目"),WMS_CONF("wms-conf","wms js后端项目"),WMS_WWWROOT("wms-wwwroot","wms js前端项目"),SPRING_GZ("spring_gz","spring-gz包项目"),JAR_MAIN("jar-main","jar-main项目");
    /**
     * 值
     */
    private String value;
    /**
     * 描述
     */
    private String comment;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    PackageType(String value, String comment) {
        this.value = value;
        this.comment = comment;
    }
}
