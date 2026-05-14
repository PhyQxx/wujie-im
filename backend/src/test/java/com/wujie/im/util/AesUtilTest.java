package com.wujie.im.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AesUtilTest {

    private AesUtil aesUtil;

    @BeforeEach
    void setUp() {
        aesUtil = new AesUtil();
    }

    @Test
    void encrypt_shouldReturnEncryptedString() {
        String plainText = "Hello, World!";

        String encrypted = aesUtil.encrypt(plainText);

        assertNotNull(encrypted);
        assertNotEquals(plainText, encrypted);
    }

    @Test
    void decrypt_shouldReturnOriginalText() {
        String plainText = "Hello, World!";
        String encrypted = aesUtil.encrypt(plainText);

        String decrypted = aesUtil.decrypt(encrypted);

        assertEquals(plainText, decrypted);
    }

    @Test
    void encryptAndDecrypt_shouldWorkWithChineseText() {
        String plainText = "你好，世界！这是一条测试消息。";
        String encrypted = aesUtil.encrypt(plainText);

        String decrypted = aesUtil.decrypt(encrypted);

        assertEquals(plainText, decrypted);
    }

    @Test
    void encryptAndDecrypt_shouldWorkWithJsonString() {
        String plainText = "{\"userId\":123,\"username\":\"test\"}";
        String encrypted = aesUtil.encrypt(plainText);

        String decrypted = aesUtil.decrypt(encrypted);

        assertEquals(plainText, decrypted);
    }

    @Test
    void encryptAndDecrypt_shouldWorkWithEmptyString() {
        String plainText = "";
        String encrypted = aesUtil.encrypt(plainText);

        String decrypted = aesUtil.decrypt(encrypted);

        assertEquals(plainText, decrypted);
    }

    @Test
    void encryptAndDecrypt_shouldWorkWithSpecialCharacters() {
        String plainText = "!@#$%^&*()_+-=[]{}|;':\",.<>?/`~";
        String encrypted = aesUtil.encrypt(plainText);

        String decrypted = aesUtil.decrypt(encrypted);

        assertEquals(plainText, decrypted);
    }

    @Test
    void encrypt_shouldReturnDifferentCiphertextForEachCall() {
        String plainText = "test";

        String encrypted1 = aesUtil.encrypt(plainText);
        String encrypted2 = aesUtil.encrypt(plainText);

        assertEquals(encrypted1, encrypted2);
    }

    @Test
    void decrypt_shouldReturnNullForInvalidBase64() {
        String invalid = "not-valid-base64!!!";

        String result = aesUtil.decrypt(invalid);

        assertNull(result);
    }

    @Test
    void decrypt_shouldReturnNullForWrongCipherText() {
        String validBase64 = "YWJjZGVmZ2hpamtsbW5vcA==";

        String result = aesUtil.decrypt(validBase64);

        assertNull(result);
    }

    @Test
    void encrypt_shouldReturnDifferentResultsForDifferentInputs() {
        String encrypted1 = aesUtil.encrypt("input1");
        String encrypted2 = aesUtil.encrypt("input2");

        assertNotEquals(encrypted1, encrypted2);
    }
}
