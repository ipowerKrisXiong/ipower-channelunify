package com.ipower.cloud.channelunify.infra.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * Created with IntelliJ IDEA.
 * MetaObjectHandler提供的默认方法的策略均为:如果属性有值则不覆盖,如果填充值为null则不填充
 * 实现自己的自定义填充策略
 */
@Component
@Slf4j
public class CommonMetaObjectHandler implements MetaObjectHandler {


    @Override
    public void insertFill(MetaObject metaObject) {
        log.debug("start insert fill ....");
        LocalDateTime now = LocalDateTime.now();
        if (metaObject.hasSetter("createTime"))
            this.setFieldValByName("createTime", now, metaObject);
        if (metaObject.hasSetter("createTimestamp"))
            this.setFieldValByName("createTimestamp", now.toInstant(ZoneOffset.UTC).toEpochMilli(), metaObject);
        if (metaObject.hasSetter("updateTime"))
            this.setFieldValByName("updateTime", now, metaObject);
        if (metaObject.hasSetter("updateTimestamp"))
            this.setFieldValByName("updateTimestamp", now.toInstant(ZoneOffset.UTC).toEpochMilli(), metaObject);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        log.debug("start update fill ....");
        LocalDateTime now = LocalDateTime.now();
        if (metaObject.hasSetter("updateTime"))
            this.setFieldValByName("updateTime", now, metaObject);
        if (metaObject.hasSetter("updateTimestamp"))
            this.setFieldValByName("updateTimestamp", now.toInstant(ZoneOffset.UTC).toEpochMilli(), metaObject);
    }
}
