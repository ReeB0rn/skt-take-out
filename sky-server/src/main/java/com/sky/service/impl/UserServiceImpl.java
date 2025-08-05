package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import com.sky.constant.JwtClaimsConstant;
import com.sky.constant.MessageConstant;
import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;
import com.sky.exception.LoginFailedException;
import com.sky.mapper.UserMapper;
import com.sky.properties.JwtProperties;
import com.sky.properties.WeChatProperties;
import com.sky.service.UserService;
import com.sky.utils.HttpClientUtil;
import com.sky.utils.JwtUtil;
import com.sky.vo.UserLoginVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.security.auth.login.LoginException;
import java.time.LocalDateTime;
import java.util.Map;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    @Autowired
    private JwtProperties jwtProperties;
    @Autowired
    private WeChatProperties weChatProperties;
    @Autowired
    private UserMapper userMapper;

    public static final String LoginURL = "https://api.weixin.qq.com/sns/jscode2session";
    @Override
    public UserLoginVO login(UserLoginDTO userLoginDTO) throws LoginException {

        User user;
        Map<String, String> params = Maps.newHashMap();
        params.put("appid",weChatProperties.getAppid());
        params.put("secret",weChatProperties.getSecret());
        params.put("js_code",userLoginDTO.getCode());
        params.put("grant_type","authorization_code");
        // 获取用户信息


        // 向微信服务接口发送CODE 获取当前微信用户ID
        HttpClientUtil httpClientUtil = new HttpClientUtil();
        String json = httpClientUtil.doGet(LoginURL,params);
        JSONObject jsonObject = JSON.parseObject(json);
        String openid = jsonObject.getString("openid");
        // 判断openid是否为空即登录失败
        if(openid==null || "".equals(openid)){
            throw new LoginFailedException(MessageConstant.LOGIN_FAILED);
        }

        // 判断当前用户是否为新用户
        user = userMapper.getUserByOpenid(openid);
        if(user == null){
            user = user.builder()
                    .openid(openid)
                    .createTime(LocalDateTime.now())
                    .build();
            userMapper.insert(user);
        }

        // 生成JWT令牌

        Map<String , Object> claims = Maps.newHashMap();
        claims.put(JwtClaimsConstant.USER_ID,user.getId());

        String token = JwtUtil.createJWT(jwtProperties.getUserSecretKey(),
                jwtProperties.getUserTtl(),
                claims);

        UserLoginVO userLoginVO = new UserLoginVO();
        userLoginVO.setOpenid(user.getOpenid());
        userLoginVO.setId(user.getId());
        userLoginVO.setToken(token);

        return userLoginVO;
    }
}
