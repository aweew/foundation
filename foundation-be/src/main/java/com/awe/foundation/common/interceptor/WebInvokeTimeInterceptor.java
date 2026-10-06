package com.awe.foundation.common.interceptor;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.map.MapUtil;
import com.alibaba.ttl.TransmittableThreadLocal;
import com.awe.foundation.common.constant.ErrorCodeEnum;
import com.awe.foundation.common.filter.RepeatedlyRequestWrapper;
import com.awe.foundation.common.util.StringUtils;
import com.awe.foundation.common.util.JsonUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.StopWatch;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.io.BufferedReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Locale;
import java.util.Objects;

/**
 * web调用时间耗时统计拦截器
 *
 * @author Awe
 * @since 2025/12/8 15:56
 */
@RequiredArgsConstructor
@Slf4j
@Service
public class WebInvokeTimeInterceptor implements HandlerInterceptor {

    /**
     * 排除敏感属性字段
     */
    public static final Set<String> SENSITIVE_PROPERTIES = new HashSet<>(Arrays.asList(
            "password", "oldpassword", "newpassword", "confirmpassword", "token", "accesstoken",
            "refreshtoken", "authorization", "secret", "phone", "mobile", "idcard", "cookie",
            "webhookurl", "jwtsecretkey"));
    public static final String[] EXCLUDE_URL = {""};

    private final TransmittableThreadLocal<StopWatch> invokeTimeTL = new TransmittableThreadLocal<>();

    /**
     * 记录请求参数并启动耗时统计
     *
     * @param request 请求对象
     * @param response 响应对象
     * @param handler 请求处理器
     * @return 是否继续处理
     * @throws Exception 拦截器处理异常
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String url = request.getMethod() + " " + request.getRequestURI();

        boolean excluded = Arrays.stream(EXCLUDE_URL).anyMatch(url::contains);
        if (!excluded) {
            return true;
        }

        // 打印请求参数
        if (isJsonRequest(request)) {
            String jsonParam = "";
            if (request instanceof RepeatedlyRequestWrapper) {
                BufferedReader reader = request.getReader();
                jsonParam = maskJson(IoUtil.read(reader));
            }
            if (log.isDebugEnabled()) {
                log.debug("\n====================[请求开始]====================\n 开始请求 => URL[{}], 参数类型[json], 参数: [{}]", url, jsonParam);
            }

        } else {
            Map<String, String[]> parameterMap = request.getParameterMap();
            if (MapUtil.isNotEmpty(parameterMap)) {
                String parameters = JsonUtils.toJsonString(maskParameterMap(parameterMap));
                if (log.isDebugEnabled()) {
                    log.debug("\n====================[请求开始]====================\n 开始请求 => URL[{}], 参数类型[param], 参数: [{}]", url, parameters);
                }
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("\n====================[请求开始]====================\n 开始请求 => URL[{}], 无参数", url);
                }
            }
        }
        StopWatch stopWatch = new StopWatch();
        invokeTimeTL.set(stopWatch);
        stopWatch.start();
        return true;
    }

    /**
     * 处理请求完成后的 MVC 回调
     *
     * @param request 请求对象
     * @param response 响应对象
     * @param handler 请求处理器
     * @param modelAndView 模型和视图
     * @throws Exception 拦截器处理异常
     */
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        HandlerInterceptor.super.postHandle(request, response, handler, modelAndView);
    }

    /**
     * 记录请求耗时并清理线程上下文
     *
     * @param request 请求对象
     * @param response 响应对象
     * @param handler 请求处理器
     * @param ex 请求异常
     * @throws Exception 拦截器处理异常
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        StopWatch stopWatch = invokeTimeTL.get();
        if (Objects.nonNull(stopWatch)) {
            stopWatch.stop();
            if (log.isDebugEnabled()) {
                log.debug("结束请求 => URL[{}], 耗时: [{} ms]\n====================[请求结束]====================\n", request.getMethod() + " " + request.getRequestURI(), stopWatch.getTime());
            }
            invokeTimeTL.remove();
        }
    }

    /**
     * 判断本次请求的数据类型是否为json
     *
     * @param request request
     * @return boolean
     */
    private boolean isJsonRequest(HttpServletRequest request) {
        String contentType = request.getContentType();
        if (Objects.nonNull(contentType)) {
            return StringUtils.startsWithIgnoreCase(contentType, MediaType.APPLICATION_JSON_VALUE);
        }
        return false;
    }

    /**
     * 脱敏 JSON 请求体中的敏感字段
     *
     * @param json JSON 请求体
     * @return 脱敏后的 JSON
     */
    private String maskJson(String json) {
        if (StringUtils.isBlank(json)) {
            return json;
        }
        Map<?, ?> jsonObject = JsonUtils.parseMap(json);
        if (Objects.isNull(jsonObject)) {
            return ErrorCodeEnum.REQUEST_BODY_NOT_LOGGED.getMsg();
        }
        return JsonUtils.toJsonString(maskValue(jsonObject));
    }

    /**
     * 脱敏表单请求参数
     *
     * @param parameterMap 请求参数
     * @return 脱敏后的参数
     */
    private Map<String, Object> maskParameterMap(Map<String, String[]> parameterMap) {
        Map<String, Object> maskedParameters = new HashMap<>();
        parameterMap.forEach((name, values) -> maskedParameters.put(name,
                isSensitive(name) ? "***" : values));
        return maskedParameters;
    }

    /**
     * 递归脱敏对象中的敏感字段
     *
     * @param value 待处理对象
     * @return 脱敏后的对象
     */
    private Object maskValue(Object value) {
        if (value instanceof Map<?, ?> map) {
            Map<String, Object> maskedMap = new HashMap<>();
            map.forEach((key, item) -> maskedMap.put(String.valueOf(key),
                    isSensitive(String.valueOf(key)) ? "***" : maskValue(item)));
            return maskedMap;
        }
        if (value instanceof Iterable<?> iterable) {
            return Arrays.stream(toArray(iterable)).map(this::maskValue).toList();
        }
        return value;
    }

    /**
     * 将可迭代对象转换为数组
     *
     * @param iterable 可迭代对象
     * @return 对象数组
     */
    private Object[] toArray(Iterable<?> iterable) {
        java.util.ArrayList<Object> values = new java.util.ArrayList<>();
        iterable.forEach(values::add);
        return values.toArray();
    }

    /**
     * 判断字段名是否属于敏感字段
     *
     * @param fieldName 字段名
     * @return 是否为敏感字段
     */
    private boolean isSensitive(String fieldName) {
        return SENSITIVE_PROPERTIES.contains(fieldName.toLowerCase(Locale.ROOT));
    }

}
