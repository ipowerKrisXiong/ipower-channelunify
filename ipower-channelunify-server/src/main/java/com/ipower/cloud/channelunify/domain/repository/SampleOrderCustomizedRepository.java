package com.ipower.cloud.channelunify.domain.repository;

import com.ipower.cloud.channelunify.domain.entity.SampleOrder;
import com.ipower.cloud.channelunify.domain.repository.dto.SampleOrderStatisticDTO;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 示例订单仓储
 */
public interface SampleOrderCustomizedRepository {

    /**
     * 使用游标读取，并查询指定字段值，减少网络消耗
     */
    public List<SampleOrder> findByTimeRange(LocalDateTime startTime, LocalDateTime endTime);

    public SampleOrderStatisticDTO statisticByTimeRange(LocalDateTime startTime, LocalDateTime endTime);

}
