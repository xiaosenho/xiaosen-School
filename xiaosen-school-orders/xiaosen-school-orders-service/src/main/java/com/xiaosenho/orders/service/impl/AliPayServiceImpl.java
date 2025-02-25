package com.xiaosenho.orders.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.request.AlipayTradeWapPayRequest;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiaosenho.base.constant.PaymentTransactionStatusEnum;
import com.xiaosenho.base.exception.ServiceException;
import com.xiaosenho.orders.config.AlipayConfig;
import com.xiaosenho.orders.mapper.XcPayRecordMapper;
import com.xiaosenho.orders.model.dto.PayStatusDto;
import com.xiaosenho.orders.model.po.XcPayRecord;
import com.xiaosenho.orders.service.PayService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Map;

/**
 * @author: 作者
 * @create: 2025-02-25 15:32
 * @Description:
 */
@Service("AliPayService")
@Slf4j
public class AliPayServiceImpl implements PayService {
    @Value("${pay.alipay.APP_ID}")
    String APP_ID;
    @Value("${pay.alipay.APP_PRIVATE_KEY}")
    String APP_PRIVATE_KEY;
    @Value("${pay.alipay.ALIPAY_PUBLIC_KEY}")
    String ALIPAY_PUBLIC_KEY;
    @Resource
    XcPayRecordMapper xcPayRecordMapper;

    public String doPost(String payNo) {
        // 根据payNo查询支付记录
        XcPayRecord xcPayRecord = xcPayRecordMapper.selectOne(new LambdaQueryWrapper<XcPayRecord>()
                .eq(XcPayRecord::getPayNo, payNo));
        if(xcPayRecord==null){
            ServiceException.cast("支付信息失效");
        }
        if(PaymentTransactionStatusEnum.PAID.getCode().equals(xcPayRecord.getStatus())){
            ServiceException.cast("已支付，请勿重复支付");
        }

        // 使用支付宝客户端发起支付请求
        AlipayClient alipayClient = new DefaultAlipayClient(AlipayConfig.URL, APP_ID, APP_PRIVATE_KEY, AlipayConfig.FORMAT, AlipayConfig.CHARSET, ALIPAY_PUBLIC_KEY,AlipayConfig.SIGNTYPE);
        //获得初始化的AlipayClient
        AlipayTradeWapPayRequest alipayRequest = new AlipayTradeWapPayRequest();//创建API对应的request
//        alipayRequest.setReturnUrl("http://domain.com/CallBack/return_url.jsp");
        alipayRequest.setNotifyUrl("https://56b50e8.r26.cpolar.top/orders/paynotify");//在公共参数中设置回跳和通知地址
        alipayRequest.setBizContent("{" +
                "    \"out_trade_no\":\"" + xcPayRecord.getPayNo() +"\"," +
                "    \"total_amount\":" + xcPayRecord.getTotalPrice() +"," +
                "    \"subject\":\"" + xcPayRecord.getOrderName() +"\"," +
                "    \"product_code\":\"QUICK_WAP_WAY\"" +
                "  }");//填充业务参数
        try {
            return alipayClient.pageExecute(alipayRequest).getBody();
        } catch (AlipayApiException e) {
            ServiceException.cast("支付出错");
        }
        return null;
    }


    /**
     * 主动查询支付支付结果
     * @param payNo
     * @return
     */
    public PayStatusDto payResult(String payNo) {
        AlipayClient alipayClient = new DefaultAlipayClient(AlipayConfig.URL, APP_ID, APP_PRIVATE_KEY, AlipayConfig.FORMAT, AlipayConfig.CHARSET, ALIPAY_PUBLIC_KEY,AlipayConfig.SIGNTYPE);
        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        JSONObject bizContent = new JSONObject();
        bizContent.put("out_trade_no", payNo);
//bizContent.put("trade_no", "2014112611001004680073956707");
        request.setBizContent(bizContent.toString());
        AlipayTradeQueryResponse response = null;
        try {
            response = alipayClient.execute(request);
            if (!response.isSuccess()) {
                ServiceException.cast("请求支付查询查询失败");
            }
        } catch (AlipayApiException e) {
            ServiceException.cast("请求支付查询查询失败");
        }
        //获取支付结果
        String resultJson = response.getBody();
        //转map
        Map resultMap = JSON.parseObject(resultJson, Map.class);
        Map alipay_trade_query_response = (Map) resultMap.get("alipay_trade_query_response");
        //支付结果
        String trade_status = (String) alipay_trade_query_response.get("trade_status");
        String total_amount = (String) alipay_trade_query_response.get("total_amount");
        String trade_no = (String) alipay_trade_query_response.get("trade_no");
        //保存支付结果
        PayStatusDto payStatusDto = new PayStatusDto();
        payStatusDto.setOut_trade_no(payNo);
        payStatusDto.setTrade_status(trade_status);
        payStatusDto.setApp_id(APP_ID);
        payStatusDto.setTrade_no(trade_no);
        payStatusDto.setTotal_amount(total_amount);
        return payStatusDto;

    }

    @Override
    public boolean check(Map<String, String> params) {
        try {
            return AlipaySignature.rsaCheckV1(params, ALIPAY_PUBLIC_KEY, AlipayConfig.CHARSET, "RSA2");
        } catch (AlipayApiException e) {
            ServiceException.cast("支付宝验证异常");
        }
        return false;
    }
}
