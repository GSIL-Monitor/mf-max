package com.mryx.matrix.process.enums;

/**
 * 删除状态枚举
 *
 * @author zxl
 * @create 2018-09-04 15:35
 **/
public enum DelFlagEnum {
    //删除状态
    DEL_YES(0, "删除"),
    //未删除状态
    DEL_NO(1, "未删除"),

    ;

    /**
     * 编码
     */
    private Integer code;

    /**
     * 名称
     */
    private String name;

    DelFlagEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    public Integer getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    /**
     * 通过状态值获取状态名称
     *
     * @param code 状态值
     * @return 状态名称
     */
    public static String getName(int code) {
        for (DelFlagEnum iterm : DelFlagEnum.values()) {
            if (iterm.getCode() == code) {
                return iterm.getName();
            }
        }
        return null;
    }
}
