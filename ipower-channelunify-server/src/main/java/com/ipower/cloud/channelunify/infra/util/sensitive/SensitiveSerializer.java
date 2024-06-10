package com.ipower.cloud.channelunify.infra.util.sensitive;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import com.mars.auth.core.util.LoginUserUtils;
import com.mars.auth.type.LoginUserPermsInfo;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.Objects;

/**
 * ContextualSerializer接口只能加在JsonSerializer的实现类里面实现，jackson框架才能探测到
 */
public class SensitiveSerializer extends JsonSerializer<String> implements ContextualSerializer {

    /**
     * 脱敏类型
     */
    private SensitiveEnum type;

    private String noSensitivePerm;

    @Override
    public void serialize(String s, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {

        //判断当前用户是否有查看明文的权限
        if(StringUtils.isNotBlank(noSensitivePerm)){
            LoginUserPermsInfo loginUserPermsInfo = LoginUserUtils.getUser().getLoginUserPermsInfo();
            if(loginUserPermsInfo!=null && loginUserPermsInfo.getStringPermissions()!=null && loginUserPermsInfo.getStringPermissions().size()>0){
                if(loginUserPermsInfo.getStringPermissions().contains(noSensitivePerm)){
                    //名文返回
                    jsonGenerator.writeString(s);
                    return;
                }
            }
        }

        //上面有权限则返回明文，没有权限则返回加密文
        switch (this.type) {
            case SET_NULL: {
                jsonGenerator.writeString(SensitiveInfoUtils.setNull(s));
                break;
            }
            case USER_NAME: {
                jsonGenerator.writeString(SensitiveInfoUtils.chineseName(s));
                break;
            }
            case ID_CARD: {
                jsonGenerator.writeString(SensitiveInfoUtils.idCardNum(s));
                break;
            }
            case FIXED_PHONE: {
                jsonGenerator.writeString(SensitiveInfoUtils.fixedPhone(s));
                break;
            }
            case MOBILE_PHONE: {
                jsonGenerator.writeString(SensitiveInfoUtils.mobilePhone(s));
                break;
            }
            case ADDRESS: {
                jsonGenerator.writeString(SensitiveInfoUtils.address(s, 4));
                break;
            }
            case EMAIL: {
                jsonGenerator.writeString(SensitiveInfoUtils.email(s));
                break;
            }
            case BANK_CARD: {
                jsonGenerator.writeString(SensitiveInfoUtils.bankCard(s));
                break;
            }
            case CNAPS_CODE: {
                jsonGenerator.writeString(SensitiveInfoUtils.cnapsCode(s));
                break;
            }
        }


    }

    /**
     * 这个方法在objectMapper对class进行序列化的时候会调用。class的每个filed都会调用看使用哪种序列化器，这个方法就是在这个时候来临时变化序列化器用的
     * 一旦一个class生成过一次，下次再序列化就会取序列化器的缓存，不会再进这个方法了。
     * @param serializerProvider Serializer provider to use for accessing config, other serializers
     * @param beanProperty Method or field that represents the property
     *   (and is used to access value to serialize).
     *   Should be available; but there may be cases where caller cannot provide it and
     *   null is passed instead (in which case impls usually pass 'this' serializer as is)
     *
     * @return
     * @throws JsonMappingException
     */
    @Override
    public JsonSerializer<?> createContextual(SerializerProvider serializerProvider, BeanProperty beanProperty) throws JsonMappingException {
        // 为空直接跳过
        if (beanProperty != null) {
            // 非 String 类直接跳过
            if (Objects.equals(beanProperty.getType().getRawClass(), String.class)) {
                SensitiveWrapped sensitiveWrapped = beanProperty.getAnnotation(SensitiveWrapped.class);
                //字段上没有注解
                if (sensitiveWrapped == null) {
                    sensitiveWrapped = beanProperty.getContextAnnotation(SensitiveWrapped.class);
                }
                if (sensitiveWrapped != null) {
                    // 如果能得到注解，且没有权限，就将注解的 value 传入 SensitiveSerialize，进行脱敏序列化处理
                    return new SensitiveSerializer(sensitiveWrapped.value(),sensitiveWrapped.noSensitivePerm());
                }
            }
            return serializerProvider.findValueSerializer(beanProperty.getType(), beanProperty);
        }
        return serializerProvider.findNullValueSerializer(beanProperty);
    }

    public SensitiveSerializer() {}

    public SensitiveSerializer(final SensitiveEnum type,final String noSensitivePerm) {
        this.type = type;
        this.noSensitivePerm = noSensitivePerm;
    }

}
