package com.ipower.cloud.channelunify.application.dto;

import cn.need.framework.common.core.bean.BeanUtil;
import com.ipower.cloud.channelunify.domain.entity.SampleOrder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SampleOrderDTO {

    private String id;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private long orderCode;
    //订单标题
    private String orderTitle;

    private String orderComment;

    //发货地址
    private ShipmentAddressDTO shipmentAddress;


    public static SampleOrderDTO valueOf(SampleOrder order){
        if(order !=null){
            SampleOrderDTO res = BeanUtil.copyNew(order,SampleOrderDTO.class);
            res.setShipmentAddress(BeanUtil.copyNew(order.getShipmentAddress(), ShipmentAddressDTO.class));
            return res;
        }else{
            return null;
        }
    }
}
