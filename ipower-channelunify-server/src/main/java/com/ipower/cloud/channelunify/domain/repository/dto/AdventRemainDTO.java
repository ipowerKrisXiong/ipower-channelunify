package com.ipower.cloud.channelunify.domain.repository.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author: hechenggang
 * @date: 2023/9/27 17:51
 * @description:
 */
@Data
public class AdventRemainDTO {

    private LocalDateTime expireTime;

    private String memberPhone ;

}
