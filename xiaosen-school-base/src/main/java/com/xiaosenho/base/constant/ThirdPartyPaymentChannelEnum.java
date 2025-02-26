package com.xiaosenho.base.constant;
/**
 * 第三方交易渠道类型
 */

import lombok.Getter;

@Getter
public enum ThirdPartyPaymentChannelEnum {
    WECHAT_PAY("603001", "微信支付"),
    ALIPAY("603002", "支付宝");

    private final String code;
    private final String desc;

    ThirdPartyPaymentChannelEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

}
