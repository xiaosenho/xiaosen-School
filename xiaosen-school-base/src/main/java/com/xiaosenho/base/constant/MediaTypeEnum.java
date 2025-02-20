package com.xiaosenho.base.constant;

import lombok.Getter;

@Getter
public enum MediaTypeEnum {
    IMAGE("001001", "图片"),
    VIDEO("001002", "视频"),
    OTHER("001003", "其它");

    private final String code;
    private final String description;

    MediaTypeEnum(String code, String description) {
        this.code = code;
        this.description = description;
    }
}
