package com.sky.task;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
public class OrderTask {

    @Autowired
    private OrderMapper orderMapper;



    @Scheduled(cron = "0 * * * * ?")
    public void processTimeOutOrder(){
        log.info("定时处理超时订单 " + LocalDateTime.now());

        //select * from orders where status = 0 and order_time < (当前时间 - 15min)
        List<Orders> byStatusAndOrderTime = orderMapper.getByStatusAndOrderTime(Orders.PENDING_PAYMENT, LocalDateTime.now().plusMinutes(-15));
        if(byStatusAndOrderTime != null && byStatusAndOrderTime.size() > 0){
            for(Orders orders : byStatusAndOrderTime){
                orders.setStatus(Orders.CANCELLED);
                orders.setCancelReason("清单超时，自动取消");
                orders.setCancelTime(LocalDateTime.now());
                orderMapper.update(orders);
            }
        }

    }


    @Scheduled(cron = "0 0 1 * * ?")
    public void processDeliveryOrder(){
        log.info("定时处理派送中订单 " + LocalDateTime.now());
        List<Orders> byStatusAndOrderTime = orderMapper.getByStatusAndOrderTime(Orders.DELIVERY_IN_PROGRESS, LocalDateTime.now().plusMinutes(-60));
        if(byStatusAndOrderTime != null && byStatusAndOrderTime.size() > 0){
            for(Orders orders : byStatusAndOrderTime){
                orders.setStatus(Orders.CANCELLED);
                orderMapper.update(orders);
            }
        }
    }


}
