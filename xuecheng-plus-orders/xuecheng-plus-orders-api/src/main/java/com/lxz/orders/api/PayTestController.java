package com.lxz.orders.api;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradeWapPayRequest;
import com.lxz.orders.config.AlipayConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @description:
 * @author: 12860
 * @time: 2024/8/19 下午4:32
 */
@Controller
public class PayTestController {
    @Value("${pay.alipay.APP_ID}")
    String APPID;

    @Value("${pay.alipay.APP_PRIVATE_KEY}")
    String APP_PRIVATE_KEY;

    @Value("${pay.alipay.ALIPAY_PUBLIC_KEY}")
    String ALIPAY_PUBLIC_KEY;

    
    // 支付宝支付测试
    @RequestMapping("/alipaytest")
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws AlipayApiException, IOException {
        AlipayClient alipayClient = new DefaultAlipayClient(AlipayConfig.URL, APPID, APP_PRIVATE_KEY, AlipayConfig.FORMAT, AlipayConfig.CHARSET, ALIPAY_PUBLIC_KEY, AlipayConfig.SIGNTYPE);
        AlipayTradeWapPayRequest alipayRequest = new AlipayTradeWapPayRequest();
        // 设置请求参数
//        alipayRequest.setReturnUrl("http://domain.com/CallBack/return_url.jsp");
        // 配置内网穿透，支付宝支付成功后的回调地址
        alipayRequest.setNotifyUrl("http://domain.com/CallBack/notify_url.jsp");
        alipayRequest.setBizContent("{" +
                "    \"out_trade_no\":\"20150320010101002\"," +   // 商户订单号
                "    \"total_amount\":\"0.9\"," +                 // 金额
                "    \"subject\":\"Iphone15 18G\"," +             // 商品名称
                "    \"product_code\":\"QUICK_WAP_PAY\"" +        // 销售产品码
                "  }");
        String form = alipayClient.pageExecute(alipayRequest).getBody(); // 调用SDK生成表单
        response.setContentType("text/html;charset=" + AlipayConfig.CHARSET);
        response.getWriter().write(form); // 直接将完整的表单html输出到页面
        response.getWriter().flush();
    }

    // 支付宝支付成功后notify_url接口
    @PostMapping("/paynotify")
    public void paynotify(HttpServletRequest request, HttpServletResponse response) throws IOException, AlipayApiException {
        //获取支付宝POST过来反馈信息
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
            //乱码解决，这段代码在出现乱码时使用。如果mysign和sign不相等也可以使用这段代码转化
            //valueStr = new String(valueStr.getBytes("ISO-8859-1"), "gbk");
            params.put(name, valueStr);
        }
        //获取支付宝的通知返回参数，可参考技术文档中页面跳转同步通知参数列表(以上仅供参考)//
        //计算得出通知验证结果
        //boolean AlipaySignature.rsaCheckV1(Map<String, String> params, String publicKey, String charset, String sign_type)
        boolean verify_result = AlipaySignature.rsaCheckV1(params, ALIPAY_PUBLIC_KEY, AlipayConfig.CHARSET, "RSA2");

        if(verify_result){//验证成功
            //获取支付宝的通知返回参数，可参考技术文档中页面跳转同步通知参数列表(以下仅供参考)//
            //商户订单号（自己设置的订单号）
            String out_trade_no = new String(request.getParameter("out_trade_no").getBytes("ISO-8859-1"),"UTF-8");
            //支付宝交易号（支付宝返回的订单号）
            String trade_no = new String(request.getParameter("trade_no").getBytes("ISO-8859-1"),"UTF-8");
            //交易状态
            String trade_status = new String(request.getParameter("trade_status").getBytes("ISO-8859-1"),"UTF-8");
            if(trade_status.equals("TRADE_FINISHED")){
                //判断该笔订单是否在商户网站中已经做过处理
                //如果没有做过处理，根据订单号（out_trade_no）在商户网站的订单系统中查到该笔订单的详细，并执行商户的业务程序
                //请务必判断请求时的total_fee、seller_id与通知时获取的total_fee、seller_id为一致的
                //如果有做过处理，不执行商户的业务程序

                //注意：
                //如果签约的是可退款协议，退款日期超过可退款期限后（如三个月可退款），支付宝系统发送该交易状态通知
                //如果没有签约可退款协议，那么付款完成后，支付宝系统发送该交易状态通知。
            } else if (trade_status.equals("TRADE_SUCCESS")){
                //判断该笔订单是否在商户网站中已经做过处理
                //如果没有做过处理，根据订单号（out_trade_no）在商户网站的订单系统中查到该笔订单的详细，并执行商户的业务程序
                //请务必判断请求时的total_fee、seller_id与通知时获取的total_fee、seller_id为一致的
                //如果有做过处理，不执行商户的业务程序

                //注意：
                //如果签约的是可退款协议，那么付款完成后，支付宝系统发送该交易状态通知。
            }
            response.getWriter().write("success");  // 验签成功，返回success
        }else{//验证失败
            response.getWriter().write("fail");    // 验签失败，返回fail

        }
    }
}
