package com.sky.service;

import com.sky.dto.*;
import com.sky.entity.Orders;
import com.sky.result.PageResult;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderService {
    /**
     * 用户下单
     * @param ordersSubmitDTO
     * @return
     */
    OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO);

    /**
     * 订单支付
     * @param ordersPaymentDTO
     * @return
     */
    OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception;

    /**
     * 支付成功，修改订单状态
     * @param outTradeNo
     */
    void paySuccess(String outTradeNo);

    PageResult page(OrdersPageQueryDTO ordersPageQueryDTO);

    OrderVO getOrderDetail(Long orderId);

    void cancelOrder(OrdersCancelDTO ordersCancelDTO);

    void repetitionOrder(Long id);

    OrderStatisticsVO statisticsOrder();

    void confirmOrder(Long id);

    void rejectOrder(OrdersRejectionDTO ordersRejectionDTO);

    void deliveryOrder(Long id);

    void completeOrder(Long id);


    void remindOrder(Long id);
}
