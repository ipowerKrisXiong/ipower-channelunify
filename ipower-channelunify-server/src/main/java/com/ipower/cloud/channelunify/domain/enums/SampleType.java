package com.ipower.cloud.channelunify.domain.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 示例枚举
 */
public enum SampleType {

    type1("TYPE_1", "类型1"),;

    //该注解用于mybatisplus数据库插入时指定值
    @EnumValue
    private final String code;
    private final String name;

    SampleType(String code, String name) {
        this.code = code;
        this.name = name;
    }

    @JsonValue
    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    @JsonCreator
    public static SampleType valueOfCode(String code) {
        SampleType[] orderTypes = values();
        for (SampleType orderType: orderTypes) {
            if (orderType.getCode().equals(code)) {
                return orderType;
            }
        }
        return null;
    }
}
