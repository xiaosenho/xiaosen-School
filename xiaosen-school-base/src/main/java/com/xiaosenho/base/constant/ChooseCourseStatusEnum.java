package com.xiaosenho.base.constant;

import lombok.Getter;

/**
 * 课程状态信息，用于选课表填充
 */
@Getter
public enum ChooseCourseStatusEnum {
    ENROLLMENT_SUCCESS("701001", "选课成功"),
    PENDING_PAYMENT("701002", "待支付");

    private final String code;
    private final String desc;

    ChooseCourseStatusEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
