package com.ipower.cloud.channelunify.domain.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 渠道类型
 */
public enum ChannelType {

    AMAZON("AMAZON", "亚马逊"),;

    //该注解用于mybatisplus数据库插入时指定值
    @EnumValue
    private final String code;
    private final String name;

    ChannelType(String code, String name) {
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
    public static ChannelType valueOfCode(String code) {
        ChannelType[] orderTypes = values();
        for (ChannelType orderType: orderTypes) {
            if (orderType.getCode().equals(code)) {
                return orderType;
            }
        }
        return null;
    }
}
