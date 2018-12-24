package com.mryx.matrix.codeanalyzer.enums;

public enum ScanTypeEnum {
    //`type_of_scan` smallint(1) NOT NULL DEFAULT '0' COMMENT '扫描类型,0sonar扫描，1pmd扫描'
    TYPE_SONAR(0, "代码质量评估"),
    TYPE_PMD(1, "Java代码规范检查"),
    TYPE_P3C(2,"静态代码扫描");
    /**
     * 编码
     */
    private Integer code;

    /**
     * 名称
     */
    private String name;

    ScanTypeEnum(Integer code, String name) {
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
        for (ScanTypeEnum iterm : ScanTypeEnum.values()) {
            if (iterm.getCode() == code) {
                return iterm.getName();
            }
        }
        return null;
    }
}
