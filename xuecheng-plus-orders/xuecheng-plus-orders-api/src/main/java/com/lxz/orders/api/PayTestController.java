package com.lxz.orders.api;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayTradeWapPayRequest;
import com.lxz.orders.config.AlipayConfig;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @description:
 * @author: 12860
 * @time: 2024/8/19 下午4:32
 */
@Controller
public class PayTestController {
    
    // 支付宝支付测试
    @RequestMapping("/alipaytest")
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws AlipayApiException, IOException {
        AlipayClient alipayClient = null;
        AlipayTradeWapPayRequest alipayRequest = new AlipayTradeWapPayRequest();
        // 设置请求参数
        alipayRequest.setReturnUrl("http://domain.com/CallBack/return_url.jsp");
        alipayRequest.setNotifyUrl("http://domain.com/CallBack/notify_url.jsp");
        alipayRequest.setBizContent("{" +
                "    \"out_trade_no\":\"20150320010101001\"," +
                "    \"total_amount\":\"88.88\"," +
                "    \"subject\":\"Iphone6 16G\"," +
                "    \"product_code\":\"QUICK_WAP_PAY\"" +
                "  }");
        String form = alipayClient.pageExecute(alipayRequest).getBody(); // 调用SDK生成表单
        response.setContentType("text/html;charset=" + AlipayConfig.CHARSET);
        response.getWriter().write(form); // 直接将完整的表单html输出到页面
        response.getWriter().flush();
    }
}
