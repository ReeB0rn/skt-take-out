package com.sky.controller.admin;


import com.sky.dto.OrdersCancelDTO;
import com.sky.dto.OrdersConfirmDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersRejectionDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController("AdminOrderController")
@RequestMapping("/admin/order")
@Slf4j
@Api(tags="管理端订单接口")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @GetMapping("/conditionSearch")
    @ApiOperation(value="订单搜索")
    public Result<PageResult> page(OrdersPageQueryDTO ordersPageQueryDTO){
        log.info("管理单 订单搜索 分页查询:{}",ordersPageQueryDTO);
        PageResult pageResult = orderService.page(ordersPageQueryDTO);
        return Result.success(pageResult);
    }

    @GetMapping("/statistics")
    @ApiOperation(value="各个状态的订单数量统计")
    public Result<OrderStatisticsVO> statisticsOrder(){
        log.info("获取各个状态的订单数量统计");
        OrderStatisticsVO orderStatisticsVO = orderService.statisticsOrder();
        return Result.success(orderStatisticsVO);
    }

    @GetMapping("/details/{id}")
    @ApiOperation(value="查询订单详情")
    public Result<OrderVO> getOrderDetail(@PathVariable Long id){
        log.info("客户端查询订单详情:{}",id);
        OrderVO orderVO = orderService.getOrderDetail(id);
        return Result.success(orderVO);
    }

    @PutMapping("/confirm")
    @ApiOperation(value="接单")
    public Result confirmOrder(@RequestBody OrdersConfirmDTO ordersConfirmDTO){
        log.info("接单:{}",ordersConfirmDTO);
        orderService.confirmOrder(ordersConfirmDTO.getId());
        return Result.success();
    }

    @PutMapping("/rejection")
    @ApiOperation(value="拒单")
    public Result rejectOrder(@RequestBody OrdersRejectionDTO ordersRejectionDTO){
        log.info("拒单:{}",ordersRejectionDTO);
        orderService.rejectOrder(ordersRejectionDTO);
        return Result.success();
    }

    @PutMapping("/cancel")
    @ApiOperation(value="取消订单")
    public Result cancelOrder(@RequestBody OrdersCancelDTO ordersCancelDTO){
        log.info("取消订单:{}",ordersCancelDTO);
        orderService.cancelOrder(ordersCancelDTO);
        return Result.success();
    }

    @PutMapping("/delivery/{id}")
    @ApiOperation(value="派送订单")
    public Result deliveryOrder(@PathVariable Long id){
        log.info("派送订单:{}",id);
        orderService.deliveryOrder(id);
        return Result.success();
    }

    @PutMapping("/complete/{id}")
    @ApiOperation(value="完成订单")
    public Result completeOrder(@PathVariable Long id)
    {
        log.info("派送订单:{}",id);
        orderService.completeOrder(id);
        return Result.success();
    }
}
