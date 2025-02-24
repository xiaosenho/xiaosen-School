package com.xiaosenho.base.constant;

/**
 * @author: 作者
 * @create: 2025-02-24 17:12
 * @Description:
 */

import lombok.Getter;

@Getter
public enum CourseChargeEnum {

    FREE("201000", "免费"),
    PAID("201001", "收费");

    private final String code;
    private final String description;

    CourseChargeEnum(String code, String description) {
        this.code = code;
        this.description = description;
    }
}
