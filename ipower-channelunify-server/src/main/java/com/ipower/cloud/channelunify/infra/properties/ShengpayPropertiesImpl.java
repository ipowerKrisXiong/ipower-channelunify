package com.ipower.cloud.channelunify.infra.properties;

import com.club.clubmanager.domain.properties.ShengpayProperties;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author: hechenggang
 * @date: 2023/1/29 10:02
 * @description:
 */
@Component
@ConfigurationProperties(prefix = "ac.pay.shengpay")
@Data
public class ShengpayPropertiesImpl implements ShengpayProperties {

    String shengpayPublicKeyPath;

    String privateKeyPath;
    String mchId;
    String subMchId;
    String payNotifyUrl;
    String refundNotifyUrl;

}
