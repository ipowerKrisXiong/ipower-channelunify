package com.ipower.cloud.channelunify.domain.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class Entity implements Serializable {

    //对应mongo中的objectId"_id",这里不用加@Id注解，save的时候空着会自动生成
//    @Id
    private String id;

    @Indexed
    private LocalDateTime createTime;

    private LocalDateTime updateTime;

}
