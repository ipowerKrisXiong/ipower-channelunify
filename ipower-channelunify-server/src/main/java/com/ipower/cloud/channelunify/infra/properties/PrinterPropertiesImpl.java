package com.ipower.cloud.channelunify.infra.properties;

import com.club.clubmanager.domain.properties.PrinterProperties;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author Ryan Wang
 * @version 1.0
 * @description 芯烨云打印机相关参数
 * @date 2022-08-17 17:14:31
 */
@Component
@ConfigurationProperties(prefix = "xpyun.net")
@Data
public class PrinterPropertiesImpl implements PrinterProperties {

    /**
     * 用户名
     */
    private String username;

    /**
     * 用户秘钥
     */
    private String userKey;

}
