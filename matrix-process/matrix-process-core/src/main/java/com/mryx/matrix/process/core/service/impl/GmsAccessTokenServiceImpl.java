package com.mryx.matrix.process.core.service.impl;

import com.mryx.matrix.process.core.utils.AesCiperUtil;
import com.mryx.matrix.process.core.service.GmsAccessTokenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Created by supeng on 2017/5/24.
 */
@Service("gmsAccessTokenService")
public class GmsAccessTokenServiceImpl implements GmsAccessTokenService {

    private static final Logger logger = LoggerFactory.getLogger(GmsAccessTokenServiceImpl.class);



    /**
     * 生成AccessToken
     *
     * @param userId
     * @param tokenType 类型 grampus
     * @return 格式：userId:时间戳毫秒:tokenType
     * example: 121:14720932121:grampus
     */
    @Override
    public String generateAccessToken(long userId, String tokenType) {
        if (userId > 0L) {
            long rightNow = System.currentTimeMillis();
            StringBuilder sb = new StringBuilder();
            sb.append(userId).append(":").append(rightNow).append(":").append(tokenType);
            String accessToken = AesCiperUtil.aesEncrypt(sb.toString());
            return accessToken;
        } else {
            return "";
        }
    }

    @Override
    public Map<String, Object> verifyAccessTokenByUserId(String accessToken) {
        Map<String, Object> returnResult = new HashMap<String, Object>();
        try {
            logger.info("accessToken={}", accessToken);
            accessToken = URLDecoder.decode(accessToken, "UTF-8");
            //logger.info("verifyAccessTokenByUserId decode accessToken = {}", accessToken);
            long userId = 0L;
            String newAccessToken = "";
            String accessTokenInfo = AesCiperUtil.aesDecrypt(accessToken);//12:1502175740256:grampus
            logger.info("accessTokenInfo = {}", accessTokenInfo);
            if (!Objects.equals(null, accessToken) && !Objects.equals("", accessToken)) {
                String[] infos = accessTokenInfo.split(":");
                if (infos.length >= 3) {
                    userId = Long.parseLong(infos[0]);
                    newAccessToken = accessToken;
                }
            }
            if (userId > 0 && verifyIsUserMember(userId)) {
                returnResult.put("userId", Long.valueOf(userId));
            } else {
                returnResult.put("userId", 0);
            }
            returnResult.put("accessToken", newAccessToken);
            return returnResult;
        } catch (Exception e) {
            logger.error("verifyAccessTokenByUserId error:", e);
            returnResult.put("userId", 0);
            returnResult.put("accessToken", accessToken);
            return returnResult;
        }
    }
    private Boolean verifyIsUserMember(long userId) throws Exception {
        Boolean flag = true;
        return flag;
    }

}
