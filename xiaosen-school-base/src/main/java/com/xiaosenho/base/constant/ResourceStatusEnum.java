package com.xiaosenho.base.constant;

import lombok.Getter;

/**
 * @author: 作者
 * @create: 2025-02-19 12:11
 * @Description:
 */
@Getter
public enum ResourceStatusEnum {
    ACTIVE("1", 1, "使用态"),
    DELETED("0", 0, "删除态"),
    TEMPORARY("-1", -1, "暂时态");

    private final String code;
    private final int codeInt;
    private final String description;

    ResourceStatusEnum(String code, int codeInt, String description) {
        this.code = code;
        this.codeInt = codeInt;
        this.description = description;
    }
}
