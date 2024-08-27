package com.lxz.orders;

import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;

import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.lxz.orders.config.AlipayConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @description: 请求支付宝查询订单状态
 * @author: 12860
 * @time: 2024/8/19 下午9:22
 */
@SpringBootTest
public class AlipayTest {
    @Value("${pay.alipay.APP_ID}")
    String APP_ID;

    @Value("${pay.alipay.APP_PRIVATE_KEY}")
    String APP_PRIVATE_KEY;

    @Value("${pay.alipay.ALIPAY_PUBLIC_KEY}")
    String ALIPAY_PUBLIC_KEY;

    @Test
    // 查询支付宝订单状态
    public void queryPayResult() throws AlipayApiException {
        // 获得初始化的AlipayClient
        AlipayClient alipayClient = new DefaultAlipayClient(AlipayConfig.URL, APP_ID, APP_PRIVATE_KEY, AlipayConfig.FORMAT, AlipayConfig.CHARSET, ALIPAY_PUBLIC_KEY, AlipayConfig.SIGNTYPE);
        AlipayTradeQueryRequest alipayRequest = new AlipayTradeQueryRequest();
        // 加载支付的订单信息
        JSONObject bizContent = new JSONObject();
        bizContent.put("out_trade_no", "20150320010101002");
        alipayRequest.setBizContent(bizContent.toJSONString());
        AlipayTradeQueryResponse body = alipayClient.execute(alipayRequest);
        System.out.println(body.getBody());
    }
}
