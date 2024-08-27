package com.lxz.orders.service;

import com.lxz.messagesdk.model.po.MqMessage;
import com.lxz.orders.model.dto.AddOrderDto;
import com.lxz.orders.model.dto.PayRecordDto;
import com.lxz.orders.model.dto.PayStatusDto;
import com.lxz.orders.model.po.XcPayRecord;

/**
 * @description:
 * @author: 12860
 * @time: 2024/8/20 上午10:30
 */
public interface OrderService {

    // 生成支付二维码
    public PayRecordDto createOrder(String userId, AddOrderDto addOrderDto);

    // 获取支付号payNo
    public XcPayRecord getPayRecordPayno(String payNo);

    // 主动请求支付宝，查询支付结果
    public PayRecordDto queryPayResult(String payNo);

    // 支付宝异步通知
    public void notifyPayResult(MqMessage mqMessage);

}
