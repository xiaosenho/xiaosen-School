package com.xiaosenho.base.constant;

import lombok.Getter;

/**
 * 业务订单类型
 */
@Getter
public enum BusinessOrderTypeEnum {
    COURSE_PURCHASE("60201", "购买课程"),
    LEARNING_MATERIALS("60202", "学习资料");

    private final String code;
    private final String desc;

    BusinessOrderTypeEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
