package com.ipower.cloud.channelunify.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * channelunify 订单
 * @author xl
 * @date 2024/6/11
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChannelUnifyOrder {
    /**
     * 订单id
     */
    private Long id;
    /**
     * 订单编号
     */
    private String orderNo;

}
