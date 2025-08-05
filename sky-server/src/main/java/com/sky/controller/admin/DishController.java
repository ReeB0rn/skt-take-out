package com.sky.controller.admin;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.mapper.DishFlavorMapper;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/admin/dish")
@Slf4j
@Api(tags="菜品接口")
public class DishController {

    @Autowired
    private DishService dishService;
    @Autowired
    private DishFlavorMapper dishFlavorMapper;
    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 新增菜品
     * @param dishDTO
     * @return
     */
    @PostMapping
    @ApiOperation(value="新增菜品")
    public Result save(@RequestBody DishDTO dishDTO) {
        log.info("新增菜品:{}", dishDTO);
        dishService.saveWithFlavor(dishDTO);
        String pattern = "dish_"+dishDTO.getCategoryId();
        return Result.success();
    }

    /**
     * 菜品分页查询
     * @param dishPageQueryDTO
     * @return
     */
    @GetMapping("/page")
    @ApiOperation(value="菜品分页查询")
    public Result<PageResult> page(DishPageQueryDTO dishPageQueryDTO){
        log.info("菜品分页查询:{}", dishPageQueryDTO);
        return dishService.page(dishPageQueryDTO);
    }

    /**
     * 批量删除菜品
     * @param ids
     * @return
     */
    @DeleteMapping
    @ApiOperation(value="批量删除菜品")
    public Result delete(@RequestParam List<Long> ids) {
        log.info("批量删除菜品:{}", ids);
        dishService.delete(ids);
        return Result.success();
    }

    /**
     * 根据ID查询菜品信息 + 口味信息
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    @ApiOperation(value = "根据ID查询菜品信息")
    public Result<DishVO> getByIdWithFlavors(@PathVariable Long id){
        log.info("根据ID查询菜品信息:{}", id);
        DishVO dishVO = dishService.getByIdWithFlavors(id);
        return Result.success(dishVO);
    }

    /**
     * 修改菜品信息
     * @param dishDTO
     * @return
     */
    @PutMapping
    @ApiOperation(value = "修改菜品信息")
    public Result update(@RequestBody DishDTO dishDTO) {
        log.info("修改菜品信息:{}", dishDTO);
        dishService.updateWithFlavor(dishDTO);
        return Result.success();
    }

    @GetMapping("/list")
    @ApiOperation("根据菜品分类ID查询菜品")
    public Result<List<Dish>> getByCategoryId(Long categoryId){
        log.info("分类ID:{}查询菜品信息", categoryId);
        return Result.success(dishService.getByCategoryId(categoryId));
    }


}
