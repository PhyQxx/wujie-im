package com.wujie.im.config;

import com.wujie.im.util.AesUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;

/**
 * 配置自定义消息转换器
 */
@Configuration
public class CryptoWebMvcConfigurer {

    @Bean
    public CryptoJackson2HttpMessageConverter cryptoJackson2HttpMessageConverter(AesUtil aesUtil) {
        return new CryptoJackson2HttpMessageConverter(aesUtil);
    }
}
