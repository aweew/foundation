package com.awe.foundation.manager.service;

import com.awe.foundation.manager.domain.auth.dto.req.LoginReq;
import com.awe.foundation.manager.domain.auth.dto.resp.LoginResp;

/**
 * @author Awe
 * @since 2025/12/11 13:08
 */
public interface IAuthStrategy {

    static LoginResp doLogin(LoginReq loginReq) {
        IAuthStrategy authStrategy = AuthStrategyHelper.getAuthStrategy(loginReq.getGrantType());
        authStrategy.validate(loginReq);
        return authStrategy.login(loginReq);
    }

    void validate(LoginReq loginReq);

    LoginResp login(LoginReq loginReq);

}
