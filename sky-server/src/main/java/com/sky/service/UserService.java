package com.sky.service;

import com.sky.dto.UserLoginDTO;
import com.sky.vo.UserLoginVO;

import javax.security.auth.login.LoginException;

public interface UserService {
    UserLoginVO login(UserLoginDTO userLoginDTO) throws LoginException;
}
