package com.wujie.im.websocket;

import com.wujie.im.util.AesUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * WebSocket 消息加解密：只对 type=message 的 data 字段加密
 */
@Slf4j
@Service
public class WsCryptoService {

    @Autowired
    private AesUtil aesUtil;

    /**
     * 加密消息的 data 字段
     * @param data 要加密的对象（会被序列化为 JSON 再加密）
     * @return 加密后的字符串
     */
    public String encryptData(Object data) {
        try {
            // data 本身是 Map 或 Entity，转 JSON 再加密
            String json = cn.hutool.json.JSONUtil.toJsonStr(data);
            return aesUtil.encrypt(json);
        } catch (Exception e) {
            log.error("[WsCrypto] 加密 data 失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 解密消息的 data 字段
     * @param encryptedData 加密字符串
     * @return 解密后的 JSON 字符串（调用方自行反序列化）
     */
    public String decryptData(String encryptedData) {
        try {
            return aesUtil.decrypt(encryptedData);
        } catch (Exception e) {
            log.error("[WsCrypto] 解密 data 失败: {}", e.getMessage());
            return null;
        }
    }
}
