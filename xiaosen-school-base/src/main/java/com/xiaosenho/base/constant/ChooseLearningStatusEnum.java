package com.xiaosenho.base.constant;

import lombok.Getter;

// 学习资格判断，用于接口返回
@Getter
public enum ChooseLearningStatusEnum {
    NORMAL_LEARNING("702001", "正常学习"),
    NO_ENROLLMENT_OR_PAYMENT("702002", "没有选课或选课后没有支付"),
    EXPIRED_NEED_RENEW_OR_PAY("702003", "已过期需要申请续期或重新支付");

    private final String code;
    private final String desc;

    ChooseLearningStatusEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
