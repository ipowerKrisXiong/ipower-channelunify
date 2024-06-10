package com.ipower.cloud.channelunify.domain.messaging;

import com.club.clubmanager.domain.messaging.dto.ConsumePointRecordMsg;

/**
 * @author: hechenggang
 * @date: 2022/8/29 19:01
 * @description:
 */
public interface ConsumePointRecordProducer {

        void  sendMqRecordConsumePoint(ConsumePointRecordMsg consumePointRecordMsg);

}
