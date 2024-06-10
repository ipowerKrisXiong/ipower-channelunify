package com.ipower.cloud.channelunify.jobs.timer;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.club.clubmanager.domain.entity.BarTableOpLog;
import com.club.clubmanager.domain.entity.Club;
import com.club.clubmanager.domain.entity.Company;
import com.club.clubmanager.domain.repository.BarTableOpLogRepository;
import com.club.clubmanager.domain.repository.ClubRepository;
import com.club.clubmanager.domain.repository.CompanyRepository;
import com.club.clubmanager.infra.config.TenantContext;
import com.club.clubmanager.infra.jobs.timer.cmd.TargetComAndClubCmd;
import com.club.clubmanager.infra.jobs.timer.util.JobParamUtil;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 台位操作记录清理
 */
@Slf4j
@Component
public class BarTableOpLogCleanHandler {

    @Autowired
    CompanyRepository companyRepository;

    @Autowired
    ClubRepository clubRepository;

    @Autowired
    BarTableOpLogRepository barTableOpLogRepository;

    @XxlJob("barTableOpLogClean")
    public void jobHandler() {

        // 分片参数 按分片数来对com进行取模后进行处理
        int shardIndex = XxlJobHelper.getShardIndex();
        int shardTotal = XxlJobHelper.getShardTotal();
        //指定参数,例子{"comId":"123","clubId":"123"}
        String jobParam = XxlJobHelper.getJobParam();

        XxlJobHelper.log("定时清理台位操作记录 开始执行, 当前分片序号 = {}, 总分片数 = {},jobParam={}", shardIndex, shardTotal, jobParam);
        log.info("定时清理台位操作记录 开始执行, 当前分片序号 = {}, 总分片数 = {},jobParam={}", shardIndex, shardTotal, jobParam);

        barTableOpLogClean(shardIndex, shardTotal, jobParam);

        XxlJobHelper.log("定时清理台位操作记录 执行完毕, 当前分片序号 = {}, 总分片数 = {},jobParam={}", shardIndex, shardTotal, jobParam);
        log.info("定时清理台位操作记录 执行完毕, 当前分片序号 = {}, 总分片数 = {},jobParam={}", shardIndex, shardTotal, jobParam);

    }

    private void barTableOpLogClean(int shardIndex, int shardTotal, String jobParam) {
        //无指定命令，执行全部公司
        if (StringUtils.isEmpty(jobParam)) {
            allBarTableOpLogClean(shardIndex, shardTotal);
            //有指定命令，执行单个公司或门店
        } else {
            TargetComAndClubCmd cmd = JobParamUtil.getParm(jobParam, TargetComAndClubCmd.class);
            if (cmd == null) {
                XxlJobHelper.log("定时清理台位操作记录 失败,参数错误, 当前分片序号 = {}, 总分片数 = {},jobParam={}", shardIndex, shardTotal, jobParam);
                log.error("定时清理台位操作记录 失败,参数错误, 当前分片序号 = {}, 总分片数 = {},jobParam={}", shardIndex, shardTotal, jobParam);
                return;
            }
            //验证命令参数正确性
            cmd.validate();
            //单处理某个公司某个门店
            if (cmd.doCom()) {
                Club club = clubRepository.getById(cmd.getClubId());
                clubCleanTableOpLog(club);
                //不支持单处理门店
            } else if (cmd.doClub()) {
                XxlJobHelper.log("定时重置赠送金额 不支持单门店处理, 当前分片序号 = {}, 总分片数 = {},jobParam={}", shardIndex, shardTotal, jobParam);
                log.error("定时重置赠送金额 不支持单门店处理, 当前分片序号 = {}, 总分片数 = {},jobParam={}", shardIndex, shardTotal, jobParam);
            }
        }
    }


    /**
     * 全company处理
     *
     * @param shardIndex
     * @param shardTotal
     */
    private void allBarTableOpLogClean(int shardIndex, int shardTotal) {
        //每次查10条公司数据处理
        Long comIdStart = 0L;
        Integer batchSize = 10;
        List<Company> comList = companyRepository.listComBatchShard(comIdStart, batchSize, shardIndex, shardTotal);
        do {
            if (comList != null && !comList.isEmpty()) {
                // 业务逻辑,循环处理每一个公司
                for (Company company : comList) {
                    List<Club> clubList = clubRepository.list(new QueryWrapper<Club>().lambda().eq(Club::getComId,company.getId()));
                    //处理某个门店
                    for (Club club : clubList) {
                        clubCleanTableOpLog(club);
                    }
                }
                //最后一个公司ID设置为下一个开始下标
                comIdStart = comList.get(comList.size() - 1).getId();
                comList = companyRepository.listComBatchShard(comIdStart, batchSize, shardIndex, shardTotal);
            }
        } while (comList != null && comList.size() > 0);
    }

    public void clubCleanTableOpLog(Club club) {
        if (club == null) {
            XxlJobHelper.log("空门店不处理");
            log.warn("空门店不处理");
            return;
        }
        XxlJobHelper.log("开始清理门店台位操作记录,公司={}", club.getName());
        log.info("开始清理门店台位操作记录,公司={}", club.getName());

        try (TenantContext tenantContext = TenantContext.getInstance()) {
            tenantContext.setTenantId(club.getComId());
            //删除40天前操作记录
            barTableOpLogRepository.remove(new QueryWrapper<BarTableOpLog>().lambda()
                    .eq(BarTableOpLog::getClubId,club.getId())
                    .le(BarTableOpLog::getCreateTime, LocalDateTime.now().minusDays(40))
            );
        }
    }

}
