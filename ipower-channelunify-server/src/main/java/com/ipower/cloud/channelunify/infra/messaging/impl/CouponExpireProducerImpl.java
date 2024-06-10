package com.ipower.cloud.channelunify.infra.messaging.impl;

import com.club.clubmanager.domain.messaging.CouponExpireProducer;
import com.club.clubmanager.domain.messaging.constant.CouponMqConstants;
import com.club.clubmanager.domain.messaging.dto.CouponMemberRecordLiveExpireMsg;
import com.club.clubmanager.domain.messaging.dto.CouponMemberRecordNotifyExpireMsg;
import com.club.clubmanager.domain.messaging.dto.CouponPayOrderExpireMsg;
import com.club.clubmanager.domain.messaging.dto.CouponProductSaleEndDisableMsg;
import com.mars.rocketmq.process.MarsMessage;
import com.mars.rocketmq.process.MarsRocketMqSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CouponExpireProducerImpl implements CouponExpireProducer {

    @Autowired
    private MarsRocketMqSender marsRocketMqSender;

    @Override
    public void sendCouponRecordExpireNotifyDelay(CouponMemberRecordNotifyExpireMsg msg) {
        MarsMessage marsMessage = MarsMessage.buildMsg(CouponMqConstants.TOPIC_COUPON_EXPIRE,
                CouponMqConstants.TAG_COUPON_MEMBER_RECORD_NOTIFY_EXPIRE,
                msg.getCouponMemberRecordId().toString()
                ,msg,msg.getComId().toString());
        marsRocketMqSender.sendDelayAsyncLocalTX(marsMessage,msg.getExpireTime());
    }

    @Override
    public void sendCouponRecordExpireLiveDelay(CouponMemberRecordLiveExpireMsg msg) {
        MarsMessage marsMessage = MarsMessage.buildMsg(CouponMqConstants.TOPIC_COUPON_EXPIRE,
                CouponMqConstants.TAG_COUPON_MEMBER_RECORD_LIVE_EXPIRE,
                msg.getCouponMemberRecordId().toString()
                ,msg,msg.getComId().toString());
        marsRocketMqSender.sendDelayAsyncLocalTX(marsMessage,msg.getExpireTime());
    }


    @Override
    public void sendCouponExpirePayOrderDelay(CouponPayOrderExpireMsg msg) {
        MarsMessage marsMessage = MarsMessage.buildMsg(CouponMqConstants.TOPIC_COUPON_EXPIRE,
                CouponMqConstants.TAG_COUPON_MALL_ORDER_PAY_EXPIRE,
                msg.getOrderId().toString(),
                msg,
                msg.getComId().toString());
        marsRocketMqSender.sendDelayAsyncLocalTX(marsMessage, msg.getExpireTime());
    }

    @Override
    public void sendCouponProductSaleEndDisableDelay(CouponProductSaleEndDisableMsg msg) {
        MarsMessage marsMessage = MarsMessage.buildMsg(CouponMqConstants.TOPIC_COUPON_EXPIRE,
                CouponMqConstants.TAG_COUPON_PRODUCT_SALE_END_DISABLE,
                msg.getProductId().toString(),
                msg,
                msg.getComId().toString());
        marsRocketMqSender.sendDelayAsyncLocalTX(marsMessage, msg.getSaleEndTime());
    }
}
