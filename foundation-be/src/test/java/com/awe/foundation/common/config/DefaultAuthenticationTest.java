package com.awe.foundation.common.config;

import cn.dev33.satoken.annotation.SaIgnore;
import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.stp.StpUtil;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mockStatic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 验证默认鉴权与显式匿名声明
 */
class DefaultAuthenticationTest {

    /**
     * 未声明鉴权注解的新接口也必须登录
     *
     * @throws Exception 请求执行异常
     */
    @Test
    void shouldRequireLoginForUnannotatedEndpoint() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new AuthenticationTestController())
                .addInterceptors(new WebConfig().authenticationInterceptor()).build();
        try (MockedStatic<StpUtil> authentication = mockStatic(StpUtil.class)) {
            authentication.when(StpUtil::checkLogin).thenThrow(
                    new NotLoginException("login", NotLoginException.NOT_TOKEN, "missing token"));
            ServletException exception = assertThrows(ServletException.class,
                    () -> mockMvc.perform(get("/protected")));
            assertInstanceOf(NotLoginException.class, exception.getCause());
            authentication.verify(StpUtil::checkLogin);

        }

    }

    /**
     * 显式匿名接口不执行登录检查
     *
     * @throws Exception 请求执行异常
     */
    @Test
    void shouldAllowExplicitAnonymousEndpoint() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new AuthenticationTestController())
                .addInterceptors(new WebConfig().authenticationInterceptor()).build();
        try (MockedStatic<StpUtil> authentication = mockStatic(StpUtil.class)) {
            mockMvc.perform(get("/anonymous")).andExpect(status().isOk());
            authentication.verifyNoInteractions();

        }

    }

    /**
     * 已登录用户可以访问未声明权限的普通接口
     *
     * @throws Exception 请求执行异常
     */
    @Test
    void shouldAllowAuthenticatedEndpoint() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new AuthenticationTestController())
                .addInterceptors(new WebConfig().authenticationInterceptor()).build();
        try (MockedStatic<StpUtil> authentication = mockStatic(StpUtil.class)) {
            mockMvc.perform(get("/protected")).andExpect(status().isOk());
            authentication.verify(StpUtil::checkLogin);

        }

    }

}

/**
 * 默认鉴权测试接口
 */
@RestController
class AuthenticationTestController {

    /**
     * 未声明鉴权注解的普通接口
     *
     * @return 请求结果
     */
    @GetMapping("/protected")
    public String protectedEndpoint() {
        return "ok";

    }

    /**
     * 显式匿名接口
     *
     * @return 请求结果
     */
    @SaIgnore
    @GetMapping("/anonymous")
    public String anonymousEndpoint() {
        return "ok";

    }

}
