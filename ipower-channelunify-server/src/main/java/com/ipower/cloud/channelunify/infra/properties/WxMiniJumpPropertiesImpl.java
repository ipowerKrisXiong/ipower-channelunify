package com.ipower.cloud.channelunify.infra.properties;

import com.club.clubmanager.domain.properties.WxMiniJumpProperties;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author xl
 * @date 2022/11/1
 */
@Component
@ConfigurationProperties(prefix = "ac.wxmini.jumpdomain")
@Data
public class WxMiniJumpPropertiesImpl implements WxMiniJumpProperties {

    private String drinkorder;

}
