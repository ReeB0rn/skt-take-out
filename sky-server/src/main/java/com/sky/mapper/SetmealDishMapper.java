package com.sky.mapper;

import com.sky.entity.Dish;
import com.sky.entity.SetmealDish;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SetmealDishMapper {


    List<Long> getSetmealIdsByDishIds(List<Long> dishIds);

    void insertBatch(List<SetmealDish> setmealDishes);

    void deleteBySetmealIdList (List<Long> ids);

    void deleteBySetmealId(Long setmealId);

    List<SetmealDish> getBySetmealId(Long setmealId);

    List<Dish> getDishBySetmealIdAndStatus(Long setmealId, Integer status);
}
