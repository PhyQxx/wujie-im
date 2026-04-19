package com.wujie.im.common;

import java.lang.annotation.*;

/**
 * 标记接口启用 AES-256-GCM 加密
 * 使用方法：@Encrypt 放在 Controller 方法上
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Encrypt {
}
