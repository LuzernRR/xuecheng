package com.lxz.orders.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lxz.base.exception.XueChengPlusException;
import com.lxz.base.utils.IdWorkerUtils;
import com.lxz.base.utils.QRCodeUtil;
import com.lxz.messagesdk.model.po.MqMessage;
import com.lxz.messagesdk.service.MqMessageService;
import com.lxz.orders.config.AlipayConfig;
import com.lxz.orders.mapper.XcOrdersGoodsMapper;
import com.lxz.orders.mapper.XcOrdersMapper;
import com.lxz.orders.mapper.XcPayRecordMapper;
import com.lxz.orders.model.dto.AddOrderDto;
import com.lxz.orders.model.dto.PayRecordDto;
import com.lxz.orders.model.dto.PayStatusDto;
import com.lxz.orders.model.po.XcOrders;
import com.lxz.orders.model.po.XcOrdersGoods;
import com.lxz.orders.model.po.XcPayRecord;
import com.lxz.orders.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageBuilderSupport;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static com.lxz.orders.config.PayNotifyConfig.PAYNOTIFY_EXCHANGE_FANOUT;

/**
 * @description:
 * @author: 12860
 * @time: 2024/8/20 上午10:31
 */
@Service
@Slf4j
public class OrderServiceImpl implements OrderService {

    @Value("${pay.QRCodeUrl}")
    String QRCodeUrl;

    @Value("${pay.alipay.APP_ID}")
    String APP_ID;

    @Value("${pay.alipay.APP_PRIVATE_KEY}")
    String APP_PRIVATE_KEY;

    @Value("${pay.alipay.ALIPAY_PUBLIC_KEY}")
    String ALIPAY_PUBLIC_KEY;

    @Autowired
    XcOrdersMapper ordersMapper;

    @Autowired
    XcOrdersGoodsMapper xcOrdersGoodsMapper;

    @Autowired
    XcPayRecordMapper payRecordMapper;

    @Autowired
    XcOrdersMapper xcOrdersMapper;

    @Autowired
    OrderServiceImpl currentProxy;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Autowired
    MqMessageService mqMessageService;

    @Override
    // 生成支付二维码
    public PayRecordDto createOrder(String userId, AddOrderDto addOrderDto) {
        // ===========插入订单表==============
        // 幂等性判断，同一个选课记录只能有一个订单
        XcOrders xcOrders = saveXcOrders(userId, addOrderDto);
        // =============插入支付记录表==============
        XcPayRecord payRecord = createPayRecord(xcOrders);
        // ==============调用支付宝接口生成支付二维码================
        // 拿到支付号
        Long payNo = payRecord.getPayNo();
        QRCodeUtil qrCodeUtil = new QRCodeUtil();
        String url = String.format(QRCodeUrl, payNo);
        String qrCode = null;
        try {
            qrCode = qrCodeUtil.createQRCode(url, 200, 200);
        } catch (IOException e) {
            XueChengPlusException.cast("生成二维码失败");
        }
        PayRecordDto payRecordDto = new PayRecordDto();
        BeanUtils.copyProperties(payRecord, payRecordDto);
        payRecordDto.setQrcode(qrCode);
        return payRecordDto;
    }

    @Override
    public XcPayRecord getPayRecordPayno(String payNo) {
        XcPayRecord xcPayRecord = payRecordMapper.selectOne(new LambdaQueryWrapper<XcPayRecord>()
                .eq(XcPayRecord::getPayNo, payNo));
        return xcPayRecord;
    }

    @Override
    public PayRecordDto queryPayResult(String payNo) {
        // ======主动请求支付宝，查询支付结果=========
        PayStatusDto payStatusDto = queryPayResultFromAlipay(payNo);
        // ======如果支付成功，更新订单表、支付记录表状态=========
        currentProxy.saveAliPayStatus(payStatusDto);
        // 返回支付状态
        XcPayRecord payRecordPayno = getPayRecordPayno(payNo);
        PayRecordDto payRecordDto = new PayRecordDto();
        BeanUtils.copyProperties(payRecordPayno, payRecordDto);
        return payRecordDto;
    }

