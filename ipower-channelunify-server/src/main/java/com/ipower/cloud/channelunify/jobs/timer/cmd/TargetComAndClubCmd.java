package com.ipower.cloud.channelunify.jobs.timer.cmd;

import lombok.Data;

@Data
public class TargetComAndClubCmd {

    Long comId;
    Long clubId;

    //必须要设置无参数构造器，才可以被正常json序列化
    public TargetComAndClubCmd() {
    }

    public void validate() {
        if (comId == null && clubId == null)
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
}
