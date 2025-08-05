package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.SetmealService;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Service
@Slf4j
public class SetmealServiceImpl implements SetmealService {

    @Autowired
    private SetmealMapper setmealMapper;
    @Autowired
    private SetmealDishMapper setmealDishMapper;

    /**
     * 根据ID查询套餐
     * @param id
     * @return
     */
    @Override
    public Setmeal getById(Long id) {
        return setmealMapper.getById(id);
    }

    /**
     * 分页查询套餐信息
     * @param setmealPageQueryDTO
     * @return
     */
    @Override
    public PageResult page(SetmealPageQueryDTO setmealPageQueryDTO) {
        PageHelper.startPage(setmealPageQueryDTO.getPage(),setmealPageQueryDTO.getPageSize());
        Page page = setmealMapper.page(setmealPageQueryDTO);
        return new PageResult(page.getTotal(),page.getResult());
    }

    /**
     * 新增菜品
     * @param setmealDTO
     */
    @Override
    @Transactional
    @CacheEvict(cacheNames="setmealCache", allEntries=true)
    public void addSetmeal(SetmealDTO setmealDTO) {
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO,setmeal);
        setmealMapper.insert(setmeal);
        Long setmealId = setmeal.getId();

        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        if(setmealDishes!=null&&!setmealDishes.isEmpty()){
            setmealDishes.forEach(setmealDish -> {
                setmealDish.setSetmealId(setmealId);
            });
            setmealDishMapper.insertBatch(setmealDishes);
        }

    }

    /**
     * 套餐状态修改
     * @param id
     * @param status
     */
    @Override
    @Transactional
    @CacheEvict(cacheNames = "setmealCache", allEntries=true)
    public void statusChange(Long id, Integer status) {
        Setmeal setmeal = setmealMapper.getById(id);
        setmeal.setStatus(status);
        if(StatusConstant.ENABLE.equals(setmeal.getStatus())){
            List<Dish> dishes= setmealDishMapper.getDishBySetmealIdAndStatus(setmeal.getId(),StatusConstant.DISABLE);
            if(dishes!=null && !dishes.isEmpty()){
                throw new DeletionNotAllowedException(MessageConstant.SETMEAL_ENABLE_FAILED);
            }
        }
        log.info("statusChange setmeal:{}",setmeal);
        setmealMapper.update(setmeal);
    }

    /**
     *批量删除套餐
     * @param ids
     */
    @Override
    @Transactional
    @CacheEvict(cacheNames="setmealCache", allEntries = true)
    public void delete(List<Long> ids) {
        // 检查是否有起售的套餐
        for(Long id:ids){
            Setmeal setmeal = setmealMapper.getById(id);
            if(setmeal.getStatus().equals(StatusConstant.ENABLE)){
                throw new DeletionNotAllowedException(MessageConstant.SETMEAL_ON_SALE);
            }
        }

        if(ids!=null&&!ids.isEmpty()){
            setmealMapper.deleteByList(ids);
            log.info("删除套餐成功ids:{}",ids);
            setmealDishMapper.deleteBySetmealIdList(ids);
        }

    }

    /**
     * 修改套餐信息
     * @param setmealDTO
     */
    @Override
    @Transactional
    @CacheEvict(cacheNames = "setmealCache", allEntries = true)
    public void update(SetmealDTO setmealDTO) {
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO,setmeal);
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();

        setmealMapper.update(setmeal);

        setmealDishMapper.deleteBySetmealId(setmeal.getId());
        if(setmealDishes!=null&&!setmealDishes.isEmpty()){
            for(SetmealDish setmealDish:setmealDishes){
                setmealDish.setSetmealId(setmeal.getId());
            }
            setmealDishMapper.insertBatch(setmealDishes);
        }
    }

    /**
     * 根据ID查询套餐+菜品信息
     * @param id
     * @return
     */
    @Override
    public SetmealVO getByIdWithDish(Long id) {
        Setmeal setmeal = setmealMapper.getById(id);
        SetmealVO setmealVO = new SetmealVO();
        BeanUtils.copyProperties(setmeal,setmealVO);
        Long setmealId = setmeal.getId();
        List<SetmealDish> setmealDishes = setmealDishMapper.getBySetmealId(setmealId);
        setmealVO.setSetmealDishes(setmealDishes);
        return setmealVO;
    }

    /**
     * 条件查询
     * @param setmeal
     * @return
     */
    public List<Setmeal> list(Setmeal setmeal) {
        List<Setmeal> list = setmealMapper.list(setmeal);
        return list;
    }

    /**
     * 根据id查询菜品选项
     * @param id
     * @return
     */
    public List<DishItemVO> getDishItemById(Long id) {
        return setmealMapper.getDishItemBySetmealId(id);
    }
}
