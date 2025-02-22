package com.xiaosenho.base.constant;

import lombok.Getter;

@Getter
public enum CoursePublishStatusEnum {
    NOT_PUBLISHED("203001", "未发布"),
    PUBLISHED("203002", "已发布"),
    OFFLINE("203003", "下线");

    private final String code;
    private final String description;

    CoursePublishStatusEnum(String code, String description) {
        this.code = code;
        this.description = description;
    }
}