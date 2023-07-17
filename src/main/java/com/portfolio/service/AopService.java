package com.portfolio.service;

import org.aspectj.lang.annotation.*;

import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

@Component
@Aspect
public class AopService {
    @Pointcut("execution(!void com.portfolio.apis ..*.*(..)) && !@annotation(com.portfolio.annotations.NoLogging)")
    public void allPointcut() {}
    @AfterReturning(value = "allPointcut()", returning = "returnObj")
    public Object after(Object returnObj) {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String token = request.getHeader("Authorization").replace("Bearer ", "");
        if (request.getAttribute("newAuthorization") != null) {
            token = request.getAttribute("newAuthorization").toString().replace("Bearer ", "");
        }
        if (returnObj instanceof Map) {
            Map<String, Object> map = (Map<String, Object>) returnObj;
            if (!map.containsKey("token")) {
                map.put("token", token);
            }
        }
        return returnObj;
    }
}
