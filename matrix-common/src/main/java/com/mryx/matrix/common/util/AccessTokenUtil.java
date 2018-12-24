package com.mryx.matrix.common.util;

import java.net.URLDecoder;
import java.util.Objects;

/**
 * AccessToken Util
 *
 * @author supeng
 * @date 2018/09/25
 */
public class AccessTokenUtil {
    /**
     * 生成 AccessToken
     *
     * @return
     */
    public static String generateAccessToken(long userId, String tokenType) {
        if (userId > 0L) {
            long rightNow = System.currentTimeMillis();
            StringBuilder sb = new StringBuilder();
            sb.append(userId).append(":").append(rightNow).append(":").append(tokenType);
            String accessToken = AesCiperUtil.aesEncrypt(sb.toString());
            return accessToken;
        } else {
            return null;
        }
    }

    /**
     * 校验 AccessToken
     *
     * @return
     */
    public static boolean verifyAccessToken(String token) {
        if (Objects.equals(token, null) || Objects.equals(token, "")) {
            return false;
        }
        try {
            token = URLDecoder.decode(token, "UTF-8");
            long userId = 0L;
            String accessTokenInfo = AesCiperUtil.aesDecrypt(token);
            String[] infos = accessTokenInfo.split(":");
            if (infos.length >= 3) {
                userId = Long.parseLong(infos[0]);
            }
            if (userId > 0) {
                return true;
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
