package com.awe.foundation.common.api;

import com.awe.foundation.common.exception.BusinessException;
import com.awe.foundation.manager.domain.user.entity.User;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 分页参数边界验证
 */
class PageRequestTest {

    /**
     * 初始化真实实体字段映射，无需连接数据库
     */
    @BeforeAll
    static void initializeEntityMapping() {
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""), User.class);

    }

    /**
     * 默认分页兼容现有前端参数
     */
    @Test
    void shouldUseDefaultPage() {
        Page<Object> page = new PageRequest().createPage(Object.class);
        assertEquals(1L, page.getCurrent());
        assertEquals(10L, page.getSize());
        Page<Object> builderPage = PageRequest.builder().build().createPage(Object.class);
        assertEquals(1L, builderPage.getCurrent());
        assertEquals(10L, builderPage.getSize());

    }

    /**
     * 分页构建拒绝无效范围，覆盖绕过 MVC 校验的调用
     */
    @Test
    void shouldRejectInvalidPageRange() {
        PageRequest request = new PageRequest();
        request.setSize(-1L);
        assertThrows(BusinessException.class, () -> request.createPage(Object.class));
        request.setSize(201L);
        assertThrows(BusinessException.class, () -> request.createPage(Object.class));
        request.setSize(10L);
        request.setCurrent(0L);
        assertThrows(BusinessException.class, () -> request.createPage(Object.class));
        request.setCurrent(null);
        assertThrows(BusinessException.class, () -> request.createPage(Object.class));

    }

    /**
     * 排序仅接受白名单字段和明确的排序方向
     */
    @Test
    void shouldRejectUnsafeSort() {
        PageRequest request = new PageRequest();
        request.setOrderBy("id desc; drop table sys_user");
        request.setOrderType("asc");
        assertThrows(BusinessException.class, () -> request.createPage(User.class));
        request.setOrderBy("id");
        request.setOrderType("invalid");
        assertThrows(BusinessException.class, () -> request.createPage(User.class));
        request.setOrderBy("id,name");
        request.setOrderType("asc");
        assertThrows(BusinessException.class, () -> request.createPage(User.class));
        request.setOrderBy("name");
        assertThrows(BusinessException.class, () -> request.createPage(User.class));

    }

    /**
     * 业务补充排序保留升降序语义
     */
    @Test
    void shouldHonorDescendingOrder() {
        PageRequest request = new PageRequest();
        request.addOrderItem("create_time", false);
        Page<User> page = request.createPage(User.class);
        assertEquals("create_time", page.orders().get(0).getColumn());
        assertFalse(page.orders().get(0).isAsc());

    }

    /**
     * 多字段排序保持请求顺序
     */
    @Test
    void shouldSupportSafeMultiColumnSort() {
        PageRequest request = new PageRequest();
        request.setOrderBy("create_time,id");
        request.setOrderType("desc,asc");
        Page<User> page = request.createPage(User.class);
        assertEquals(2, page.orders().size());
        assertFalse(page.orders().get(0).isAsc());
        assertTrue(page.orders().get(1).isAsc());

    }

    /**
     * 请求参数无法注入服务端排序项或原始 MyBatis 排序对象
     */
    @Test
    void shouldIgnoreClientSuppliedInternalOrderItems() {
        MockHttpServletRequest servletRequest = new MockHttpServletRequest();
        servletRequest.addParameter("serverOrderItems[0].column", "id desc; drop table sys_user");
        servletRequest.addParameter("_orderItems[0].column", "id desc; drop table sys_user");
        servletRequest.addParameter("orders[0].column", "id desc; drop table sys_user");
        PageRequest request = new PageRequest();
        new ServletRequestDataBinder(request).bind(servletRequest);
        assertTrue(request.createPage(User.class).orders().isEmpty());

    }

    /**
     * 合法业务列从当前实体映射自动获得排序资格
     */
    @Test
    void shouldSupportEntitySpecificColumn() {
        PageRequest request = new PageRequest();
        request.setOrderBy("nick_name");
        request.setOrderType("asc");
        assertEquals("nick_name", request.createPage(User.class).orders().get(0).getColumn());

    }

}
