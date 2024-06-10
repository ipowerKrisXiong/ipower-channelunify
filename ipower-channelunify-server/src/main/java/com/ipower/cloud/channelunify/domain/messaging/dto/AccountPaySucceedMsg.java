package com.ipower.cloud.channelunify.domain.messaging.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountPaySucceedMsg {

    /**
     * 支付ID（支付业务方的ID）
     */
    private String outTradeNo;
    /**
     * 支付本金金额
     */
    private Long payAmount;
    /**
     * 支付赠送
     */
    private Long payGiftAmount;
    /**
     * 支付时间
     */
    private LocalDateTime transactionTime;
    /**
     * 支付ID （余额支付单的ID）
     */
    private String transactionId;
    /**
     * 公司ID
     */
    private Long comId;
}
