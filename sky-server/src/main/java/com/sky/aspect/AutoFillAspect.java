package com.sky.aspect;


import com.sky.annotation.AutoFill;
import com.sky.constant.AutoFillConstant;
import com.sky.context.BaseContext;
import com.sky.enumeration.OperationType;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import springfox.documentation.spi.service.contexts.OperationContext;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

@Aspect
@Component
@Slf4j
public class AutoFillAspect {

    /**
     * 公共字段补全 切入点
     */
    @Pointcut("execution(* com.sky.mapper.*.*(..)) && @annotation(com.sky.annotation.AutoFill)")
    public void autoFillPointcut() {}

    /**
     * 前置通知 公共字段补全切入点
     * @param joinPoint
     */
    @Before("autoFillPointcut()")
    public void beforeAutoFill(JoinPoint joinPoint) {
        // 获取操作类型 插入/更新
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        OperationType operationType = signature.getMethod().getAnnotation(AutoFill.class).value();

        // 获取操作对象
        Object[] args = joinPoint.getArgs();
        Object entity = args[0];

        // 获取操作数据
        LocalDateTime now = LocalDateTime.now();
        Long currentId = BaseContext.getCurrentId();

        // 根据操作类型操作
        if(operationType == OperationType.INSERT) {
            try {
                Method setCreateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_TIME, LocalDateTime.class);
                setCreateTime.invoke(entity, now);
                Method setCreateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_USER, Long.class);
                setCreateUser.invoke(entity, currentId);
                Method setUpdateUSer = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);
                setUpdateUSer.invoke(entity, currentId);
                Method setUpdateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                setUpdateTime.invoke(entity, now);

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }else if(operationType == OperationType.UPDATE) {
            try {
                Method setUpdateUSer = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);
                setUpdateUSer.invoke(entity, currentId);
                Method setUpdateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                setUpdateTime.invoke(entity, now);

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

    }
}
