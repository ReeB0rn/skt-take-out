package com.sky.service;

import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.ShoppingCart;

import java.util.List;

public interface ShoppingCartService {

    void addShoppingCart(ShoppingCartDTO shoppingCartDTO);

    List<ShoppingCart> list(Long userId);

    void cleanByUserId(Long userId);

    void subShoppingCart(ShoppingCartDTO shoppingCartDTO);
}
