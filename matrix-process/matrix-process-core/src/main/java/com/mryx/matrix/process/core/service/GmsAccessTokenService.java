package com.mryx.matrix.process.core.service;

import java.util.Map;

/**
 * Created by supeng on 2017/5/24.
 */
public interface GmsAccessTokenService {
    String generateAccessToken(long userId, String tokenType);
    Map<String, Object> verifyAccessTokenByUserId(String accessToken);
    }
