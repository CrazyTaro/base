package com.taro.base.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import static android.R.attr.value;

/**
 * Created by taro on 16/11/8.
 */
public class EncryptUtil {
    /**
     * MD5加密
     *
     * @param content
     * @return
     */
    public static final byte[] encryptInMD5(String content) {
        if (content == null || content.length() <= 0) {
            return null;
        }
        try {
            //指定MD5算法
            MessageDigest digest = MessageDigest.getInstance("MD5");
            digest.update(content.getBytes());
            return digest.digest();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * SHA-1加密
     *
     * @param content
     * @return
     */
    public static final byte[] encryptInSha1(String content) {
        if (content == null || content.length() <= 0) {
            return null;
        }
        try {
            //指定sha1算法
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            digest.update(content.getBytes());
            //获取字节数组
            return digest.digest();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }


    /**
     * HMACSHA-1加密
     *
     * @param value
     * @param key
     * @return
     */
    public static byte[] encryptInHMACSha1(String value, String key) {
        try {
            // Get an hmac_sha1 key from the raw key bytes
            byte[] keyBytes = key.getBytes();
            SecretKeySpec signingKey = new SecretKeySpec(keyBytes, "HmacSHA1");

            // Get an hmac_sha1 Mac instance and initialize with the signing key
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(signingKey);

            // Compute the hmac on input data bytes
            byte[] rawHmac = mac.doFinal(value.getBytes());

            return rawHmac;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    /**
     * byte转16进制字符串
     *
     * @param b
     * @return
     */
    public static String byte2hex(final byte[] b) {
        // Create Hex String
        StringBuffer hexString = new StringBuffer();
        // 字节数组转换为 十六进制 数
        for (int i = 0; i < b.length; i++) {
            String shaHex = Integer.toHexString(b[i] & 0xFF);
            if (shaHex.length() < 2) {
                hexString.append(0);
            }
            hexString.append(shaHex);
        }
        return hexString.toString();
    }
}
