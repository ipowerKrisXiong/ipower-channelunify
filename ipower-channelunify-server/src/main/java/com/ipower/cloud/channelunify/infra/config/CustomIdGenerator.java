package com.ipower.cloud.channelunify.infra.config;

import com.baomidou.mybatisplus.core.incrementer.IdentifierGenerator;
import com.club.clubmanager.domain.entity.BaseEntity;
import com.mars.commonutils.core.utils.SnowflakeIdWorker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 自定义ID生成器
 * 仅作为示范
 */
@Slf4j
@Component
public class CustomIdGenerator implements IdentifierGenerator {

    //有ID则用已有ID，没有ID则重新配置ID
    @Override
    public Long nextId(Object entity) {
        //可以将当前传入的class全类名来作为bizKey,或者提取参数来生成bizKey进行分布式Id调用生成.

        Long id = null;
        if (entity instanceof BaseEntity) {
            Long hasId = ((BaseEntity) entity).getId();
            if (hasId != null) {
                id = hasId;
            }
        }

        if (id == null) {
            String bizKey = entity.getClass().getName();
            final long nextId = SnowflakeIdWorker.getInstance().nextId();
            log.debug("为{}生成主键值->:{}", bizKey, id);
            return nextId;
        } else {
            return id;
        }
    }

}
