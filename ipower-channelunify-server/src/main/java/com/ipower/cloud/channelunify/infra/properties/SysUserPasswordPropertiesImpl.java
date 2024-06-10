package com.ipower.cloud.channelunify.infra.properties;

import com.club.clubmanager.domain.properties.SysUserPasswordProperties;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author 郑兴泉 956607644@qq.com
 * @data 2021/12/10
 * 描述：
 */
@Component
@ConfigurationProperties(prefix = "st.user.password")
@Data
public class SysUserPasswordPropertiesImpl implements SysUserPasswordProperties {

    private String defaultInit = "12345678";
    private boolean enable = true;


    @Override
    public boolean enablePassword() {
        return enable;
    }

    @Override
    public String getDefaultPassword() {
        return defaultInit;
    }
}
