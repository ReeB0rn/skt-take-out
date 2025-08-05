package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@Slf4j
public class DishServiceImpl implements DishService {

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private DishFlavorMapper dishFlavorMapper;
    @Autowired
    private SetmealDishMapper setmealDishMapper;
    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 新增菜品+口味
     * @param dishDTO
     */
    @Override
    @Transactional
    public void saveWithFlavor(DishDTO dishDTO) {
        // 插入一条菜品信息
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        dishMapper.insert(dish);
        Long id = dish.getId();
        // 插入多条口味信息
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if(flavors != null && !flavors.isEmpty()) {
            flavors.forEach(flavor -> {
                flavor.setDishId(id);
            });
            dishFlavorMapper.insertBatch(flavors);
        }
        Long categoryId = dishDTO.getCategoryId();
        String pattern = "dish_"+categoryId;
        cleanCache(pattern);
        log.info("插入操作执行清除缓存操作,categoryId:{}", categoryId);
    }

    /**
     * 菜品分页查询
     * @param dishPageQueryDTO
     */
    @Override
    public Result<PageResult> page(DishPageQueryDTO dishPageQueryDTO) {
        PageHelper.startPage(dishPageQueryDTO.getPage(),dishPageQueryDTO.getPageSize());
        Page<DishVO> page = dishMapper.page(dishPageQueryDTO);
        PageResult pageResult = new PageResult(page.getTotal(),page.getResult());
        return  Result.success(pageResult);
    }

    /**
     * 批量删除菜品
     * @param ids
     */
    @Override
    @Transactional
    public void delete(List<Long> ids) {
        // 起售菜品不可以删除
        for(Long id : ids) {
            Dish dish = dishMapper.getById(id);
            if(dish.getStatus()== StatusConstant.ENABLE){
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }
        }

        // 套菜中菜品不可删除
        List<Long> setmealIds = setmealDishMapper.getSetmealIdsByDishIds(ids);
        if(setmealIds != null && !setmealIds.isEmpty()) {
            throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
        }

        // 删除菜品
        for(Long id : ids){
            dishMapper.deleteById(id);
            // 删除口味
            dishFlavorMapper.deleteByDishId(id);
        }

        cleanCache("dish_*");
        log.info("删除操作执行清除缓存操作");
    }

    /**
     * 根据ID查询菜品信息
     * @param id
     * @return
     */
    @Override
    public Dish getById(Long id) {
        return dishMapper.getById(id);
    }

    /**
     * 根据ID查询菜品信息+口味信息
     * @param id
     * @return
     */
    @Override
    public DishVO getByIdWithFlavors(Long id) {
        DishVO dishVO = new DishVO();
        Dish dish = dishMapper.getById(id);
        BeanUtils.copyProperties(dish, dishVO);
        dishVO.setFlavors(dishFlavorMapper.getByDishId(id));
        return dishVO;
    }

    /**
     * 修改菜品信息
     * @param dishDTO
     */
    @Override
    @Transactional
    public void updateWithFlavor(DishDTO dishDTO) {
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        dishMapper.update(dish);

        List<DishFlavor> flavors = dishDTO.getFlavors();
        dishFlavorMapper.deleteByDishId(dishDTO.getId());
        log.info("口味信息新:{}",flavors);
        if(flavors != null && !flavors.isEmpty()) {
            flavors.forEach(flavor -> {
                flavor.setDishId(dish.getId());
            });
            dishFlavorMapper.insertBatch(flavors);
        }

        log.info("增加口味完毕");

        Long categoryId = dishDTO.getCategoryId();
        String pattern = "dish_*";
        cleanCache(pattern);
        log.info("插入操作执行清除缓存操作,categoryId:{}", categoryId);
    }

    @Override
    public List<Dish>getByCategoryId(Long categoryId) {
        return dishMapper.getByCategoryId(categoryId);
    }

    /**
     * 条件查询菜品和口味
     * @param dish
     * @return
     */
    public List<DishVO> listWithFlavor(Dish dish) {

        // 判断缓存
        Long categoryId = dish.getCategoryId();
        List<DishVO> list = (List<DishVO>)redisTemplate.opsForValue().get("dish_"+categoryId);
        if(list!=null&&list.size()>0){
            log.info("从缓存中读取到菜品:{}",list);
            return list;
        }

        // 根据 Dish 的 categoryId 和 status 获取 list<dish>
        List<Dish> dishList = dishMapper.list(dish);
        List<DishVO> dishVOList = new ArrayList<>();

        for (Dish d : dishList) {
            DishVO dishVO = new DishVO();
            BeanUtils.copyProperties(d,dishVO);

            //根据菜品id查询对应的口味
            List<DishFlavor> flavors = dishFlavorMapper.getByDishId(d.getId());

            dishVO.setFlavors(flavors);
            dishVOList.add(dishVO);
        }
        redisTemplate.opsForValue().set("dish_"+categoryId,dishVOList);
        log.info("缓存中未读取到数据,从数据中读取数据并返回");

        return dishVOList;
    }

    private void cleanCache(String pattern){
        Set keys = redisTemplate.keys(pattern);
        redisTemplate.delete(keys);
    }
}
