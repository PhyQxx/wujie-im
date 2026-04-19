package com.wujie.im.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * AES-256-CBC 加密工具（简化版，无 IV）
 */
@Slf4j
@Component
public class AesUtil {

    // 固定密钥
    private static final String SECRET_KEY = "wujie-im-aes-256-key-2024-change-in-production";

    /**
     * 加密：Base64(AES-CBC(json))
     */
    public String encrypt(String plainText) {
        try {
            byte[] keyBytes = SECRET_KEY.substring(0, 32).getBytes(StandardCharsets.UTF_8);
            SecretKeySpec key = new SecretKeySpec(keyBytes, "AES");

            // 使用固定 IV（简化版，生产环境应随机生成）
            byte[] iv = "1234567890123456".getBytes(StandardCharsets.UTF_8);
            IvParameterSpec ivSpec = new IvParameterSpec(iv);

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, key, ivSpec);

            byte[] cipherText = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(cipherText);
        } catch (Exception e) {
            log.error("AES加密失败", e);
            return plainText;
        }
    }

    /**
     * 解密
     */
    public String decrypt(String encryptedText) {
        try {
            byte[] keyBytes = SECRET_KEY.substring(0, 32).getBytes(StandardCharsets.UTF_8);
            SecretKeySpec key = new SecretKeySpec(keyBytes, "AES");

            byte[] iv = "1234567890123456".getBytes(StandardCharsets.UTF_8);
            IvParameterSpec ivSpec = new IvParameterSpec(iv);

            byte[] cipherText = Base64.getDecoder().decode(encryptedText);

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, key, ivSpec);

            byte[] plainText = cipher.doFinal(cipherText);
            return new String(plainText, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("AES解密失败: {}", e.getMessage());
            return null;
        }
    }
}
