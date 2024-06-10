package com.ipower.cloud.channelunify.domain.repository.query;

import lombok.Data;

/**
 * @description 获取公司或门店绑定的移动收单账户或设备收单账户查询条件
 * @author Ryan Wang
 * @date 2023-04-21 16:27:21
 * @version 1.0
 */
@Data
public class AccountPageListQueryWrapper  {

    /**
     * 收单账户绑定类型:1-移动收单户 2-设备收单户
     */
    private Integer incomeMchBindType;

    //公司ID
    private Long comId;

    //门店ID，当绑定对象是门店时必传
    private Long clubId;

    public AccountPageListQueryWrapper(Integer incomeMchBindType, Long comId, Long clubId) {
        this.incomeMchBindType = incomeMchBindType;
        this.comId = comId;
        this.clubId = clubId;
    }
}
