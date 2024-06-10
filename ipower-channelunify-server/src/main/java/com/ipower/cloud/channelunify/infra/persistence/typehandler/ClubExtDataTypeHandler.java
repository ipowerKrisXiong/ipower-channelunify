package com.ipower.cloud.channelunify.infra.persistence.typehandler;

import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.club.clubmanager.domain.types.ClubExtData;
import com.mars.commonutils.core.utils.JsonUtil;

import java.io.IOException;

/**
 * 自定义复杂类型处理器<br/>
 * 不要问我为什么要重写 parse 因为顶层父类是无法获取到准确的待转换复杂返回类型数据
 * extends BaseTypeHandler<T>
 *
 *     JacksonTypeHandler
 *
 */
public class ClubExtDataTypeHandler extends JacksonTypeHandler {

    public ClubExtDataTypeHandler(Class<?> type) {
        super(type);
    }

    @Override
    protected Object parse(String json) {
        try {
            if(json==null){
                return null;
            }else{
                return JsonUtil.getObjectMapper().readValue(json, ClubExtData.class);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected String toJson(Object obj) {
        return JsonUtil.toJSONString(obj);
    }



}
