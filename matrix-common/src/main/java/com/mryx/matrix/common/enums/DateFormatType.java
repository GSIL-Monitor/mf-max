package com.mryx.matrix.common.enums;

/**
 * 日期格式化类型
 *
 * @author supeng
 * @date 2018/09/28
 */
public enum DateFormatType {
    /**
     * yyyyMMdd
     */
    yyyyMMdd("yyyyMMdd"),
    /**
     * yyyy-MM-dd
     */
    yyyy_MM_dd("yyyy-MM-dd"),
    /**
     * yyyyMMddHHmmss
     */
    yyyyMMddHHmmss("yyyyMMddHHmmss"),
    /**
     * yyyy-MM-dd HH:mm:ss
     */
    yyyy_MM_dd_HH_mm_ss("yyyy-MM-dd HH:mm:ss"),
    /**
     * yyyyMMddHHmmssSSS
     */
    yyyyMMddHHmmssSSS("yyyyMMddHHmmssSSS"),
    /**
     * yyyy-MM-dd HH:mm:ss:SSS
     */
    yyyy_MM_dd_HH_mm_ss_SSS("yyyy-MM-dd HH:mm:ss:SSS");

    private String format;

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    DateFormatType(String format) {
        this.format = format;
    }
}
