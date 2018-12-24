package com.mryx.matrix.process.enums;

/**
 * 删除状态枚举
 *
 * @author zxl
 * @create 2018-09-04 15:35
 **/
public enum ProjectProcessStatusEnum {

    /**
     * 1：项目初始完成
     */
    CREAT(0, "项目创建成功"),

    /**
     * 1：项目初始完成
     */
    CREATSUCESS(1, "开发阶段"),

    /**
     * 2：提测阶段
     */
    COMMIT_TEST(2, "提测阶段"),

    /**
     * 3：测试阶段
     */
    TESTING(3, "测试发布阶段"),


    /**
     * 4：发布阶段
     */
    PUBLISH(4, "生产发布阶段"),

    /**
     * 5：发布完成
     */
    PUBLISH_FINISHI(5, "发布完成");

    /**
     * 编码
     */
    private Integer code;

    /**
     * 名称
     */
    private String name;

    ProjectProcessStatusEnum(Integer code, String name) {
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
        for (ProjectProcessStatusEnum iterm : ProjectProcessStatusEnum.values()) {
            if (iterm.getCode() == code) {
                return iterm.getName();
            }
        }
        return null;
    }
}
