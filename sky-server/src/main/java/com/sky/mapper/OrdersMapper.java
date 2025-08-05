package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.entity.Orders;

import com.sky.vo.BusinessDataVO;
import io.swagger.models.auth.In;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface OrdersMapper {

   /**
    * 插入订单数据
    * @param order
    */
   void insert(Orders order);

   /**
    * 根据订单号查询订单
    * @param orderNumber
    */
   @Select("select * from orders where number = #{orderNumber}")
   Orders getByNumber(String orderNumber);

   /**
    * 修改订单信息
    * @param orders
    */
   void update(Orders orders);

   @Update("update orders set status = #{orderStatus},pay_status = #{orderPaidStatus} ,checkout_time = #{checkOutTime} where number = #{orderNumber}")
   void updateStatus(Integer orderStatus, Integer orderPaidStatus, LocalDateTime checkOutTime, String orderNumber);

   Page<Orders> page(OrdersPageQueryDTO ordersPageQueryDTO);

   @Select("select * from orders where id = #{orderId}")
   Orders getByOrderId(Long orderId);

   List<Orders> getByStatus(Integer deliveryInProgress);


   /**
    * 获取相应状态以及早于给定时间的订单
    * @param status
    * @param time
    * @return
    */
   List<Orders> getByStatusAndOrderTime(Integer status, LocalDateTime time);

   Double sumByMap(Map map);

   Integer countByMap(Map map);

}
