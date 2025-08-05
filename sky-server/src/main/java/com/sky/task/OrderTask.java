package com.sky.task;

import com.sky.constant.MessageConstant;
import com.sky.entity.Orders;
import com.sky.mapper.OrdersMapper;
import com.sky.service.OrderService;
import com.sky.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
public class OrderTask {

    @Autowired
    private OrderService orderService;
    @Autowired
    private OrdersMapper ordersMapper;

    /**
     * 未支付订单处理
     */
    @Scheduled(cron="0 */5 * * * ?") // 每5分钟
    @Transactional
    public void orderTimeoutTask(){
        log.info("订单支付超时处理,date:{}", LocalDateTime.now());

        LocalDateTime now = LocalDateTime.now();
        now = now.plusMinutes(-15);
        List<Orders> list = ordersMapper.getByStatusAndOrderTime(Orders.PENDING_PAYMENT,now);
        if(list!=null && list.size()>0){
            for(Orders o:list){
                log.info("支付超时清除 id:{}",o.getId());
                o.setStatus(Orders.CANCELLED);
                o.setCancelReason(MessageConstant.PAY_TIMEOUT);
                o.setCancelTime(now);
                ordersMapper.update(o);
            }
        }
    }

    @Scheduled(cron="0 0 1 * * ?") // 每凌晨一点
    @Transactional
    public void deliveryTimeoutTask(){
        log.info("订单派送超时处理,date:{}", LocalDateTime.now());
        LocalDateTime now = LocalDateTime.now();
        now = now.plusMinutes(-60);
        List<Orders> list = ordersMapper.getByStatusAndOrderTime(Orders.DELIVERY_IN_PROGRESS,now);
        if(list!=null && list.size()>0){
            for(Orders o:list){
                o.setStatus(Orders.CANCELLED);
                o.setCancelReason(MessageConstant.DELIVERY_TIMEOUT);
                o.setCancelTime(now);
                ordersMapper.update(o);
            }
        }
    }
}
