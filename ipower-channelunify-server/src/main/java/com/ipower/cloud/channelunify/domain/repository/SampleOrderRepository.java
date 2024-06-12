package com.ipower.cloud.channelunify.domain.repository;

import com.ipower.cloud.channelunify.domain.entity.SampleOrder;
import org.springframework.data.mongodb.repository.MongoRepository;


/**
 * 示例订单仓储
 */
public interface SampleOrderRepository extends MongoRepository<SampleOrder,String> {


}
