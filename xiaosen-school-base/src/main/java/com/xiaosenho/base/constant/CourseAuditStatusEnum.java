package com.xiaosenho.base.constant;

import lombok.Getter;

/**
 * @author: 作者
 * @create: 2025-02-19 12:09
 * @Description:
 */
@Getter
public enum CourseAuditStatusEnum {
    AUDIT_NOT_PASSED("202001", "审核未通过"),
    NOT_SUBMITTED("202002", "未提交"),
    SUBMITTED("202003", "已提交"),
    AUDIT_PASSED("202004", "审核通过");

    private final String code;
    private final String description;

    CourseAuditStatusEnum(String code, String description) {
        this.code = code;
        this.description = description;
    }
}
