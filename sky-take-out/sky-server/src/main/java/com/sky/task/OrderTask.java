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
    /**
     * 处理超时订单的方法
     */
    @Scheduled(cron = "0 * * * * ?")    //表示每分钟执行一次
    public void processTimeoutOrder() {
        log.info("定时处理超时订单：{}", LocalDateTime.now());
//        逻辑是：查询出状态为待支付且订单时间早于当前时间15分钟的订单
        LocalDateTime time = LocalDateTime.now().plusMinutes(-15);
        List<Orders> ordersList =  orderMapper.getByStatusAndOrderTimeLT(Orders.PENDING_PAYMENT, time);
//         判断数据是否为空
        if(ordersList != null && ordersList.size() > 0){
//            遍历集合
            for (Orders orders : ordersList) {
                // 修改订单状态为已取消
                orders.setStatus(Orders.CANCELLED);
                //设置取消原因
                orders.setCancelReason("订单超时，自动取消");
                orders.setCancelTime(LocalDateTime.now());
//                调用修改订单状态的方法
                orderMapper.update(orders);
            }
        }
    }

    @Scheduled(cron = "0 0 1 * * ?")//每天凌晨1点触发一次
    public void processDeliveryOrder() {
        log.info("定时处理处于派送中的订单：{}", LocalDateTime.now());
//        逻辑是：查询出状态为派送中且订单时间处于当前时间减一个小时之后的订单日末尾
        LocalDateTime time = LocalDateTime.now().plusMinutes(-60);
        List<Orders> ordersList = orderMapper.getByStatusAndOrderTimeLT(Orders.DELIVERY_IN_PROGRESS, time);

        //         判断数据是否为空
        if(ordersList != null && ordersList.size() > 0){
//            遍历集合
            for (Orders orders : ordersList) {
                // 修改订单状态为已完成
                orders.setStatus(Orders.COMPLETED);
//                调用修改订单状态的方法
                orderMapper.update(orders);
            }
        }
    }
}

