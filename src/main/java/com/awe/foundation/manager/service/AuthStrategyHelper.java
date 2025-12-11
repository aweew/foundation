package com.awe.foundation.manager.service;

import com.awe.foundation.common.constant.enums.GrantTypeEnum;
import com.awe.foundation.manager.service.impl.PasswordAuthStrategy;

/**
 * @author Awe
 * @since 2025/12/11 13:17
 */
public class AuthStrategyHelper {

    public static IAuthStrategy getAuthStrategy(GrantTypeEnum grantType) {
        switch (grantType) {
            case PASSWORD:
                return new PasswordAuthStrategy();
            default:
                throw new IllegalArgumentException("Unsupported auth type: " + grantType);
        }
    }

}
