package com.sky.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.entity.*;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.OrderBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.*;
import com.sky.service.OrderService;
import com.sky.service.ShoppingCartService;
import com.sky.utils.WeChatPayUtil;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderSubmitVO;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.weaver.ast.Or;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class OrderServiceImpl implements OrderService {

    @Autowired
    OrderMapper orderMapper;

    @Autowired
    OrderDetailMapper orderDetailMapper;

    @Autowired
    private AddressBookMapper addressBookMapper;

    @Autowired
    private ShoppingCartMapper shoppingCartMapper;

    @Autowired
    private WeChatPayUtil weChatPayUtil;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private OrderService orderService;

    @Autowired
    private ShoppingCartService shoppingCartService;
    /**
     * 用户下单
     * @param ordersSubmitDTO
     * @return
     */
    @Override
    @Transactional
    public OrderSubmitVO submit(OrdersSubmitDTO ordersSubmitDTO) {
        //处理业务异常
            //地址薄为空
            //购物车为空
        log.info("调用sub");
        AddressBook byId = addressBookMapper.getById(ordersSubmitDTO.getAddressBookId());
        if(byId == null){
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }

        List<ShoppingCart> list = shoppingCartMapper
                .list(ShoppingCart.builder().userId(BaseContext.getCurrentId()).build());
        if(list == null || list.size() == 0){
            throw  new ShoppingCartBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }

        //向订单表插入一条数据

        Orders orders = new Orders();
        BeanUtils.copyProperties(ordersSubmitDTO, orders);
        orders.setOrderTime(LocalDateTime.now());
        orders.setPayStatus(Orders.UN_PAID);
        orders.setStatus(Orders.PENDING_PAYMENT);
        orders.setNumber(String.valueOf(System.currentTimeMillis()));
        orders.setPhone(byId.getPhone());
        orders.setConsignee(byId.getConsignee());
        orders.setUserId(BaseContext.getCurrentId());

        orderMapper.insert(orders);

        //向订单明细表插入n条数据
        List<OrderDetail> orderDetailList = new ArrayList<>();
        for(ShoppingCart cart : list){
            OrderDetail orderDetail = new OrderDetail();
            BeanUtils.copyProperties(cart, orderDetail);
            orderDetail.setOrderId(orders.getId());
            orderDetailList.add(orderDetail);
        }

        orderDetailMapper.insertBatch(orderDetailList);


        //清空购物车数据

        shoppingCartMapper.deleteById(BaseContext.getCurrentId());

        //封装VO返回结果

        OrderSubmitVO orderSubmitVO = OrderSubmitVO.builder()
                .id(orders.getId())
                .orderTime(orders.getOrderTime())
                .orderNumber(orders.getNumber())
                .orderAmount(orders.getAmount())
                .build();

        return orderSubmitVO;
    }

    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        // 当前登录用户id
        Long userId = BaseContext.getCurrentId();

        User user = userMapper.getById(userId);

        //调用微信支付接口，生成预支付交易单
        /*JSONObject jsonObject = weChatPayUtil.pay(
                ordersPaymentDTO.getOrderNumber(), //商户订单号
                new BigDecimal(0.01), //支付金额，单位 元
                "苍穹外卖订单", //商品描述
                user.getOpenid() //微信用户的openid
        );

        JSONObject jsonObject = new JSONObject();

        if (jsonObject.getString("code") != null && jsonObject.getString("code").equals("ORDERPAID")) {
            throw new OrderBusinessException("该订单已支付");
        }*/
        JSONObject jo = new JSONObject();
        jo.put("timeStamp", "");
        jo.put("nonceStr", "");
        jo.put("package", "prepay_id=" + ordersPaymentDTO.getOrderNumber());
        jo.put("signType", "RSA");
        jo.put("paySign", "");

        OrderPaymentVO vo = jo.toJavaObject(OrderPaymentVO.class);
        vo.setPackageStr(jo.getString("package"));
        orderService.paySuccess(ordersPaymentDTO.getOrderNumber());
        shoppingCartService.cleanShoppingCart();
        return vo;
    }

    /**
     * 支付成功，修改订单状态
     *
     * @param outTradeNo
     */
    public void paySuccess(String outTradeNo) {

        // 根据订单号查询订单
        Orders ordersDB = orderMapper.getByNumber(outTradeNo);

        // 根据订单id更新订单的状态、支付方式、支付状态、结账时间
        Orders orders = Orders.builder()
                .id(ordersDB.getId())
                .status(Orders.TO_BE_CONFIRMED)
                .payStatus(Orders.PAID)
                .checkoutTime(LocalDateTime.now())
                .build();
        System.out.println("交易完成");
        System.out.println(orders);

        orderMapper.update(orders);
    }

}
