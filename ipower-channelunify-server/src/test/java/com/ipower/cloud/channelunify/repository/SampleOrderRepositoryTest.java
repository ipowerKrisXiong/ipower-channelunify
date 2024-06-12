package com.ipower.cloud.channelunify.repository;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.RandomUtil;
import com.ipower.cloud.channelunify.domain.entity.SampleOrder;
import com.ipower.cloud.channelunify.domain.repository.SampleOrderCustomizedRepository;
import com.ipower.cloud.channelunify.domain.repository.SampleOrderRepository;
import com.ipower.cloud.channelunify.domain.types.ShipmentAddress;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;

import java.time.LocalDateTime;
import java.util.List;

//@FixMethodOrder(MethodSorters.NAME_ASCENDING)//按方法名字顺序执行
////@FixMethodOrder(MethodSorters.JVM)//从上到下执行，默认是default不可预期，可能同时执行
//@SpringBootTest(
////        classes= Application.class,webEnvironment = SpringBootTest.WebEnvironment.MOCK
//)

@DataMongoTest
public class SampleOrderRepositoryTest {

    @Autowired
    private SampleOrderRepository sampleOrderRepository;

    @Autowired
    private SampleOrderCustomizedRepository sampleOrderCustomizedRepository;


    @Test
    void testcrud() {

        SampleOrder sampleOrder = new SampleOrder();
        LocalDateTime now = LocalDateTime.now();
        long epoch = LocalDateTimeUtil.toEpochMilli(now);
        String orderCode = epoch + RandomUtil.randomString(6);
        String title = "测试订单"+ orderCode;
        sampleOrder.setOrderTitle(title);
        sampleOrder.setOrderCode(orderCode);
        sampleOrder.setShipmentAddress(new ShipmentAddress("1123","地址:"+orderCode));
        sampleOrder.setCreateTime(now);
        sampleOrder = sampleOrderRepository.save(sampleOrder);
        Assertions.assertNotNull(sampleOrder);

        SampleOrder findOne = sampleOrderRepository.findById(sampleOrder.getId()).orElse(null);
        Assertions.assertNotNull(findOne);
        Assertions.assertEquals(orderCode,findOne.getOrderCode());

        List<SampleOrder> sampleOrders = sampleOrderCustomizedRepository.findByTimeRange(LocalDateTime.now().minusDays(1),LocalDateTime.now().plusDays(1));

        Assertions.assertNotNull(sampleOrders);
        Assertions.assertTrue(sampleOrders.size()>0);

    }

}
