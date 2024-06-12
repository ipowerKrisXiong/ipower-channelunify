package com.ipower.cloud.channelunify.infra.repository.impl;

import com.ipower.cloud.channelunify.domain.entity.SampleOrder;
import com.ipower.cloud.channelunify.domain.repository.SampleOrderCustomizedRepository;
import com.ipower.cloud.channelunify.domain.repository.SampleOrderRepository;
import com.ipower.cloud.channelunify.domain.repository.dto.SampleOrderStatisticDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 自定义方法示例
 */
@Repository
public class SampleOrderCustomizedRepositoryImpl implements SampleOrderCustomizedRepository {

    @Autowired
    private MongoTemplate mongoTemplate;


    @Override
    public List<SampleOrder> findByTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        // 创建一个新的Query对象
        Query query = new Query();
        // 如果你想要添加一些条件，你可以使用Criteria
        query.addCriteria(Criteria.where("createTime").gte(startTime));
        query.addCriteria(Criteria.where("createTime").lte(endTime));
        List<SampleOrder> res = mongoTemplate.find(query,SampleOrder.class);
        return res;
    }

    @Override
    public SampleOrderStatisticDTO statisticByTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        return new SampleOrderStatisticDTO("2022-01",10002L);
    }
}
