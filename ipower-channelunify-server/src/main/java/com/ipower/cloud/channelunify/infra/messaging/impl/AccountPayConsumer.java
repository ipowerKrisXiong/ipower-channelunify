package com.ipower.cloud.channelunify.infra.messaging.impl;

import com.club.clubmanager.application.service.TradePayService;
import com.club.clubmanager.domain.entity.PayTypeInfo;
import com.club.clubmanager.domain.enums.PayTypeEnum;
import com.club.clubmanager.domain.messaging.constant.PayMqConstant;
import com.club.clubmanager.domain.messaging.dto.AccountPaySucceedMsg;
import com.mars.rocketmq.process.MarsSimpleRocketConsumer;
import com.mars.rocketmq.process.RocketMsgWrap;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class AccountPayConsumer extends MarsSimpleRocketConsumer<AccountPaySucceedMsg> {
    private final TradePayService tradePayService;

    public AccountPayConsumer(TradePayService tradePayService) {
        this.tradePayService = tradePayService;
    }

    @Override
    public void process(RocketMsgWrap<AccountPaySucceedMsg> rocketMsgWrap) {
        AccountPaySucceedMsg msg = rocketMsgWrap.getMsgBody();
        if (Objects.equals(rocketMsgWrap.getTag(), PayMqConstant.TAG_ACCOUNT_PAY_SUCCEED_CASH)) {
            PayTypeInfo payTypeInfo = PayTypeInfo.builder().payType(PayTypeEnum.CASH).build();
            tradePayService.paySuccess(msg.getOutTradeNo(), msg.getPayAmount(), msg.getPayGiftAmount(), payTypeInfo,
                    msg.getTransactionId(), msg.getTransactionTime(), msg.getComId());
        } else if (Objects.equals(rocketMsgWrap.getTag(), PayMqConstant.TAG_ACCOUNT_PAY_SUCCEED_WALLET_CARD)) {
            PayTypeInfo payTypeInfo = PayTypeInfo.builder().payType(PayTypeEnum.WALLET_CARD).build();
            tradePayService.paySuccess(msg.getOutTradeNo(), msg.getPayAmount(), msg.getPayGiftAmount(), payTypeInfo,
                    msg.getTransactionId(), msg.getTransactionTime(), msg.getComId());
        } else if (Objects.equals(rocketMsgWrap.getTag(), PayMqConstant.TAG_ACCOUNT_PAY_SUCCEED_STORED_VALUE_CARD)) {
            PayTypeInfo payTypeInfo = PayTypeInfo.builder().payType(PayTypeEnum.STORED_VALUE_CARD).build();
            tradePayService.paySuccess(msg.getOutTradeNo(), msg.getPayAmount(), msg.getPayGiftAmount(), payTypeInfo,
                    msg.getTransactionId(), msg.getTransactionTime(), msg.getComId());
        }
    }

    @Override
    public Class<AccountPaySucceedMsg> bodyClass() {
        return AccountPaySucceedMsg.class;
    }

    @Override
    public String getTopic() {
        return PayMqConstant.TOPIC_ACCOUNT_PAY_SUCCEED;
    }

    @Override
    public String getConsumerGroupName() {
        return PayMqConstant.CONSUMER_ACCOUNT_PAY_SUCCEED;
    }
}
