package com.awe.foundation.manager.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.awe.foundation.manager.domain.user.entity.User;
import com.awe.foundation.manager.mapper.UserMapper;
import com.awe.foundation.manager.service.IUserService;
import org.springframework.stereotype.Service;

/**
 * 系统用户服务实现类
 *
 * @author Awe
 * @since 2025-12-10 16:03:20
 */
@Service("userService")
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    @Override
    public User getByPhone(String phone) {
        return this.baseMapper.selectOne(Wrappers.lambdaQuery(User.class).eq(User::getPhone, phone));
    }

}
