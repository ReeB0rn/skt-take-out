package com.sky.service;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.vo.DishVO;

import java.util.List;

public interface DishService {
    void saveWithFlavor(DishDTO dishDTO);

    Result<PageResult> page(DishPageQueryDTO dishPageQueryDTO);

    void delete(List<Long> ids);

    Dish getById(Long id);

    DishVO getByIdWithFlavors(Long id);

    void updateWithFlavor(DishDTO dishDTO);

    List<Dish> getByCategoryId(Long categoryId);

    /**
     * 条件查询菜品和口味
     * @param dish
     * @return
     */
    List<DishVO> listWithFlavor(Dish dish);
}
