package com.ipower.cloud.channelunify.infra.properties;

import com.club.clubmanager.domain.properties.SmsProperties;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "ac.sms")
@Data
public class SmsPropertiesImpl implements SmsProperties {

    public String customerWxMiniAppName;

}
