package com.ipower.cloud.channelunify.jobs.timer.cmd;

import lombok.Data;

import java.time.YearMonth;

@Data
public class TargetCommissionMoneyCmd {

    YearMonth yearMonth;
    Long comId;
    Long clubId;

    //必须要设置无参数构造器，才可以被正常json序列化
    public TargetCommissionMoneyCmd() {
    }

    public void validate() {
        if (yearMonth == null)
            throw new RuntimeException("job命令参数错误!");
        if (comId == null)
            throw new RuntimeException("job命令参数错误!");
    }

    public boolean doCom() {
        if (comId != null && clubId == null) {
            return true;
        } else {
            return false;
        }
    }

    public boolean doClub() {
        if (comId != null && clubId != null) {
            return true;
        } else {
            return false;
        }
    }

//    public static void main(String[] args) {
//        TargetCommissionMoneyCmd a =new TargetCommissionMoneyCmd();
//        a.setClubId(100L);
//        a.setComId(100L);
//        a.setYearMonth(DateUtil.strToYearMonth("2022-01",DateUtil.MOTH_PATTERN_1));
//        System.out.println(a);
//        System.out.println(JobParamUtil.getParm("yearMonth=2022-10&comId=1&clubId=1"));
//        System.out.println(JobParamUtil.<TargetCommissionMoneyCmd>getParm("yearMonth=2022-10&comId=1&clubId=1", TargetCommissionMoneyCmd.class));
//    }
}
