package com.xiaosenho.base.constant;
import lombok.Getter;
/**
 * @author: 作者
 * @create: 2025-02-25 19:37
 * @Description: 订单交易类型状态
 */

@Getter
public enum OrderTransactionStatusEnum {
    UNPAID("600001", "未支付"),
    PAID("600002", "已支付"),
    CLOSED("600003", "已关闭"),
    REFUNDED("600004", "已退款"),
    COMPLETED("600005", "已完成");

    private final String code;
    private final String desc;

    OrderTransactionStatusEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
