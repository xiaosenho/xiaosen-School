package com.xiaosenho.learning.service.impl;

import com.alibaba.fastjson.JSON;
import com.xiaosenho.base.constant.BusinessOrderTypeEnum;
import com.xiaosenho.base.exception.ServiceException;
import com.xiaosenho.learning.mapper.XcChooseCourseMapper;
import com.xiaosenho.learning.service.XcCourseTablesService;
import com.xiaosenho.messagesdk.model.po.MqMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author: 作者
 * @create: 2025-02-25 21:48
 * @Description:
 */
@Service
@Slf4j
public class ReceivePayNotifyService {
    @Resource
    private XcCourseTablesService xcCourseTablesService;


    //默认从消息队列当前offset，即最新消息中获取
    @KafkaListener(topics = "payresult_notify",groupId = "order-service")
    public void listen(ConsumerRecord record, Acknowledgment acknowledgment){
        //获取消息
        MqMessage mqMessage = JSON.parseObject((String) record.value(), MqMessage.class);
        log.debug("学习中心服务接收支付结果:{}", mqMessage);

        //消息类型
        String messageType = mqMessage.getMessageType();
        //订单类型,60201表示购买课程
        String businessKey2 = mqMessage.getBusinessKey2();
        //这里只处理购买课程的支付结果通知
        if (BusinessOrderTypeEnum.COURSE_PURCHASE.getCode().equals(businessKey2)) {
            //选课记录id
            String choosecourseId = mqMessage.getBusinessKey1();
            //添加选课
            boolean b = xcCourseTablesService.addCourseTables(choosecourseId);
            if(!b){
                //添加选课失败，抛出异常，消息重回队列
                ServiceException.cast("收到支付结果，添加选课失败");
            }
            // 消息处理成功后手动提交偏移量
            acknowledgment.acknowledge();
        }

    }
}
