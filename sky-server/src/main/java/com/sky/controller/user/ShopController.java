package com.sky.controller.user;

import com.sky.result.Result;
import com.sky.service.ShopService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController("UserShopController")
@RequestMapping("/user/shop")
@Slf4j
@Api(tags="用户端商店接口")
public class ShopController {

    @Autowired
    private ShopService shopService;


    @GetMapping("/status")
    @ApiOperation(value="查询商店状态")
    public Result<Integer> getStatus(){
        Integer status = shopService.getStatus();
        return Result.success(status);
    }
}
