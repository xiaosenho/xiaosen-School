package com.xiaosenho.orders.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xiaosenho.orders.model.dto.PayRecordDto;
import com.xiaosenho.orders.model.dto.PayStatusDto;
import com.xiaosenho.orders.model.po.XcOrders;
import com.xiaosenho.orders.model.po.XcPayRecord;

/**
 * 支付记录业务逻辑
 */
public interface XcPayRecordService extends IService<XcPayRecord> {
    XcPayRecord addPayRecord(XcOrders xcOrders);

    PayRecordDto queryPayResult(String payNo);

    void saveAliPayStatus(PayStatusDto payStatusDto);
}
