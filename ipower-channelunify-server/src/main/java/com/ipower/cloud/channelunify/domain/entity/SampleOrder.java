package com.ipower.cloud.channelunify.domain.entity;


import com.ipower.cloud.channelunify.domain.types.ShipmentAddress;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;


/**mongo操作例子对象
 * #{@sampleOrderRepositoyImpl.getCollectionName()}是用spel用来实现动态指定表名字，实现动态分表
 * 但是要注意，一开始在扫描@Document的时候bean还没有生成，所以需要在生成MongoMappingContext的地方把applicationContext注入进去
 * 在本工程中是在MongoConfig里面注入的,还有一个要注意的是在该表对应的dao里面不要再写死collection名字来查询来，全部靠这个class来判断
 * */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
//@Document(collection = "#{@sampleOrderRepositoyImpl.getCollectionName()}")
@Document(collection = "sample_order")
public class SampleOrder extends Entity {

    //主键用mongo自带的，这个作为唯一索引
    @Indexed(unique = true)
    private String orderCode;
    //订单标题
    private String orderTitle;

    @Transient//这个注解标识不会映射
    private String orderComment;

    //发货地址
    private ShipmentAddress shipmentAddress;


}
