package com.xiaosenho.orders.api;

import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import com.xiaosenho.base.exception.ServiceException;
import com.xiaosenho.base.utils.QRCodeUtil;
import com.xiaosenho.orders.config.AlipayConfig;
import com.xiaosenho.orders.model.dto.AddOrderDto;
import com.xiaosenho.orders.model.dto.PayRecordDto;
import com.xiaosenho.orders.model.dto.PayStatusDto;
import com.xiaosenho.orders.model.po.XcOrders;
import com.xiaosenho.orders.model.po.XcPayRecord;
import com.xiaosenho.orders.service.XcOrdersService;
import com.xiaosenho.orders.service.XcPayRecordService;
import com.xiaosenho.orders.service.impl.AliPayServiceImpl;
import com.xiaosenho.orders.util.SecurityUtil;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author: 作者
 * @create: 2025-02-25 13:17
 * @Description:
 */
@RestController
@ApiOperation("订单接口")
public class OrderController {
    @Resource
    private XcOrdersService xcOrdersService;
    @Resource
    private XcPayRecordService xcPayRecordService;
    @Resource
    private AliPayServiceImpl aliPayServiceImpl;

    @PostMapping("/generatepaycode")
    @ApiOperation("生成支付码")
    public PayRecordDto generatePayCode(@RequestBody AddOrderDto addOrderDto) {
        SecurityUtil.XcUser user = SecurityUtil.getUser();
        if(user==null){
            ServiceException.cast("登录后才能交易");
        }
        QRCodeUtil qrCodeUtil = new QRCodeUtil();
        // 生成订单信息
        XcOrders xcOrders = xcOrdersService.addOrder(user.getId(),addOrderDto);
        // 新增支付信息
        // TODO 根据传入的不同支付类型生成不同渠道支付二维码
        XcPayRecord xcPayRecord = xcPayRecordService.addPayRecord(xcOrders);
        PayRecordDto payRecordDto = new PayRecordDto();
        BeanUtils.copyProperties(xcPayRecord,payRecordDto);
        try {
            String content = "http://192.168.0.110:63030/orders/requestpay?payNo="+xcPayRecord.getPayNo();
            String qrCode = qrCodeUtil.createQRCode(content, 300, 300);
            payRecordDto.setQrcode(qrCode);
        } catch (IOException e) {
            ServiceException.cast("生成支付码失败");
        }
        return payRecordDto;
    }

    @ApiOperation("支付宝扫码下单接口")
    @GetMapping("/requestpay")
    public void requestpay(String payNo, HttpServletResponse httpResponse) throws IOException {
        if(StringUtils.isEmpty(payNo)){
            ServiceException.cast("支付码错误");
        }
        String form = aliPayServiceImpl.doPost(payNo);
        httpResponse.setContentType("text/html;charset=" + AlipayConfig.CHARSET);
        httpResponse.getWriter().write(form);//直接将完整的表单html输出到页面
        httpResponse.getWriter().flush();
    }

    @ApiOperation("主动查询支付结果")
    @GetMapping("/payresult")
    public PayRecordDto payresult(String payNo) throws IOException {
        return xcPayRecordService.queryPayResult(payNo);
    }

    @ApiOperation("接收支付宝支付结果通知")
    @PostMapping("/receivenotify")
    public void receivenotify(HttpServletRequest request) throws UnsupportedEncodingException {
        Map<String,String> params = new HashMap<String,String>();
        Map requestParams = request.getParameterMap();
        for (Iterator iter = requestParams.keySet().iterator(); iter.hasNext();) {
            String name = (String) iter.next();
            String[] values = (String[]) requestParams.get(name);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i]
                        : valueStr + values[i] + ",";
            }
            params.put(name, valueStr);
        }

        //验签
        boolean verify_result = aliPayServiceImpl.check(params);

        if(verify_result) {//验证成功

            //商户订单号
            String out_trade_no = new String(request.getParameter("out_trade_no").getBytes("ISO-8859-1"),"UTF-8");
            //支付宝交易号
            String trade_no = new String(request.getParameter("trade_no").getBytes("ISO-8859-1"),"UTF-8");
            //交易状态
            String trade_status = new String(request.getParameter("trade_status").getBytes("ISO-8859-1"),"UTF-8");
            //appid
            String app_id = new String(request.getParameter("app_id").getBytes("ISO-8859-1"),"UTF-8");
            //total_amount
            String total_amount = new String(request.getParameter("total_amount").getBytes("ISO-8859-1"),"UTF-8");

            //交易成功处理
            if (trade_status.equals("TRADE_SUCCESS")) {

                PayStatusDto payStatusDto = new PayStatusDto();
                payStatusDto.setOut_trade_no(out_trade_no);
                payStatusDto.setTrade_status(trade_status);
                payStatusDto.setApp_id(app_id);
                payStatusDto.setTrade_no(trade_no);
                payStatusDto.setTotal_amount(total_amount);

                //处理逻辑,保存交易结果到数据库
                xcPayRecordService.saveAliPayStatus(payStatusDto);
            }
        }


    }

}
