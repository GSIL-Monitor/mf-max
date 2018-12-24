package com.mryx.matrix.codeanalyzer.enums;

public enum RunStatusEnum {
    STATUS_SUCCESS(0,"运行成功"),
    STATUS_FAIL(1,"异常失败"),
    STATUS_NOTRUN(2,"未运行"),
    STATUS_BUILDFAIL(3,"编译失败"),
    STATUS_NOPMD(4,"非Java代码，无法进行PMD扫描"),
    STATUS_RUNING(5,"运行中"),
    STATUS_TIMEOUT(6,"运行超时")
    ;

    /**
     * 编码
     */
    private Integer code;

    /**
     * 名称
     */
    private String name;

    RunStatusEnum(Integer code, String name) {
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
        for (RunStatusEnum iterm : RunStatusEnum.values()) {
            if (iterm.getCode() == code) {
                return iterm.getName();
            }
        }
        return null;
    }
}
