package com.ipower.cloud.channelunify.application;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.RandomUtil;
import cn.need.framework.common.core.bean.BeanUtil;
import com.ipower.cloud.channelunify.application.dto.SampleOrderDTO;
import com.ipower.cloud.channelunify.application.dto.ShipmentAddressDTO;
import com.ipower.cloud.channelunify.domain.entity.SampleOrder;
import com.ipower.cloud.channelunify.domain.repository.SampleOrderRepository;
import com.ipower.cloud.channelunify.domain.types.ShipmentAddress;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 业务编排层service，对领域层的逻辑进行编排，满足一个个业务case作为方法入口
 * 一个类未一组业务case的集合
 */
@Service
public class SampleOrderManageService {


    SampleOrderRepository sampleOrderRepositoy;

    @Autowired
    public SampleOrderManageService(SampleOrderRepository sampleOrderRepositoy) {
        this.sampleOrderRepositoy = sampleOrderRepositoy;
    }

    //查询详情
    public SampleOrderDTO detail(String orderId){
        SampleOrder order =  sampleOrderRepositoy.findById(orderId).orElse(null);
        SampleOrderDTO res = SampleOrderDTO.valueOf(order);
        return res;
    }

    //自动新增
    public SampleOrderDTO addAuto(){
        SampleOrder sampleOrder = new SampleOrder();
        LocalDateTime now = LocalDateTime.now();
        long epoch = LocalDateTimeUtil.toEpochMilli(now);
        String orderCode = epoch + RandomUtil.randomString(6);
        String title = "测试订单"+ orderCode;
        sampleOrder.setOrderTitle(title);
        sampleOrder.setOrderCode(orderCode);
        sampleOrder.setShipmentAddress(new ShipmentAddress("1123","地址:"+orderCode));
        sampleOrder.setCreateTime(now);
        sampleOrder = sampleOrderRepositoy.save(sampleOrder);
        return SampleOrderDTO.valueOf(sampleOrder);
    }




}