    @Override
    // 发布者：调用rabbitmq发送支付结果
    public void notifyPayResult(MqMessage mqMessage) {
        // 消息id
        Long id = mqMessage.getId();
        // 消息内容
        String jsonString = JSON.toJSONString(mqMessage);
        // 封装消息
        Message message1 = MessageBuilder
                .withBody(jsonString.getBytes(StandardCharsets.UTF_8))   // 消息内容
                .setDeliveryMode(MessageDeliveryMode.PERSISTENT)   // 持久化
                .build();                                           // 构建消息
        CorrelationData correlationData = new CorrelationData(id.toString());
        // 使用correlationData指定回调方法
        correlationData.getFuture().addCallback(result ->{
            if (result.isAck()) {
                // 消息发送成功
                log.debug("消息发送成功{}", jsonString);
                // 将消息从数据库表中删除
                 mqMessageService.completed(id);
            } else {
                // 消息发送失败
                log.debug("消息发送失败{}", jsonString);
            }
        }, ex -> {
            // 消息发送异常
            log.debug("消息发送异常{}", jsonString);
        });
        // 发送消息
        rabbitTemplate.convertAndSend(PAYNOTIFY_EXCHANGE_FANOUT, "",  message1, correlationData);
    }

    @Transactional
    public void saveAliPayStatus(PayStatusDto payStatusDto) {
        // ===========更新订单表、支付记录表状态==========
        String payNo = payStatusDto.getOut_trade_no();
        XcPayRecord payRecordPayno = getPayRecordPayno(payNo);
        if (payRecordPayno == null) {
            XueChengPlusException.cast("支付记录不存在");
        }
        // 拿到订单id
        Long orderId = payRecordPayno.getOrderId();
        XcOrders orders = ordersMapper.selectById(orderId);
        if (orders == null) {
            XueChengPlusException.cast("订单不存在");
        }
        // 判断是否已支付
        if (payRecordPayno.getStatus().equals("601002")) {
            XueChengPlusException.cast("支付已完成");
            return;
        }
        // 从支付宝返回的支付状态
        String trade_status = payStatusDto.getTrade_status();
        if ("TRADE_SUCCESS".equals(trade_status)) {
            // 更新订单表状态
            orders.setStatus("601002");
            ordersMapper.updateById(orders);
            // 更新支付记录表状态
            payRecordPayno.setStatus("601002");
            payRecordPayno.setOutPayNo(payStatusDto.getTrade_no());
            payRecordPayno.setOutPayChannel("AliPay");
            payRecordPayno.setPaySuccessTime(LocalDateTime.now());
            payRecordMapper.updateById(payRecordPayno);
        }
        orders.setStatus("601002");
        ordersMapper.updateById(orders);

        // 将消息写到数据库
        MqMessage mqMessage1 = mqMessageService.addMessage("payresult_notify", orders.getOutBusinessId(), orders.getOrderType(), null);
        // rabbitmq发送支付结果
        notifyPayResult(mqMessage1);
    }

    private PayStatusDto queryPayResultFromAlipay(String payNo){
        // 获得初始化的AlipayClient
        AlipayClient alipayClient = new DefaultAlipayClient(AlipayConfig.URL, APP_ID, APP_PRIVATE_KEY, AlipayConfig.FORMAT, AlipayConfig.CHARSET, ALIPAY_PUBLIC_KEY, AlipayConfig.SIGNTYPE);
        AlipayTradeQueryRequest alipayRequest = new AlipayTradeQueryRequest();
        // 加载支付的订单信息
        JSONObject bizContent = new JSONObject();
        bizContent.put("out_trade_no", "20150320010101002");
        alipayRequest.setBizContent(bizContent.toJSONString());
        String body = null;
        try {
            AlipayTradeQueryResponse response = alipayClient.execute(alipayRequest);
            if (!response.isSuccess()) {
                XueChengPlusException.cast("请求支付宝查询订单状态失败");
            }
            body = response.getBody();
        } catch (AlipayApiException e) {
            e.printStackTrace();
            XueChengPlusException.cast("请求支付宝查询订单状态失败");
        }
        // 解析支付宝返回的json
        Map map = JSON.parseObject(body, Map.class);
        Map alipay_trade_query_response = (Map) map.get("alipay_trade_query_response");
        String status = (String) alipay_trade_query_response.get("trade_status");
        String tradeNo = (String) alipay_trade_query_response.get("trade_no");
        String totalAmount = (String) alipay_trade_query_response.get("total_amount");
        PayStatusDto payStatusDto = new PayStatusDto();
        payStatusDto.setOut_trade_no(payNo);
        payStatusDto.setTrade_status(status);
        payStatusDto.setTrade_no(tradeNo);
        payStatusDto.setApp_id(APP_ID);
        payStatusDto.setTotal_amount(totalAmount);
        return payStatusDto;
    }

