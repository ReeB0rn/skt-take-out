package com.sky.mapper;

import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.ShoppingCart;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ShoppingCartMapper {

    List<ShoppingCart> list(ShoppingCart shoppingCart);

    void updateNumberById(ShoppingCart newShoppingCart);

    void insert(ShoppingCart newShoppingCart);

    void cleanByUserId(Long userId);

    void deleteById(Long id);
}
