package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.*;
import com.sky.entity.*;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.OrderBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.*;
import com.sky.result.PageResult;
import com.sky.service.OrderService;
import com.sky.utils.HttpClientUtil;
import com.sky.utils.WeChatPayUtil;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import com.sky.websocket.WebSocketServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrdersMapper ordersMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private AddressBookMapper addressBookMapper;
    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private WeChatPayUtil weChatPayUtil;
    @Autowired
    private OrderService orderService;
    @Value("${sky.baidu.ak}")
    private String ak;
    @Value("${sky.shop.address}")
    private String shopAddress;
    @Autowired
    private WebSocketServer webSocketServer;

    /**
     * 用户提交订单
     * @param ordersSubmitDTO
     * @return
     */
    @Override
    @Transactional
    public OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO) {

        // 添加配送距离判断
        AddressBook addressBook = addressBookMapper.getById(ordersSubmitDTO.getAddressBookId());
        String address = addressBook.getCityName()+addressBook.getDistrictName()+addressBook.getDetail();
        checkOutOfRange(address);



        // 判断异常
        if(ordersSubmitDTO.getAddressBookId()==null){
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }

        // 获取购物车信息

        ShoppingCart shoppingCart = ShoppingCart.builder()
                .userId(BaseContext.getCurrentId())
                .build();

        List<ShoppingCart> shoppingCartList = shoppingCartMapper.list(shoppingCart);
        // 购物车异常
        if(shoppingCartList==null|| shoppingCartList.isEmpty()){
            throw new ShoppingCartBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }

        // 生成订单信息


        Orders orders = new Orders();
        User user = userMapper.getUserByUserId(BaseContext.getCurrentId());

        BeanUtils.copyProperties(ordersSubmitDTO,orders);
        BeanUtils.copyProperties(addressBook,orders);
        orders.setNumber(String.valueOf(System.currentTimeMillis()));
        orders.setStatus(Orders.PENDING_PAYMENT);
        orders.setUserId(BaseContext.getCurrentId());
        orders.setOrderTime(LocalDateTime.now());
        orders.setPayStatus(Orders.UN_PAID);
        orders.setAddress(address);
        orders.setUserName(user.getName());
         ordersMapper.insert(orders);
        List<OrderDetail> orderDetailList = new ArrayList<OrderDetail>();
        for (ShoppingCart sc : shoppingCartList) {
            OrderDetail orderDetail = new OrderDetail();
            BeanUtils.copyProperties(sc,orderDetail);
            orderDetail.setOrderId(orders.getId());
            orderDetailList.add(orderDetail);
        }
        orderDetailMapper.insertBatch(orderDetailList);

        // 生成VO
        OrderSubmitVO orderSubmitVO
                = OrderSubmitVO.builder()
                .id(orders.getId())
                .orderAmount(orders.getAmount())
                .orderNumber(orders.getNumber())
                .orderTime(orders.getOrderTime())
                .build();

        shoppingCartMapper.cleanByUserId(BaseContext.getCurrentId());
        log.info("执行清除购物车命令");
        return orderSubmitVO;

    }

    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    @Transactional
    public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        // 当前登录用户id
        Long userId = BaseContext.getCurrentId();
        User user = userMapper.getUserByUserId(userId);

