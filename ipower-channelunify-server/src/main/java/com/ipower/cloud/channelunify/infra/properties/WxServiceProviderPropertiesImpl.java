package com.ipower.cloud.channelunify.infra.properties;

import com.club.clubmanager.domain.properties.WxServiceProviderProperties;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @description 微信服务商配置
 */
@Component
@ConfigurationProperties(prefix = "wxserviceprovider.conf")
@Data
public class WxServiceProviderPropertiesImpl implements WxServiceProviderProperties {

    /**
     * 微信服务商appId
     * @return
     */
    private String appId;

    /**
     * 微信服务商app密钥
     * @return
     */
    private String appSecret;


    /**
     * 消息校验Token
     * @return
     */
    private String token;


    /**
     * 消息加密密钥
     * @return
     */
    private String encodingAesKey;


}
