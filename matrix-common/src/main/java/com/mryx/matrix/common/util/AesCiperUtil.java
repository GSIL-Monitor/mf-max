package com.mryx.matrix.common.util;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;

/**
 * Created by supeng on 2017/5/24.
 */
public class AesCiperUtil {

    private static final String KEY_AES = "AES";
    private static final String ALGORITHM_AES = "AES/CBC/PKCS5Padding";
    private static final String IV_PARAM_AES = "MATRIX*&^PUB!#45";
    private static final String AESKEY_SEED = "AEDIEWWOD(#$)**#$EDIDNKA*&)##)(";
    private static final String SECURE_RANDOM = "SHA1PRNG";
    private static AlgorithmParameterSpec paramSpec = new IvParameterSpec(IV_PARAM_AES.getBytes());
    private static ThreadLocal<Cipher> cipherThreadLocal = new ThreadLocal<>();
    private static Key k;

    public AesCiperUtil() {
    }

    static {
        try {
            k = toKey(generateAESKey(128, AESKEY_SEED), KEY_AES);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String generateAESKey(int keySize, String seed) {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance(KEY_AES);
            SecureRandom random = SecureRandom.getInstance(SECURE_RANDOM);
            random.setSeed(seed.getBytes());
            keyGenerator.init(keySize, random);
            SecretKey key = keyGenerator.generateKey();
            return TranscodeUtil.byteArrayToBase64Str(key.getEncoded());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * AES 加密
     *
     * @param data
     * @return
     */
    public static String aesEncrypt(String data) {
        return Base64.encode(aesCipher(data, 1).getBytes());
    }

    /**
     * AES 解密
     *
     * @param data
     * @return
     */
    public static String aesDecrypt(String data) {
        return aesCipher(new String(Base64.decode(data)), 2);
    }

    private static String aesCipher(String data, int mode) {
        try {
            Cipher cipher = cipherThreadLocal.get();
            if (cipher == null) {
                cipher = Cipher.getInstance(ALGORITHM_AES);
                cipherThreadLocal.set(cipher);
            }
            cipher.init(mode, k, paramSpec);
            return mode == 2 ? new String(cipher.doFinal(TranscodeUtil.base64StrToByteArray(data))) : TranscodeUtil.byteArrayToBase64Str(cipher.doFinal(data.getBytes()));
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }finally {
            cipherThreadLocal.remove();
        }
    }

    private static Key toKey(String key, String algorithm) {
        SecretKeySpec secretKey = new SecretKeySpec(TranscodeUtil.base64StrToByteArray(key), algorithm);
        return secretKey;
    }

    //TODO 生成Access_token
    public static void main(String[] args) {
        long rightNow = System.currentTimeMillis();
        StringBuilder sb = new StringBuilder();
        sb.append("12").append(":").append(rightNow).append(":").append("grampus");
        String accessToken = AesCiperUtil.aesEncrypt(sb.toString());
        System.out.println(accessToken);

//        String aa="YTJrKzVyQ3pMOExmK1duTnBiSFkzekV5NUo1U1liRVJaRWFIN292UElKcz0=";
//        System.out.println(AesCiperUtil.aesDecrypt(aa));

//        String accessToken = AesCiperUtil.aesEncrypt("123125141231");
//        System.out.println(accessToken);

//        String aa="VHF0N0JYMlJWSHdkMUV5eDk2NFc1UT09";
//        System.out.println(AesCiperUtil.aesDecrypt(aa));

    }
}