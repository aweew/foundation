package com.awe.foundation.manager.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.awe.foundation.manager.domain.user.entity.User;

/**
 * 系统用户服务接口
 *
 * @author Awe
 * @since 2025-12-10 16:03:20
 */
public interface IUserService extends IService<User> {

    User getByPhone(String phone);

}