    // ==================插入支付记录表==================
    private XcPayRecord createPayRecord(XcOrders xcOrders) {
        Long id = xcOrders.getId();
        XcOrders orders = ordersMapper.selectById(id);
        // ===========幂等性判断==========
        if (orders == null) {
            XueChengPlusException.cast("订单不存在");
        }
        // 订单状态
        String status = xcOrders.getStatus();
        if ("601002".equals(status)) {  // 订单已支付
            XueChengPlusException.cast("订单已支付");
        }
        // ===========插入支付记录表==========
        XcPayRecord payRecord = new XcPayRecord();
        payRecord.setPayNo(IdWorkerUtils.getInstance().nextId()); // 支付号
        payRecord.setOrderId(id);
        payRecord.setOrderName(xcOrders.getOrderName());
        payRecord.setTotalPrice(xcOrders.getTotalPrice());
        payRecord.setCurrency("CNY");
        payRecord.setCreateDate(LocalDateTime.now());
        payRecord.setStatus("601001");  // 未支付
        payRecord.setUserId(xcOrders.getUserId());
        int insert = payRecordMapper.insert(payRecord);
        if (insert <= 0) {
            XueChengPlusException.cast("插入支付记录表失败");
        }
        return payRecord;
    }

    // ==================插入订单表==================
    public XcOrders saveXcOrders(String userId, AddOrderDto addOrderDto) {
        // ===========幂等性判断==========
        XcOrders xcOrders = getOrderByBusinessId(addOrderDto.getOutBusinessId());
        if (xcOrders != null) {
            return xcOrders;
        }
        // ===========插入订单表=============
        xcOrders = new XcOrders();
        // ========使用雪花算法生成订单号===========
        xcOrders.setId(IdWorkerUtils.getInstance().nextId());
        xcOrders.setTotalPrice(addOrderDto.getTotalPrice());
        xcOrders.setCreateDate(LocalDateTime.now());
        xcOrders.setStatus("600001");
        xcOrders.setUserId(userId);
        xcOrders.setOrderType("60201");
        xcOrders.setOrderName(addOrderDto.getOrderName());
        xcOrders.setOrderDescrip(addOrderDto.getOrderDescrip());
        xcOrders.setOrderDetail(addOrderDto.getOrderDetail());
        xcOrders.setOutBusinessId(addOrderDto.getOutBusinessId());  // 主键id
        int insert = ordersMapper.insert(xcOrders);
        if (insert <= 0) {
            XueChengPlusException.cast("插入订单表失败");
        }
        Long orderId = xcOrders.getId();
        // ===========插入订单明细表=============
        // 将前端传过来的选课json转换为List存入订单明细表
        String orderDetailJson = addOrderDto.getOrderDetail();
        List<XcOrdersGoods> xcOrdersGoods = JSON.parseArray(orderDetailJson, XcOrdersGoods.class);
        // 遍历xcOrdersGoods插入订单明细表
        xcOrdersGoods.forEach(ordersGoods -> {
            ordersGoods.setOrderId(orderId);
            // 插入订单明细
            xcOrdersGoodsMapper.insert(ordersGoods);
        });

        return xcOrders;
    }

    // 查询订单表
    public XcOrders getOrderByBusinessId(String businessId) {
        XcOrders orders = ordersMapper.selectOne(new LambdaQueryWrapper<XcOrders>()
                .eq(XcOrders::getOutBusinessId, businessId));
        return orders;
    }
}
