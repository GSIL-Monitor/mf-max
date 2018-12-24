package com.mryx.matrix.process.enums;

/**
 * 优先级枚举
 *
 * @author zxl
 * @create 2018-09-04 15:35
 **/
public enum ProjectPriorityEnum {

    /**
     * 1：P1
     */
    PO(1, "P1"),

    /**
     * 2：P2
     */
    PT(2, "P2"),

    /**
     * 3：P3
     */
    PTH(3, "P3"),


    /**
     * 4：P4
     */
    PF(4, "P4");


    /**
     * 编码
     */
    private Integer code;

    /**
     * 名称
     */
    private String name;

    ProjectPriorityEnum(Integer code, String name) {
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
        for (ProjectPriorityEnum iterm : ProjectPriorityEnum.values()) {
            if (iterm.getCode() == code) {
                return iterm.getName();
            }
        }
        return null;
    }
}
