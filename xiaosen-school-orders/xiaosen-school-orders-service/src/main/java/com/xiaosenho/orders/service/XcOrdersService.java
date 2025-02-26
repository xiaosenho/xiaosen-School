package com.xiaosenho.orders.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xiaosenho.orders.model.dto.AddOrderDto;
import com.xiaosenho.orders.model.po.XcOrders;

/**
 * 订单业务逻辑
 */
public interface XcOrdersService extends IService<XcOrders> {

    XcOrders addOrder(String userId, AddOrderDto addOrderDto);
}
