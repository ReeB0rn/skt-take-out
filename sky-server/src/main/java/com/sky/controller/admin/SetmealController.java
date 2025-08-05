package com.sky.controller.admin;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.SetmealService;
import com.sky.vo.SetmealVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Delete;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequestMapping ("/admin/setmeal")
@Api(tags="套餐接口")
public class SetmealController {

    @Autowired
    SetmealService setmealService;

    /**
     * ID查询套餐 + 菜品
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    @ApiOperation(value="ID查询套餐")
    public Result<SetmealVO> getById(@PathVariable Long id) {
        log.info("查询ID{}套餐",id);
        return Result.success(setmealService.getByIdWithDish(id));
    }

    /**
     * 分页查询套餐信息
     * @param setmealPageQueryDTO
     * @return
     */
    @GetMapping("/page")
    @ApiOperation(value="分页查询套餐信息")
    public Result<PageResult> page(SetmealPageQueryDTO setmealPageQueryDTO) {
        log.info("分页查询套餐信息:{}",setmealPageQueryDTO);
         return Result.success(setmealService.page(setmealPageQueryDTO));
    }

    /**
     * 新增菜品
     * @param setmealDTO
     * @return
     */
    @PostMapping
    @ApiOperation(value="新增菜品")
    public Result addSetmeal(@RequestBody SetmealDTO setmealDTO){
        log.info("新增菜品:{}",setmealDTO);
        setmealService.addSetmeal(setmealDTO);
        return Result.success();
    }

    /**
     * 套餐状态修改
     * @param status
     * @param id
     * @return
     */
    @PostMapping("status/{status}")
    @ApiOperation(value="套餐起售/停售")
    public Result statusChange(@PathVariable Integer status, Long id){
        log.info("套餐ID:{} 状态修改为{}",id,status);
        setmealService.statusChange(id,status);
        return Result.success();
    }

    /**
     * 批量删除套餐
     * @param ids
     * @return
     */
    @DeleteMapping
    @ApiOperation(value="批量删除套餐")
    public Result delete(@RequestParam List<Long> ids){
        log.info("批量删除套餐:{}",ids);
        setmealService.delete(ids);

        return Result.success();
    }

    @PutMapping
    @ApiOperation(value="修改套餐")
    public Result update(@RequestBody SetmealDTO setmealDTO){
        log.info("修改套餐:{}",setmealDTO);
        setmealService.update(setmealDTO);
        return Result.success();
    }
}
