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
        return null;
    }

    @Override
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
        return null;
    }

    @Override
    public List<MenuResp> listAllMenusByUserId(Long userId) {
        return this.baseMapper.listAllMenusByUserId(userId);
    }

    @Override
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
        return null;
    }

    @Override
    public List<MenuResp> listAllMenusByRoleId(Long roleId) {
        return this.baseMapper.listAllMenusByRoleId(roleId);
    }

    @Override
    public List<MenuResp> treeAllMenusByRoleId(Long roleId) {
        return this.baseMapper.treeAllMenusByRoleId(roleId);
    }

    @Override
    public List<String> listPermissionsByUserId(Long userId) {
        return this.baseMapper.listPermissionsByUserId(userId);
    }

    @Override
    public List<String> listPermissionsByRoleId(Long roleId) {
        return this.baseMapper.listPermissionsByRoleId(roleId);
    }

    private List<MenuResp> getChild(Long id, List<MenuResp> rootAcl, Predicate<MenuResp> predicate) {
        List<MenuResp> childList = new ArrayList<>();
        for (MenuResp Menu : rootAcl) {
            if (Objects.nonNull(Menu.getParentId())) {
                if (Menu.getParentId().equals(id)) {
                    if (predicate.test(Menu)) {
                        childList.add(Menu);
                    }
                }
            }
        }
        for (MenuResp Menu : childList) {
            Menu.setChildList(getChild(Menu.getId(), rootAcl, predicate));
        }
        if (CollUtil.isEmpty(childList)) {
            return null;
        }
        return childList.stream()
                .sorted(Comparator.comparing(MenuResp::getSort,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .collect(Collectors.toList());
    }

}
