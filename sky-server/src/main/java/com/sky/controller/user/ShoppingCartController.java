package com.sky.controller.user;

import com.sky.context.BaseContext;
import com.sky.dto.ShoppingCartDTO;
import com.sky.result.Result;
import com.sky.service.ShoppingCartService;
import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.sky.entity.ShoppingCart;

import java.util.List;

@RestController
@RequestMapping("/user/shoppingCart")
@Slf4j
@Api(tags="C端-购物车接口")
public class ShoppingCartController {

    @Autowired
    private ShoppingCartService shoppingCartService;

    @PostMapping("/add")
    @ApiOperation(value="添加购物车接口")
    public Result add(@RequestBody ShoppingCartDTO shoppingCartDTO){
        log.info("添加购物车:{}",shoppingCartDTO);
        shoppingCartService.addShoppingCart(shoppingCartDTO);
        return Result.success();
    }

    @GetMapping("/list")
    @ApiOperation(value="查看购物车")
    public Result<List<ShoppingCart>> list(){
        log.info("查询用户ID:{} 购物车", BaseContext.getCurrentId());
        List<ShoppingCart>list = shoppingCartService.list(BaseContext.getCurrentId());
        return Result.success(list);
    }

    @DeleteMapping("/clean")
    @ApiOperation(value="清空购物车")
    public Result cleanShoppingCart(){
        log.info("清空用户ID:{} 购物车", BaseContext.getCurrentId());
        shoppingCartService.cleanByUserId(BaseContext.getCurrentId());
        return Result.success();
    }

    @PostMapping("/sub")
    @ApiOperation(value="删除购物车中一个商品")
    public Result subShoppingCart(@RequestBody ShoppingCartDTO shoppingCartDTO){
        log.info("删除购物车中一个商品:{}",shoppingCartDTO);
        shoppingCartService.subShoppingCart(shoppingCartDTO);
        return Result.success();
    }
}
