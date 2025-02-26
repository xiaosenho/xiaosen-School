package com.xiaosenho.base.constant;

/**
 * @author: 作者
 * @create: 2025-02-25 19:37
 * @Description: 支付记录交易状态
 */
import lombok.Getter;

@Getter
public enum PaymentTransactionStatusEnum {
    NOT_PAID("601001", "未支付"),
    PAID("601002", "已支付"),
    REFUNDED("601003", "已退款");

    private final String code;
    private final String desc;

    PaymentTransactionStatusEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

}
