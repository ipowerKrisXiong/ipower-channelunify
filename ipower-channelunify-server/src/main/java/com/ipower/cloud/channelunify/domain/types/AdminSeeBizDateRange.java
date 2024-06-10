package com.ipower.cloud.channelunify.domain.types;

import lombok.Data;

import java.time.LocalDate;

/**
 * 管理端可见的工作日范围 = 当前公工作日+未来可预留预约日期工作日
 */
@Data
public class AdminSeeBizDateRange {

    //开始时间 2022-01-01
    LocalDate bizDateStart;

    //结束时间 2022-01-08
    LocalDate bizDateEnd;

    LocalDate curBizDate;


    public AdminSeeBizDateRange(LocalDate bizDateStart, LocalDate bizDateEnd,LocalDate curBizDate) {
        this.bizDateStart = bizDateStart;
        this.bizDateEnd = bizDateEnd;
        this.curBizDate = curBizDate;
    }
}
