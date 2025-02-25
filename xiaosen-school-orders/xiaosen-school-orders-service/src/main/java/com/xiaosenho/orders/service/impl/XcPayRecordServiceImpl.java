package com.xiaosenho.orders.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaosenho.base.constant.OrderTransactionStatusEnum;
import com.xiaosenho.base.constant.PaymentTransactionStatusEnum;
import com.xiaosenho.base.constant.ThirdPartyPaymentChannelEnum;
import com.xiaosenho.base.exception.ServiceException;
import com.xiaosenho.messagesdk.model.po.MqMessage;
import com.xiaosenho.messagesdk.service.MqMessageService;
import com.xiaosenho.orders.mapper.XcOrdersMapper;
import com.xiaosenho.orders.mapper.XcPayRecordMapper;
import com.xiaosenho.orders.model.dto.PayRecordDto;
import com.xiaosenho.orders.model.dto.PayStatusDto;
import com.xiaosenho.orders.model.po.XcOrders;
import com.xiaosenho.orders.model.po.XcPayRecord;
import com.xiaosenho.orders.service.PayService;
import com.xiaosenho.orders.service.XcPayRecordService;
import com.xiaosenho.orders.utils.IdWorkerUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.KafkaClient;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.BeanUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;

/**
 * @author: 作者
 * @create: 2025-02-25 13:46
 * @Description:
 */
@Service
@Slf4j
public class XcPayRecordServiceImpl extends ServiceImpl<XcPayRecordMapper,XcPayRecord> implements XcPayRecordService {
    @Resource
    XcOrdersMapper xcOrdersMapper;
    @Resource
    XcPayRecordService currentProxy;
    @Resource
    ApplicationContext context;
    @Resource
    MqMessageService mqMessageService;
    @Resource
    KafkaTemplate kafkaTemplate;
    @Override
    public XcPayRecord addPayRecord(XcOrders xcOrders) {
        if(xcOrders==null){
            ServiceException.cast("订单不存在");
        }
        if(xcOrders.getStatus().equals(OrderTransactionStatusEnum.UNPAID.getCode())){
            ServiceException.cast("订单已支付");
        }
        XcPayRecord payRecord = new XcPayRecord();
        //生成支付交易流水号,将支付交易流水号和订单号解耦，保证同一订单即使交易失败下次支付时不会重复创建订单
        long payNo = IdWorkerUtils.getInstance().nextId();
        payRecord.setPayNo(payNo);
        payRecord.setOrderId(xcOrders.getId());//商品订单号
        payRecord.setOrderName(xcOrders.getOrderName());
        payRecord.setTotalPrice(xcOrders.getTotalPrice());
        // 支付渠道类型，目前只支持支付宝
        payRecord.setOutPayChannel(ThirdPartyPaymentChannelEnum.ALIPAY.getCode());
        payRecord.setCurrency("CNY");
        payRecord.setCreateDate(LocalDateTime.now());
        payRecord.setStatus(PaymentTransactionStatusEnum.NOT_PAID.getCode());//未支付
        payRecord.setUserId(xcOrders.getUserId());
        save(payRecord);
        return payRecord;
    }

    @Override
    public PayRecordDto queryPayResult(String payNo) {
        // 查询交易记录
        XcPayRecord payRecord = getOne(new LambdaQueryWrapper<XcPayRecord>()
                .eq(XcPayRecord::getPayNo, payNo));
        // 判断交易记录是否存在
        if(payRecord==null){
            ServiceException.cast("交易不存在，请重新扫码支付");
        }
        // 判断是否已支付,如果已经支付直接返回
        if(PaymentTransactionStatusEnum.PAID.getCode().equals(payRecord.getStatus())){
            PayRecordDto payRecordDto = new PayRecordDto();
            BeanUtils.copyProperties(payRecord, payRecordDto);
            return payRecordDto;
        }
        PayStatusDto payStatusDto = null;

        //使用支付宝第三方渠道
        if(ThirdPartyPaymentChannelEnum.ALIPAY.getCode().equals(payRecord.getOutPayChannel())){
            // 调用支付宝查询接口查询交易是否完成
            PayService aliPayService = context.getBean("AliPayService", PayService.class);
            payStatusDto = aliPayService.payResult(payNo);
            // 将支付宝查询结果添加到数据库表中
            currentProxy.saveAliPayStatus(payStatusDto);
        }  //可以后续扩展其它渠道

        // 重新查询支付记录
        payRecord = getOne(new LambdaQueryWrapper<XcPayRecord>()
                .eq(XcPayRecord::getPayNo, payNo));
        PayRecordDto payRecordDto = new PayRecordDto();
        BeanUtils.copyProperties(payRecord, payRecordDto);
        return payRecordDto;
    }

