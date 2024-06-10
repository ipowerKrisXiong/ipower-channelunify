package com.ipower.cloud.channelunify.infra.util.sensitive;


import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 使用例子：
 * @SensitiveWrapped(value = SensitiveEnum.USER_NAME,noSensitivePerm="user:admin")
 * String name;
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@JacksonAnnotationsInside
@JsonSerialize(using = SensitiveSerializer.class)
public @interface SensitiveWrapped {

    /**
     * 脱敏类型
     * @return
     */
    SensitiveEnum value();

    /**
     * 全展示时候用户需要带上得权限
     * @return
     */
    String noSensitivePerm() default "";

}
