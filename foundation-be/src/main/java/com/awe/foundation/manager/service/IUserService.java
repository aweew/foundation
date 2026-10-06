package com.awe.foundation.manager.service;

import com.awe.foundation.common.api.Result;
import com.awe.foundation.manager.domain.auth.dto.resp.UserInfoResp;
import com.awe.foundation.manager.domain.user.dto.resp.UserResp;
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

    UserInfoResp getUserInfo(Long userId);

}
