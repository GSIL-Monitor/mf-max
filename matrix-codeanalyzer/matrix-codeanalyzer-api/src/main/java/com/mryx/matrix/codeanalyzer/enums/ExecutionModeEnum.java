package com.mryx.matrix.codeanalyzer.enums;

public enum ExecutionModeEnum {
    //`manual_or_automatic` smallint(1) NOT NULL DEFAULT '0' COMMENT '创建扫描任务方式,0手动，1自动，2定时任务',
    MODE_MANUAL(0, "手动"),
    MODE_AUTOMATIC(1, "自动"),
    MODE_TIME(2, "定时任务");
    /**
     * 编码
     */
    private Integer code;

    /**
     * 名称
     */
    private String name;

    ExecutionModeEnum(Integer code, String name) {
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
        for (ExecutionModeEnum iterm : ExecutionModeEnum.values()) {
            if (iterm.getCode() == code) {
                return iterm.getName();
            }
        }
        return null;
    }
}
