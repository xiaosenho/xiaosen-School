package com.xiaosenho.orders.service;

import com.xiaosenho.orders.model.dto.PayStatusDto;

import java.util.Map;

public interface PayService {
    /**
     * 发起支付请求，调用第三方支付接口
     * @param payNo
     * @return
     */
    public String doPost(String payNo);

    /**
     * 手动查询支付结果，调用第三方支付接口
     * @param payNo
     * @return
     */
    public PayStatusDto payResult(String payNo);

    /**
     * 验证数据合法性
     * @param params
     * @return
     */
    boolean check(Map<String, String> params);
}
