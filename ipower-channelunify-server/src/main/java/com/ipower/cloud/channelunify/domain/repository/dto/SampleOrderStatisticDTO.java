package com.ipower.cloud.channelunify.domain.repository.dto;

import lombok.Data;

/**
 * 如果有要求直接转换而不是返回entity的，返回对象定义放在这，一般统计聚合类的对象这种需求比较多
 */
@Data
public class SampleOrderStatisticDTO {

    String month;

    Long totalOrderCount;

    public SampleOrderStatisticDTO(String month, Long totalOrderCount) {
        this.month = month;
        this.totalOrderCount = totalOrderCount;
    }

    public SampleOrderStatisticDTO() {
    }
}
