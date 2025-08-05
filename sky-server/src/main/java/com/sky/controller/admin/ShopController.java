package com.sky.controller.admin;

import com.sky.result.Result;
import com.sky.service.ShopService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController("AdminShopController")
@RequestMapping("/admin/shop")
@Slf4j
@Api(tags="管理端店铺接口")
public class ShopController {

    @Autowired
    private ShopService shopService;

    @PutMapping("/{status}")
    @ApiOperation(value="店铺状态修改")
    public Result statusChange(@PathVariable Integer status){
        log.info("店铺状态修改:{}",status);
        shopService.statusChange(status);
        return Result.success();
    }

    @GetMapping("/status")
    @ApiOperation(value="获取店铺状态")
    public Result<Integer> getStatus(){
        return Result.success(shopService.getStatus());
    }
}
