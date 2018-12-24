package com.mryx.matrix.project.enums;

/**
 * 项目状态枚举
 *
 * @author zxl
 * @create 2018-09-04 15:35
 **/
public enum ProjectTypeEnum {

    /**
     * 1：产品需求
     */
    PR(1, "产品需求"),

    /**
     * 2：技术需求
     */
    TR(2, "技术需求"),

    /**
     * 3：紧急需求
     */
    ER(3, "紧急需求"),


    /**
     * 4：线上bug
     */
    OB(4, "线上bug");


    /**
     * 编码
     */
    private Integer code;

    /**
     * 名称
     */
    private String name;

    ProjectTypeEnum(Integer code, String name) {
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
        for (ProjectTypeEnum iterm : ProjectTypeEnum.values()) {
            if (iterm.getCode() == code) {
                return iterm.getName();
            }
        }
        return null;
    }
}
