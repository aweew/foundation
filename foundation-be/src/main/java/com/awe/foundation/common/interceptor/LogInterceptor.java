package com.awe.foundation.common.interceptor;

import cn.dev33.satoken.stp.StpUtil;
import com.awe.foundation.common.constant.Constants;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;


/**
 * 日志拦截器
 *
 * @author Awe
 * @since 2023/11/22 11:38
 */
@Slf4j
public class LogInterceptor implements HandlerInterceptor {

    /**
     * 设置当前请求的用户日志上下文
     *
     * @param request 请求对象
     * @param response 响应对象
     * @param handler 请求处理器
     * @return 是否继续处理
     * @throws Exception 拦截器处理异常
     */
    @Override
    public boolean preHandle(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) throws Exception {

        // todo 添加客户端类型等信息

        // 记录当前登录人员信息
        String userId = request.getHeader(Constants.USER_ID);
        if (StringUtils.isEmpty(userId)) {
            try {
                userId = StpUtil.getLoginIdAsString();
            } catch (Exception e) {
            }
        }

        if (StringUtils.isNotBlank(userId)) {
            MDC.put(Constants.USER_ID, userId);
            response.addHeader(Constants.USER_ID, userId);
        } else {
            MDC.remove(Constants.USER_ID);
        }
        return HandlerInterceptor.super.preHandle(request, response, handler);
    }

    /**
     * 处理请求完成前的 MVC 回调
     *
     * @param request 请求对象
     * @param response 响应对象
     * @param handler 请求处理器
     * @param modelAndView 模型和视图
     * @throws Exception 拦截器处理异常
     */
    @Override
    public void postHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler, ModelAndView modelAndView) throws Exception {
        HandlerInterceptor.super.postHandle(request, response, handler, modelAndView);
    }

    /**
     * 清理当前请求的日志上下文
     *
     * @param request 请求对象
     * @param response 响应对象
     * @param handler 请求处理器
     * @param ex 请求异常
     * @throws Exception 拦截器处理异常
     */
    @Override
    public void afterCompletion(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler, Exception ex) throws Exception {
        MDC.remove(Constants.TRACE_ID);
        MDC.remove(Constants.USER_ID);

        HandlerInterceptor.super.afterCompletion(request, response, handler, ex);
    }

}
