package com.mryx.matrix.codeanalyzer.enums;

/**
 * @program: matrix-codeanalyzer
 * @description: 代码评审结果状态枚举
 * @author: jianghong
 * @create: 2018-09-28 13:54
 */

public enum CodeReviewResultEnum {
    NEEDSREVIEW(0, "需要评审"),
    NEEDSREVISION(1, "需要修正"),
    ACCEPTED(2, "评审通过"),
    CLOSED(3, "评审结束"),
    ;

    /**
     * 编码
     */
    private Integer code;

    /**
     * 名称
     */
    private String name;

    CodeReviewResultEnum(Integer code, String name) {
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
     * @Description: 通过状态值获取状态名称
     * @Param: code 状态值
     * @return: 状态名称
     */

    public static String getName(int code) {
        for (CodeReviewResultEnum iterm : CodeReviewResultEnum.values()) {
            if (iterm.getCode() == code) {
                return iterm.getName();
            }
        }
        return null;
    }

}