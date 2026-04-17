package com.wujie.im.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * AES-256-GCM 加密工具
 * 传输层加密：客户端使用相同算法和密钥加密消息内容后传输
 */
@Slf4j
@Component
public class AesUtil {

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_TAG_LENGTH = 128;
    private static final int GCM_IV_LENGTH = 12;

    @Value("${wujie.im.crypto.aes-key:default-key-change-in-production}")
    private String aesKey;

    /**
     * 加密
     * @param plainText 明文
     * @return Base64(IV + 密文 + TAG)
     */
    public String encrypt(String plainText) {
        try {
            byte[] keyBytes = getKeyBytes();
            SecretKey key = new SecretKeySpec(keyBytes, "AES");

            byte[] iv = new byte[GCM_IV_LENGTH];
            new SecureRandom().nextBytes(iv);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, key, gcmSpec);

            byte[] cipherText = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

            // 拼接 IV + 密文
            byte[] combined = new byte[iv.length + cipherText.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(cipherText, 0, combined, iv.length, cipherText.length);

            return Base64.getEncoder().encodeToString(combined);
        } catch (Exception e) {
            log.error("AES加密失败", e);
            return plainText; // 降级：返回原文
        }
    }

    /**
     * 解密
     * @param encryptedText Base64(IV + 密文 + TAG)
     * @return 明文
     */
    public String decrypt(String encryptedText) {
        try {
            byte[] combined = Base64.getDecoder().decode(encryptedText);
            byte[] keyBytes = getKeyBytes();
            SecretKey key = new SecretKeySpec(keyBytes, "AES");

            // 分离 IV 和密文
            byte[] iv = new byte[GCM_IV_LENGTH];
            byte[] cipherText = new byte[combined.length - GCM_IV_LENGTH];
            System.arraycopy(combined, 0, iv, 0, iv.length);
            System.arraycopy(combined, iv.length, cipherText, 0, cipherText.length);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, key, gcmSpec);

            byte[] plainText = cipher.doFinal(cipherText);
            return new String(plainText, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("AES解密失败", e);
            return encryptedText; // 降级：返回原文
        }
    }

    private byte[] getKeyBytes() {
        // 密钥必须是 16/24/32 字节（AES-128/192/256）
        byte[] key = new byte[32];
        byte[] src = aesKey.getBytes(StandardCharsets.UTF_8);
        System.arraycopy(src, 0, key, 0, Math.min(src.length, 32));
        return key;
    }
}
