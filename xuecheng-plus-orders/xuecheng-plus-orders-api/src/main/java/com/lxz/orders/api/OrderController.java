package com.lxz.orders.api;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradeWapPayRequest;
import com.lxz.base.exception.XueChengPlusException;
import com.lxz.orders.config.AlipayConfig;
import com.lxz.orders.model.dto.AddOrderDto;
import com.lxz.orders.model.dto.PayRecordDto;
import com.lxz.orders.model.dto.PayStatusDto;
import com.lxz.orders.model.po.XcPayRecord;
import com.lxz.orders.service.OrderService;
import com.lxz.orders.util.SecurityUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @description:
 * @author: 12860
 * @time: 2024/8/20 上午10:25
 */

@Api(value = "订单支付接口", description = "订单支付接口")
@Slf4j
@Controller
public class OrderController {

    @Autowired
    OrderService orderService;

    @Value("${pay.alipay.APP_ID}")
    String APPID;

    @Value("${pay.alipay.APP_PRIVATE_KEY}")
    String APP_PRIVATE_KEY;

    @Value("${pay.alipay.ALIPAY_PUBLIC_KEY}")
    String ALIPAY_PUBLIC_KEY;


    @ApiOperation("生成支付二维码")
    @PostMapping("/generatepaycode")
    @ResponseBody
    public PayRecordDto generatePayCode(@RequestBody AddOrderDto addOrderDto) {
        SecurityUtil.XcUser user = SecurityUtil.getUser();
        String id = user.getId();
        // 调用service插入订单信息、支付记录、生成支付二维码
        PayRecordDto payRecordDto = orderService.createOrder(id, addOrderDto);
        return payRecordDto;
    }

    @ApiOperation("扫码下单")
    @GetMapping("/requestpay")
    public void requestPay(String payNo, HttpServletResponse response) throws IOException, AlipayApiException {

        // 传入支付记录号，判断支付记录号是否存在
        XcPayRecord payRecordPayno = orderService.getPayRecordPayno(payNo);
        if (payRecordPayno == null) {
            XueChengPlusException.cast("支付记录不存在");
        }
        // 判断是否已支付
        if (payRecordPayno.getStatus().equals("601002")) {
            XueChengPlusException.cast("支付已完成");
        }
        Float totalPrice = payRecordPayno.getTotalPrice();
        String orderName = payRecordPayno.getOrderName();
        // 请求支付宝下单
        AlipayClient alipayClient = new DefaultAlipayClient(AlipayConfig.URL, APPID, APP_PRIVATE_KEY, AlipayConfig.FORMAT, AlipayConfig.CHARSET, ALIPAY_PUBLIC_KEY, AlipayConfig.SIGNTYPE);
        AlipayTradeWapPayRequest alipayRequest = new AlipayTradeWapPayRequest();
        // 设置请求参数
//        alipayRequest.setReturnUrl("http://domain.com/CallBack/return_url.jsp");
        // 配置内网穿透，支付宝支付成功后的回调地址
//        alipayRequest.setNotifyUrl("http://domain.com/CallBack/notify_url.jsp");
        alipayRequest.setBizContent("{" +
                "    \"out_trade_no\":\""+ payNo + "\"," +         // 商户订单号
                "    \"total_amount\":\""+ totalPrice + "\"," +    // 金额
                "    \"subject\":\"" + orderName +"\"," +         // 商品名称
                "    \"product_code\":\"QUICK_WAP_PAY\"" +        // 销售产品码
                "  }");
        String form = alipayClient.pageExecute(alipayRequest).getBody(); // 调用SDK生成表单
        response.setContentType("text/html;charset=" + AlipayConfig.CHARSET);
        response.getWriter().write(form); // 直接将完整的表单html输出到页面
        response.getWriter().flush();
    }

    @ApiOperation("查询支付结果")
    @GetMapping("/payresult")
    @ResponseBody
    public PayRecordDto payResult(String payNo) {
        // 查询支付结果
        PayRecordDto payRecordDto = orderService.queryPayResult(payNo);
        return payRecordDto;
    }
}
