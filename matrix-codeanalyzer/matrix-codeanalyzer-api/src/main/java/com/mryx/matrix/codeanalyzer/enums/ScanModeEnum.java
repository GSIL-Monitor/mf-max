package com.mryx.matrix.codeanalyzer.enums;

public enum ScanModeEnum {
    //`mode_of_scan` smallint(1) NOT NULL DEFAULT '0' COMMENT '扫描模式,0全量扫描，1增量扫描',
    MODE_FULL(0,"全量扫描"),
    MODE_INCREMENT(1,"增量扫描")
    ;
    /**
     * 编码
     */
    private Integer code;

    /**
     * 名称
     */
    private String name;

    ScanModeEnum(Integer code, String name) {
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
        for (ScanModeEnum iterm : ScanModeEnum.values()) {
            if (iterm.getCode() == code) {
                return iterm.getName();
            }
        }
        return null;
    }
}
