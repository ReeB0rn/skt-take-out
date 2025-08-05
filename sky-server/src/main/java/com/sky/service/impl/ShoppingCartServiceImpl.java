package com.sky.service.impl;

import com.sky.context.BaseContext;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.entity.Setmeal;
import com.sky.entity.ShoppingCart;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class ShoppingCartServiceImpl implements ShoppingCartService {

    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private SetmealMapper setmealMapper;
    @Autowired
    private DishFlavorMapper dishFlavorMapper;
    @Autowired
    private ShoppingCartService shoppingCartService;

    /**
     * 添加购物车
     * @param shoppingCartDTO
     */
    @Override
    public void addShoppingCart(ShoppingCartDTO shoppingCartDTO) {

        // 获取购物车信息
        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO, shoppingCart);
        shoppingCart.setUserId(BaseContext.getCurrentId());

        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);
        ShoppingCart newShoppingCart = new ShoppingCart();
        // 判断添加商品是否 存在 不存在添加商品 存在则 数量加一
        // 存在
        if(list != null && list.size() > 0){
            newShoppingCart = list.get(0);
            newShoppingCart.setNumber(newShoppingCart.getNumber()+1);
            shoppingCartMapper.updateNumberById(newShoppingCart);
        }else{ //不存在
            Long dishId = shoppingCartDTO.getDishId();
            Long setmealId = shoppingCartDTO.getSetmealId();
            if(dishId!=null){
                Dish dish = dishMapper.getById(dishId);
                newShoppingCart = ShoppingCart.builder()
                        .image(dish.getImage())
                        .amount(dish.getPrice())
                        .dishId(dishId)
                        .name(dish.getName())
                        .createTime(LocalDateTime.now())
                        .userId(BaseContext.getCurrentId())
                        .number(1)
                        .dishFlavor(shoppingCartDTO.getDishFlavor())
                        .build();
            }
            else if(setmealId!=null){
                Setmeal setmeal = setmealMapper.getById(setmealId);
                newShoppingCart = ShoppingCart.builder()
                        .image(setmeal.getImage())
                        .amount(setmeal.getPrice())
                        .setmealId(setmealId)
                        .name(setmeal.getName())
                        .createTime(LocalDateTime.now())
                        .userId(BaseContext.getCurrentId())
                        .number(1)
                        .build();
            }

            shoppingCartMapper.insert(newShoppingCart);
        }

    }

    /**
     * 用户查看购物车
     * @param userId
     * @return
     */
    @Override
    public List<ShoppingCart> list(Long userId) {
        ShoppingCart shoppingCart = ShoppingCart.builder()
                .userId(userId)
                .build();
        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);
        return list;
    }

    /**
     * 用户清空购物车
     * @param userId
     */
    @Override
    public void cleanByUserId(Long userId) {
        shoppingCartMapper.cleanByUserId(userId);
    }

    /**
     * 清除购物车中一个商品
     * @param shoppingCartDTO
     */
    @Override
    public void subShoppingCart(ShoppingCartDTO shoppingCartDTO) {
        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO, shoppingCart);
        shoppingCart.setUserId(BaseContext.getCurrentId());
        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);
        shoppingCart = list.get(0);
        shoppingCart.setNumber(shoppingCart.getNumber()-1);
        if(shoppingCart.getNumber()<=0){
            shoppingCartMapper.deleteById(shoppingCart.getId());
        }else {
            shoppingCartMapper.updateNumberById(shoppingCart);
        }

    }
}
