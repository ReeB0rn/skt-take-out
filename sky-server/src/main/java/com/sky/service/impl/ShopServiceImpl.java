package com.sky.service.impl;

import com.sky.constant.ShopConstant;
import com.sky.service.ShopService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ShopServiceImpl implements ShopService {

    @Autowired
    RedisTemplate redisTemplate;

    /**
     *商店状态修改
     * @param status
     */
    @Override
    public void statusChange(Integer status) {
        redisTemplate.opsForValue().set(ShopConstant.SHOP_STATUS, status);
    }

    @Override
    public Integer getStatus() {
        return (Integer) redisTemplate.opsForValue().get(ShopConstant.SHOP_STATUS);
    }
}
