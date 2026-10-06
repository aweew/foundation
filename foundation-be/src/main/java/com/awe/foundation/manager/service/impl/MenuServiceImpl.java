package com.awe.foundation.manager.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.awe.foundation.common.constant.enums.StatusEnum;
import com.awe.foundation.manager.domain.menu.convert.MenuConvert;
import com.awe.foundation.manager.domain.menu.dto.req.MenuReq;
import com.awe.foundation.manager.domain.menu.dto.resp.MenuResp;
import com.awe.foundation.manager.domain.menu.entity.Menu;
import com.awe.foundation.manager.mapper.MenuMapper;
import com.awe.foundation.manager.service.IMenuService;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * 系统权限服务实现类
 *
 * @author Awe
 * @since 2025-12-11 16:45:01
 */
@Service("menuService")
public class MenuServiceImpl extends ServiceImpl<MenuMapper, Menu> implements IMenuService {

    @Resource
    private MenuConvert menuConvert;

    @Override
    /**
     * 查询全部启用菜单
     *
     * @return 菜单列表
     */
    public List<MenuResp> listAllMenus() {
        // 全部权限，树状
        List<Menu> allMenus = this.list();

        // 过滤和排序
        return allMenus.stream()
                .filter(t -> StatusEnum.ENABLE.equals(t.getStatus()))
                .map(menuConvert::toResp)
                .sorted(Comparator.comparing(MenuResp::getSort, Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();
    }

    @Override
    /**
     * 查询全部启用菜单树
     *
     * @return 菜单树
     */
    public List<MenuResp> treeAllMenus() {
        // 全部权限，树状
        List<Menu> allMenus = this.list();

        // 过滤和排序
        List<MenuResp> allMenuResps = allMenus.stream()
                .filter(t -> StatusEnum.ENABLE.equals(t.getStatus()))
                .map(menuConvert::toResp)
                .sorted(Comparator.comparing(MenuResp::getSort, Comparator.nullsLast(Comparator.naturalOrder())))
                .collect(Collectors.toList());
        if (CollUtil.isNotEmpty(allMenuResps)) {
            // 根据根菜单节点挂子菜单
            List<MenuResp> allRootMenus = allMenuResps.stream()
                    .filter(t -> Objects.isNull(t.getParentId()))
                    .toList();
            for (MenuResp Menu : allRootMenus) {
                Menu.setChildList(getChild(Menu.getId(), allMenuResps, menuResp -> true));
            }
            return allRootMenus;
        }
        return Collections.emptyList();
    }

    @Override
    /**
     * 按条件查询菜单树
     *
     * @param req 菜单查询条件
     * @return 菜单树
     */
    public List<MenuResp> treeAllMenus(MenuReq req) {
        // 全部权限，树状
        List<Menu> allMenus = this.list(Wrappers.lambdaQuery(menuConvert.toEntity(req)));

        // 过滤和排序
        List<MenuResp> allMenuResps = allMenus.stream()
                .filter(t -> StatusEnum.ENABLE.equals(t.getStatus()))
                .map(menuConvert::toResp)
                .sorted(Comparator.comparing(MenuResp::getSort, Comparator.nullsLast(Comparator.naturalOrder())))
                .collect(Collectors.toList());
        if (CollUtil.isNotEmpty(allMenuResps)) {
            // 根据根菜单节点挂子菜单
            List<MenuResp> allRootMenus = allMenuResps.stream()
                    .filter(t -> Objects.isNull(t.getParentId()))
                    .toList();
            for (MenuResp Menu : allRootMenus) {
                Menu.setChildList(getChild(Menu.getId(), allMenuResps, menuResp -> true));
            }
            return allRootMenus;
        }
        return Collections.emptyList();
    }

    @Override
    /**
     * 查询用户可见菜单
     *
     * @param userId 用户 ID
     * @return 菜单列表
     */
    public List<MenuResp> listAllMenusByUserId(Long userId) {
        return this.baseMapper.listAllMenusByUserId(userId);
    }

    @Override
    /**
     * 查询用户可见菜单树
     *
     * @param userId 用户 ID
     * @return 菜单树
     */
    public List<MenuResp> treeAllMenusByUserId(Long userId) {
        List<MenuResp> allUserMenus = this.listAllMenusByUserId(userId);
        // 过滤和排序
        List<MenuResp> allMenuResps = allUserMenus.stream()
                .filter(t -> StatusEnum.ENABLE.equals(t.getStatus()))
                .sorted(Comparator.comparing(MenuResp::getSort, Comparator.nullsLast(Comparator.naturalOrder())))
                .collect(Collectors.toList());
        if (CollUtil.isNotEmpty(allMenuResps)) {
            // 根据根菜单节点挂子菜单
            List<MenuResp> allRootMenus = allMenuResps.stream()
                    .filter(t -> Objects.isNull(t.getParentId()))
                    .toList();
            for (MenuResp Menu : allRootMenus) {
                Menu.setChildList(getChild(Menu.getId(), allMenuResps, menuResp -> true));
            }
            return allRootMenus;
        }
        return Collections.emptyList();
    }

    @Override
    /**
     * 查询角色关联菜单
     *
     * @param roleId 角色 ID
     * @return 菜单列表
     */
    public List<MenuResp> listAllMenusByRoleId(Long roleId) {
        return this.baseMapper.listAllMenusByRoleId(roleId);
    }

    @Override
    /**
     * 查询角色关联菜单树
     *
     * @param roleId 角色 ID
     * @return 菜单树
     */
    public List<MenuResp> treeAllMenusByRoleId(Long roleId) {
        List<MenuResp> menuRespList = this.baseMapper.listAllMenusByRoleId(roleId);
        if (CollUtil.isEmpty(menuRespList)) {
            return Collections.emptyList();
        }

        List<MenuResp> enabledMenus = menuRespList.stream()
                .filter(menu -> StatusEnum.ENABLE.equals(menu.getStatus()))
                .sorted(Comparator.comparing(MenuResp::getSort, Comparator.nullsLast(Comparator.naturalOrder())))
                .collect(Collectors.toList());
        List<MenuResp> rootMenus = enabledMenus.stream()
                .filter(menu -> Objects.isNull(menu.getParentId()))
                .toList();
        for (MenuResp rootMenu : rootMenus) {
            rootMenu.setChildList(getChild(rootMenu.getId(), enabledMenus, menuResp -> true));
        }
        return rootMenus;
    }

    @Override
    /**
     * 查询用户权限标识
     *
     * @param userId 用户 ID
     * @return 权限标识列表
     */
    public List<String> listPermissionsByUserId(Long userId) {
        return this.baseMapper.listPermissionsByUserId(userId);
    }

    @Override
    /**
     * 查询角色权限标识
     *
     * @param roleId 角色 ID
     * @return 权限标识列表
     */
    public List<String> listPermissionsByRoleId(Long roleId) {
        return this.baseMapper.listPermissionsByRoleId(roleId);
    }

    /**
     * 递归组装菜单子节点
     *
     * @param id 父菜单 ID
     * @param rootMenu 菜单集合
     * @param predicate 子菜单过滤条件
     * @return 子菜单列表
     */
    private List<MenuResp> getChild(Long id, List<MenuResp> rootMenu, Predicate<MenuResp> predicate) {
        List<MenuResp> childList = new ArrayList<>();
        for (MenuResp Menu : rootMenu) {
            if (Objects.nonNull(Menu.getParentId())) {
                if (Menu.getParentId().equals(id)) {
                    if (predicate.test(Menu)) {
                        childList.add(Menu);
                    }
                }
            }
        }
        for (MenuResp Menu : childList) {
            Menu.setChildList(getChild(Menu.getId(), rootMenu, predicate));
        }
        if (CollUtil.isEmpty(childList)) {
            return Collections.emptyList();
        }
        return childList.stream()
                .sorted(Comparator.comparing(MenuResp::getSort,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .collect(Collectors.toList());
    }

}
