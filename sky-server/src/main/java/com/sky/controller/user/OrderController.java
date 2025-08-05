package com.sky.controller.user;

import com.sky.context.BaseContext;
import com.sky.dto.*;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user/order")
@Slf4j
@Api(tags="C端用户接口")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping("/submit")
    @ApiOperation(value="用户下单接口")
    public Result<OrderSubmitVO> submitOrder(@RequestBody OrdersSubmitDTO ordersSubmitDTO){
        log.info("用户提交订单{}:",ordersSubmitDTO);
        OrderSubmitVO orderSubmitVO = orderService.submitOrder(ordersSubmitDTO);
        return Result.success(orderSubmitVO);
    }

    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    @PutMapping("/payment")
    @ApiOperation(value="订单支付")
    public Result<OrderPaymentVO> payment(@RequestBody OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        log.info("订单支付：{}", ordersPaymentDTO);
        OrderPaymentVO orderPaymentVO = orderService.payment(ordersPaymentDTO);
        log.info("生成预支付交易单：{}", orderPaymentVO);
        return Result.success(orderPaymentVO);
    }

    @GetMapping("/historyOrders")
    @ApiOperation(value="获取历史订单")
    public Result<PageResult> getHistoryOrders(OrdersPageQueryDTO ordersPageQueryDTO){
        log.info("用户端分页获取历史订单:{}",ordersPageQueryDTO);
        ordersPageQueryDTO.setUserId(BaseContext.getCurrentId());
        PageResult pg = orderService.page(ordersPageQueryDTO);
        return Result.success(pg);
    }

    @GetMapping("/orderDetail/{id}")
    @ApiOperation(value="查询订单详情")
    public Result<OrderVO> getOrderDetail(@PathVariable Long id){
        log.info("获取订单详情:{}",id);
        OrderVO orderVO = orderService.getOrderDetail(id);
        return Result.success(orderVO);
    }

    @PutMapping("/cancel/{id}")
    @ApiOperation(value="取消订单")
    public Result cancelOrder(@PathVariable Long id){
        log.info("取消订单:{}",id);
        OrdersCancelDTO ordersCancelDTO = new OrdersCancelDTO();
        ordersCancelDTO.setId(id);
        orderService.cancelOrder(ordersCancelDTO);
        return Result.success();
    }

    @PostMapping("/repetition/{id}")
    @ApiOperation(value="再来一单")
    public Result repetitionOrder(@PathVariable Long id){
        log.info("再来一单:{}",id);
        orderService.repetitionOrder(id);
        return Result.success();
    }

    @GetMapping("/reminder/{id}")
    @ApiOperation(value="用户催单")
    public Result remindOrder(@PathVariable Long id){
        log.info("用户催单 ID:{}",id);
        orderService.remindOrder(id);
        return Result.success();
    }

}
