package com.awe.foundation.manager.service;

import com.awe.foundation.manager.domain.auth.dto.req.ChangePasswordReq;
import com.awe.foundation.manager.domain.auth.dto.resp.UserInfoResp;
import com.awe.foundation.manager.domain.user.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 系统用户服务接口
 *
 * @author Awe
 * @since 2025-12-10 16:03:20
 */
public interface IUserService extends IService<User> {

    User getByPhone(String phone);

    UserInfoResp getUserInfo(Long userId);

    /**
     * 修改用户密码
     *
     * @param userId 用户 ID
     * @param req    修改密码请求
     */
    void changePassword(Long userId, ChangePasswordReq req);

}
