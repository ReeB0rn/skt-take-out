package com.sky.controller.user;


import com.sky.dto.UserLoginDTO;
import com.sky.result.Result;
import com.sky.service.UserService;
import com.sky.vo.UserLoginVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.security.auth.login.LoginException;

@RestController
@RequestMapping("/user/user")
@Slf4j
@Api(tags="用户接口")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    @ApiOperation(value="用户登录")
    public Result<UserLoginVO> login(@RequestBody UserLoginDTO userLoginDTO) throws LoginException {
        log.info("用户登录:{}",userLoginDTO.getCode());
        UserLoginVO userLoginVO = userService.login(userLoginDTO);
        log.info("登陆成功:{}",userLoginVO);
        return Result.success(userLoginVO);

    }



}
