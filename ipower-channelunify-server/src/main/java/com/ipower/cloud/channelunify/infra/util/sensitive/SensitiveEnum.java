package com.ipower.cloud.channelunify.infra.util.sensitive;

/**
 * 脱敏数据类型枚举
 */
public enum SensitiveEnum {

    /**
     * 设置为空
     */
    SET_NULL,

    /**
     * 人名
     */
    USER_NAME,

    /**
     * 身份证号
     */
    ID_CARD,

    /**
     * 座机号
     */
    FIXED_PHONE,

    /**
     * 手机号
     */
    MOBILE_PHONE,

    /**
     * 地址
     */
    ADDRESS,

    /**
     * 电子邮件
     */
    EMAIL,

    /**
     * 银行卡
     */
    BANK_CARD,

    /**
     * 公司开户银行联号
     */
    CNAPS_CODE,
    ;

}
