package com.ipower.cloud.channelunify.infra.properties;

import com.club.clubmanager.domain.properties.WxThirdPlatProperties;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author xl
 * @date 2022/11/1
 */
@Component
@ConfigurationProperties(prefix = "wxthirdplat.conf")
@Data
public class WxThirdPlatPropertiesImpl implements WxThirdPlatProperties {

    private String appId;

    private String appSecret;

    private String encodingAesKey;

    private String verifyToken;

    private String authSuccessRedirectUrl;

    private String platContactPhone;

}