    @Transactional
    @Override
    public void saveAliPayStatus(PayStatusDto payStatusDto) {
        //支付流水号
        String payNo = payStatusDto.getOut_trade_no();
        XcPayRecord payRecord = getOne(new LambdaQueryWrapper<XcPayRecord>()
                .eq(XcPayRecord::getPayNo, payNo));
        if (payRecord == null) {
            ServiceException.cast("支付记录找不到");
        }
        //支付结果
        String trade_status = payStatusDto.getTrade_status();
        log.debug("收到支付结果:{},支付记录:{}}", payStatusDto.toString(),payRecord.toString());
        if (trade_status.equals("TRADE_SUCCESS")) {

            //支付金额变为分
            Float totalPrice = payRecord.getTotalPrice() * 100;
            Float total_amount = Float.parseFloat(payStatusDto.getTotal_amount()) * 100;
            //校验是否一致
            if (totalPrice.intValue() != total_amount.intValue()) {
                //校验失败
                log.info("校验支付结果失败,支付记录:{},APP_ID:{},totalPrice:{}" ,payRecord.toString(),payStatusDto.getApp_id(),total_amount.intValue());
                ServiceException.cast("校验支付结果失败");
            }
            log.debug("更新支付结果,支付交易流水号:{},支付结果:{}", payNo, trade_status);
            XcPayRecord payRecord_u = new XcPayRecord();
            payRecord_u.setStatus(PaymentTransactionStatusEnum.PAID.getCode());//支付成功
            payRecord_u.setOutPayChannel(ThirdPartyPaymentChannelEnum.ALIPAY.getCode());
            payRecord_u.setOutPayNo(payStatusDto.getTrade_no());//支付宝交易号
            payRecord_u.setPaySuccessTime(LocalDateTime.now());//通知时间
            boolean update1 = update(payRecord_u, new LambdaQueryWrapper<XcPayRecord>().eq(XcPayRecord::getPayNo, payNo));
            if (update1) {
                log.info("更新支付记录状态成功:{}", payRecord_u.toString());
            } else {
                log.info("更新支付记录状态失败:{}", payRecord_u.toString());
                ServiceException.cast("更新支付记录状态失败");
            }
            //关联的订单号
            Long orderId = payRecord.getOrderId();
            XcOrders orders = xcOrdersMapper.selectById(orderId);
            if (orders == null) {
                log.info("根据支付记录[{}}]找不到订单", payRecord_u.toString());
                ServiceException.cast("根据支付记录找不到订单");
            }
            XcOrders order_u = new XcOrders();
            order_u.setStatus(OrderTransactionStatusEnum.PAID.getCode());//支付成功
            int update = xcOrdersMapper.update(order_u, new LambdaQueryWrapper<XcOrders>().eq(XcOrders::getId, orderId));
            if (update > 0) {
                log.info("更新订单表状态成功,订单号:{}", orderId);
            } else {
                log.info("更新订单表状态失败,订单号:{}", orderId);
                ServiceException.cast("更新订单表状态失败");
            }
            // 插入消息表中，等待处理
            // 保存消息记录,参数1：支付结果通知类型，2: 业务id，3:业务类型
            MqMessage mqMessage = mqMessageService.addMessage("payresult_notify", orders.getOutBusinessId(), orders.getOrderType(), null);

            // 生产者发送消息到kafka消息队列
            String msg = JSON.toJSONString(mqMessage);
            // 根据业务类型划分topic
            String topic = "payresult_notify";
            String key = String.valueOf(mqMessage.getId());
            String value = msg;
            kafkaTemplate.send(topic,key,value).addCallback(
                    success -> {
                        // 消息发送成功,移除数据库持久化的消息信息
                        log.debug("通知支付结果消息发送成功, ID:{}", mqMessage.getId());
                        mqMessageService.completed(mqMessage.getId());
                    },
                    failure -> {
                        // 消息发送失败，有数据库消息表兜底，定时任务重新发送
                        log.error("通知支付结果消息发送失败, ID:{}, 原因{}",mqMessage.getId(), failure.getMessage());
                    }
            );
        }
    }
}
