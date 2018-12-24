package com.mryx.matrix.publish.enums;

/**
 * TODO 状态code比乱
 *
 * @author dinglu
 * @date 2018/9/13
 */
public enum PublishStatusEnum {

    INIT(0, "未发布"),
    CODE_PUSH_SUCCESS(11, "代码合并成功"),
    CODE_PUSH_FAIL(12, "代码合并失败"),
    BUILD_SUCCESS(13, "编译成功"),
    BUILD_FAIL(14, "编译失败"),

    //beta发布状态
    BETA_WAIT(1, "待发布"),
    BETA_RELEASE(2, "发布中"),
    BETA_SUCCESS(3, "发布成功"),
    BETA_FAIL(4, "发布失败"),
    BETA_SUSPEND(5, "发布中断"),
    BETA_PART_SUCCESS(22, "部分发布完成"),

    //release发布状态
    RELEASE_WAIT(6, "待发布"),
    RELEASE_RELEASE(7, "发布中"),
    RELEASE_SUCCESS(8, "发布成功"),
    RELEASE_FAIL(9, "发布失败"),
    RELEASE_SUSPEND(10, "发布中断"),
    RELEASE_PART_SUCCESS(15, "部分发布完成"),

    //release回滚状态
    ROLLBACK_WAIT(16, "待回滚"),
    ROLLBACK_RELEASE(17, "回滚中"),
    ROLLBACK_SUCCESS(18, "回滚成功"),
    ROLLBACK_FAIL(19, "回滚失败"),
    ROLLBACK_SUSPEND(20, "回滚中断"),
    ROLLBACK_PART_SUCCESS(21, "部分回滚完成");

    private Integer code;
    private String value;

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    PublishStatusEnum(Integer code, String value) {
        this.code = code;
        this.value = value;
    }

    public static String getValueByCode(int code) {
        for (PublishStatusEnum item : values()) {
            if (item.getCode() == code) {
                return item.getValue();
            }
        }
        return null;
    }

    /**
     * 使用发布状态码生成回滚状态描述
     *
     * @param rollbackCode 发布状态码
     * @return 回滚描述
     */
    public static String convertRelease2RollbackDesc(int rollbackCode) {
        if (RELEASE_WAIT.getCode().equals(rollbackCode)) {
            return ROLLBACK_WAIT.getValue();
        } else if (RELEASE_RELEASE.getCode().equals(rollbackCode)) {
            return ROLLBACK_RELEASE.getValue();
        } else if (RELEASE_SUCCESS.getCode().equals(rollbackCode)) {
            return ROLLBACK_SUCCESS.getValue();
        } else if (RELEASE_FAIL.getCode().equals(rollbackCode)) {
            return ROLLBACK_FAIL.getValue();
        } else if (RELEASE_SUSPEND.getCode().equals(rollbackCode)) {
            return ROLLBACK_SUSPEND.getValue();
        } else if (RELEASE_PART_SUCCESS.getCode().equals(rollbackCode)) {
            return ROLLBACK_PART_SUCCESS.getValue();
        }
        return getValueByCode(rollbackCode);
    }
}