//        //调用微信支付接口，生成预支付交易单
//        JSONObject jsonObject = weChatPayUtil.pay(
//                ordersPaymentDTO.getOrderNumber(), //商户订单号
//                new BigDecimal(0.01), //支付金额，单位 元
//                "苍穹外卖订单", //商品描述
//                user.getOpenid() //微信用户的openid
//        );
//
//        if (jsonObject.getString("code") != null && jsonObject.getString("code").equals("ORDERPAID")) {
//            throw new OrderBusinessException("该订单已支付");
//        }

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("code","ORDERPAID");
        OrderPaymentVO vo = jsonObject.toJavaObject(OrderPaymentVO.class);
        vo.setPackageStr(jsonObject.getString("package"));
        Integer OrderPaidStatus = Orders.PAID;//支付状态，已支付
        Integer OrderStatus = Orders.TO_BE_CONFIRMED;  //订单状态，待接单
        LocalDateTime check_out_time = LocalDateTime.now();//更新支付时间
        Orders orders = ordersMapper.getByNumber(ordersPaymentDTO.getOrderNumber());
        ordersMapper.updateStatus(OrderStatus, OrderPaidStatus, check_out_time,ordersPaymentDTO.getOrderNumber());
        
        // 支付成功后WebSocket向商家推送来单提示
        Map map = new HashMap();
        map.put("type",1);
        map.put("orderId",orders.getId());
        map.put("content","OrderNumber: "+orders.getNumber());
        String msg = JSON.toJSONString(map);
        webSocketServer.sendToAllClient(msg);
        
        
        
        return vo;

    }

    /**
     * 支付成功，修改订单状态
     *
     * @param outTradeNo
     */
    @Transactional
    public void paySuccess(String outTradeNo) {

        // 根据订单号查询订单
        Orders ordersDB = ordersMapper.getByNumber(outTradeNo);

        // 根据订单id更新订单的状态、支付方式、支付状态、结账时间
        Orders orders = Orders.builder()
                .id(ordersDB.getId())
                .status(Orders.TO_BE_CONFIRMED)
                .payStatus(Orders.PAID)
                .checkoutTime(LocalDateTime.now())
                .build();

        ordersMapper.update(orders);
    }

    /**
     * 分页获取历史订单
     * @param ordersPageQueryDTO
     * @return
     */
    @Override
    public PageResult page(OrdersPageQueryDTO ordersPageQueryDTO) {
        PageHelper.startPage(ordersPageQueryDTO.getPage(),ordersPageQueryDTO.getPageSize());
        Page<Orders> page = ordersMapper.page(ordersPageQueryDTO);
        List<OrderVO> list = new ArrayList<>();
        for(Orders o : page){
            OrderVO orderVO = new OrderVO();
            BeanUtils.copyProperties(o,orderVO);
            orderVO.setOrderDetailList(orderDetailMapper.getByOrderId(o.getId()));
            list.add(orderVO);
        }
        PageResult pageResult = new PageResult();
        pageResult.setTotal(page.getTotal());
        pageResult.setRecords(list);
        return pageResult;
    }

    @Override
    public OrderVO getOrderDetail(Long orderId) {
        OrderVO orderVO = new OrderVO();
        Orders order = ordersMapper.getByOrderId(orderId);
        BeanUtils.copyProperties(order,orderVO);
        orderVO.setOrderDetailList(orderDetailMapper.getByOrderId(orderId));
        return orderVO;
    }

    @Override
    @Transactional
    public void cancelOrder(OrdersCancelDTO ordersCancelDTO) {
        Orders orders = ordersMapper.getByOrderId(ordersCancelDTO.getId());
        orders.setStatus(Orders.CANCELLED);
        orders.setCancelReason(ordersCancelDTO.getCancelReason());
        orders.setCancelTime(LocalDateTime.now());
        ordersMapper.update(orders);
    }

    @Override
    @Transactional
    public void repetitionOrder(Long id) {
        List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(id);
        Orders orders = ordersMapper.getByOrderId(id);
        orders.setNumber(String.valueOf(System.currentTimeMillis()));
        orders.setStatus(Orders.PENDING_PAYMENT);
        orders.setUserId(BaseContext.getCurrentId());
        orders.setOrderTime(LocalDateTime.now());
        orders.setPayStatus(Orders.UN_PAID);
        orders.setCancelReason(null);
        orders.setCancelTime(null);
        orders.setRejectionReason(null);
        orders.setCheckoutTime(null);
        orders.setUserName(null);



        ordersMapper.insert(orders);
        Long orderId = orders.getId();

        for(OrderDetail orderDetail : orderDetailList){
            orderDetail.setOrderId(orderId);
        }
        orderDetailMapper.insertBatch(orderDetailList);

        orderService.paySuccess(orders.getNumber());

    }

    @Override
    public OrderStatisticsVO statisticsOrder() {

        List<Orders> list =new ArrayList<>();
        OrderStatisticsVO vo = new OrderStatisticsVO();

        list = ordersMapper.getByStatus(Orders.CONFIRMED);
        vo.setConfirmed(list.size());
        list = ordersMapper.getByStatus(Orders.DELIVERY_IN_PROGRESS);
        vo.setDeliveryInProgress(list.size());
        list = ordersMapper.getByStatus(Orders.TO_BE_CONFIRMED);
        vo.setToBeConfirmed(list.size());
        return vo;
    }

    @Override
    public void confirmOrder(Long id) {
        Orders orders = ordersMapper.getByOrderId(id);
        orders.setStatus(Orders.CONFIRMED);
        ordersMapper.update(orders);
    }

    @Override
    public void rejectOrder(OrdersRejectionDTO ordersRejectionDTO) {
        Orders orders = ordersMapper.getByOrderId(ordersRejectionDTO.getId());
        orders.setStatus(Orders.CANCELLED);
        orders.setRejectionReason(ordersRejectionDTO.getRejectionReason());
        orders.setCancelTime(LocalDateTime.now());
        ordersMapper.update(orders);
    }

    @Override
    public void deliveryOrder(Long id) {
        Orders orders = ordersMapper.getByOrderId(id);
        orders.setStatus(Orders.DELIVERY_IN_PROGRESS);
        ordersMapper.update(orders);
    }

    @Override
    public void completeOrder(Long id) {
        Orders orders = ordersMapper.getByOrderId(id);
        orders.setStatus(Orders.COMPLETED);
        orders.setDeliveryTime(LocalDateTime.now());
        ordersMapper.update(orders);
    }

    @Override
    public void remindOrder(Long id) {
        Map map = new HashMap();
        map.put("orderId", id);
        map.put("type",2);
        map.put("content","用户"+BaseContext.getCurrentId()+" 催单,订单号: "+ordersMapper.getByOrderId(id).getNumber());
        String msg = JSON.toJSONString(map);
        webSocketServer.sendToAllClient(msg);
    }

    public void checkOutOfRange(String address){
        // https://api.map.baidu.com/geocoding/v3/
        // ?address=北京市海淀区上地十街10号&output=json&ak=您的ak&callback=showLocation
        String URL = "https://api.map.baidu.com/geocoding/v3";
        String URL2 = "https://api.map.baidu.com/directionlite/v1/driving";
        Map<String,String> map = new HashMap<String,String>();
        map.put("address", shopAddress);
        map.put("output", "json");
        map.put("ak",ak);

        // 获取店铺地址
        String shopCallback = HttpClientUtil.doGet(URL,map);
        JSONObject jsonObject = JSONObject.parseObject(shopCallback);
        if(!jsonObject.getString("status").equals("0")){
            // TODO 异常信息规范化
            throw new OrderBusinessException("获取店铺地址失败");
        }
        jsonObject = jsonObject.getJSONObject("result");
        String lat = jsonObject.getJSONObject("location").getString("lat");
        String lng = jsonObject.getJSONObject("location").getString("lng");
        String origin = lat+","+lng;


        // 获取收货地址
        map.put("address",address);
        String userCallback = HttpClientUtil.doGet(URL,map);
        jsonObject = JSONObject.parseObject(userCallback);
        if(!jsonObject.getString("status").equals("0")){
            // TODO 异常信息规范化
            throw new OrderBusinessException("获取店铺地址失败");
        }
        jsonObject = jsonObject.getJSONObject("result");
        lat = jsonObject.getJSONObject("location").getString("lat");
        lng = jsonObject.getJSONObject("location").getString("lng");
        String destination = lat+","+lng;
        map.clear();
        map.put("ak",ak);
        map.put("destination",destination);
        map.put("origin",origin);
        map.put("step_info","0");
        JSONObject json = JSONObject.parseObject(HttpClientUtil.doGet(URL2,map));
        if(!json.getString("status").equals("0")){
            throw new OrderBusinessException("规划路线失败");
        }
        JSONArray jsonArray = json.getJSONObject("result").getJSONArray("routes");
        Integer distance = (Integer)((JSONObject)jsonArray.get(0)).get("distance");
        if(distance>5000){
            throw new OrderBusinessException("超出配送范围: "+distance);
        }

    }
}
