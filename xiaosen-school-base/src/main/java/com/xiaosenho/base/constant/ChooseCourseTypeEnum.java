package com.xiaosenho.base.constant;

import lombok.Getter;

// 课程收费类型，用于选课表填充
@Getter
public enum ChooseCourseTypeEnum {
    FREE_COURSE("700001", "免费课程"),
    PAID_COURSE("700002", "收费课程");

    private final String code;
    private final String desc;

    ChooseCourseTypeEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
