package com.mryx.matrix.project.enums;

/**
 * @author pengcheng
 * @description 优先级枚举类型
 * @email pengcheng@missfresh.cn
 * @date 2018-11-19 10:54
 **/
public enum PriorityEnum {


    P0("1", "P0", "This problem will block progress."),

    P1("2", "P1", "Serious problem that could block progress."),

    P2("3", "P2", "Has the potential to affect progress."),

    P3("4", "P3", "Minor problem or easily worked around."),

    P4("5", "P4", "Trivial problem with little or no impact on progress.");

    private String id;

    private String name;

    private String description;

    PriorityEnum(String id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    public static final PriorityEnum getPriorityByName(String name) {
        for (PriorityEnum priority : values()) {
            if (priority.name.equals(name)) {
                return priority;
            }
        }
        return null;
    }
}
