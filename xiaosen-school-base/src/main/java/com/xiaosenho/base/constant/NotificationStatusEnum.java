package com.xiaosenho.base.constant;

import lombok.Getter;

@Getter
public enum NotificationStatusEnum {
    NOT_NOTIFIED("003001", "未通知"),
    SUCCESS("003002", "成功");

    private final String code;
    private final String desc;

    NotificationStatusEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
