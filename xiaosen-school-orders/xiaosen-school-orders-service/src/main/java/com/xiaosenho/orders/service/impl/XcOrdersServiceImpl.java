package com.xiaosenho.orders.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaosenho.base.constant.OrderTransactionStatusEnum;
import com.xiaosenho.base.constant.PaymentTransactionStatusEnum;
import com.xiaosenho.orders.mapper.XcOrdersGoodsMapper;
import com.xiaosenho.orders.mapper.XcOrdersMapper;
import com.xiaosenho.orders.model.dto.AddOrderDto;
import com.xiaosenho.orders.model.po.XcOrders;
import com.xiaosenho.orders.model.po.XcOrdersGoods;
import com.xiaosenho.orders.service.XcOrdersService;
import com.xiaosenho.orders.utils.IdWorkerUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author: 作者
 * @create: 2025-02-25 13:47
 * @Description:
 */
@Service
public class XcOrdersServiceImpl extends ServiceImpl<XcOrdersMapper,XcOrders> implements XcOrdersService {

    @Resource
    private XcOrdersGoodsMapper xcOrdersGoodsMapper;

    @Transactional
    @Override
    public XcOrders addOrder(String userId, AddOrderDto addOrderDto) {
        // 根据业务id查询订单是否已经创建，业务id本身是唯一的（如选课记录）
        LambdaQueryWrapper<XcOrders> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(XcOrders::getOutBusinessId, addOrderDto.getOutBusinessId())
                .eq(XcOrders::getOrderType, addOrderDto.getOrderType());
        XcOrders xcOrders = getOne(queryWrapper);
        if(xcOrders!=null){//订单已经存在，直接返回，不重复创建
            return xcOrders;
        }
        //创建订单
        xcOrders = new XcOrders();
        BeanUtils.copyProperties(addOrderDto,xcOrders);
        // 订单id生成
        long orderId = IdWorkerUtils.getInstance().nextId();
        xcOrders.setUserId(userId);
        xcOrders.setCreateDate(LocalDateTime.now());
        xcOrders.setStatus(OrderTransactionStatusEnum.UNPAID.getCode());// 交易状态初始化为未支付
        save(xcOrders); //插入数据库订单表

        // 将订单详细内容（即订单项）进行解析，逐个插入到订单详细表中
        String orderDetailJson = addOrderDto.getOrderDetail();
        List<XcOrdersGoods> xcOrdersGoodsList = JSON.parseArray(orderDetailJson, XcOrdersGoods.class);
        xcOrdersGoodsList.forEach(goods->{
            XcOrdersGoods xcOrdersGoods = new XcOrdersGoods();
            BeanUtils.copyProperties(goods,xcOrdersGoods);
            xcOrdersGoods.setOrderId(orderId);//订单号
            xcOrdersGoodsMapper.insert(xcOrdersGoods);
        });
        return xcOrders;
    }
}
