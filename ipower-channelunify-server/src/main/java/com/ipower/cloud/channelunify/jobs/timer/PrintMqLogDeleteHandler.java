package com.ipower.cloud.channelunify.jobs.timer;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.club.clubmanager.domain.entity.Club;
import com.club.clubmanager.domain.entity.Company;
import com.club.clubmanager.domain.entity.PrintMqLog;
import com.club.clubmanager.domain.repository.ClubRepository;
import com.club.clubmanager.domain.repository.CompanyRepository;
import com.club.clubmanager.domain.repository.PrintMqLogRepository;
import com.club.clubmanager.domain.service.PrintMqLogDomainService;
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
 * @description 打印Mq日志删除处理程序
 * @author Ryan Wang
 * @date 2023-06-02 11:23:33
 * @version 1.0
 */
@Slf4j
@Component
public class PrintMqLogDeleteHandler {

    @Autowired
    private PrintMqLogRepository printMqLogRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private ClubRepository clubRepository;

    @Autowired
    private PrintMqLogDomainService printMqLogDomainService;

    @XxlJob("printMqLogDeleteHandler")
    public void printMqLogDeleteHandler() {

        // 分片参数 按分片数来对com进行取模后进行处理
        int shardIndex = XxlJobHelper.getShardIndex();
        int shardTotal = XxlJobHelper.getShardTotal();
        // 指定参数,例子{"comId":"123","clubId":"123"}
        String jobParam = XxlJobHelper.getJobParam();

        XxlJobHelper.log("定时删除打印Mq日志数据 开始执行, 当前分片序号 = {}, 总分片数 = {},jobParam={}", shardIndex, shardTotal, jobParam);
        log.info("定时删除打印Mq日志数据 开始执行, 当前分片序号 = {}, 总分片数 = {},jobParam={}", shardIndex, shardTotal, jobParam);

        // 超时释放
        deletePrintMqLogData(shardIndex, shardTotal, jobParam);

        XxlJobHelper.log("定时删除打印Mq日志数据 执行完毕, 当前分片序号 = {}, 总分片数 = {},jobParam={}", shardIndex, shardTotal, jobParam);
        log.info("定时删除打印Mq日志数据 执行完毕, 当前分片序号 = {}, 总分片数 = {},jobParam={}", shardIndex, shardTotal, jobParam);
    }


    private void deletePrintMqLogData(int shardIndex, int shardTotal, String jobParam) {
        // 无指定命令，执行全部公司
        if (StringUtils.isEmpty(jobParam)) {
            processAllDeletePrintMqLogData(shardIndex, shardTotal);
            // 有指定命令，执行单个公司或门店
        } else {
            TargetComAndClubCmd cmd = JobParamUtil.getParm(jobParam, TargetComAndClubCmd.class);
            if (cmd == null) {
                XxlJobHelper.log("定时删除打印Mq日志数据 失败,参数错误, 当前分片序号 = {}, 总分片数 = {},jobParam={}", shardIndex, shardTotal, jobParam);
                log.error("定时删除打印Mq日志数据 失败,参数错误, 当前分片序号 = {}, 总分片数 = {},jobParam={}", shardIndex, shardTotal, jobParam);
                return;
            }
            // 验证命令参数正确性
            cmd.validate();
            // 单处理公司
            if (cmd.doCom()) {
                Company company = companyRepository.getById(cmd.getComId());
                processCompanyDeletePrintMqLogData(company);
            } else if (cmd.doClub()) {
                // 单处理门店
                Club club = clubRepository.getById(cmd.getClubId());
                processClubDeletePrintMqLogData(club);
            }
        }
    }


    /**
     * 处理全部数据
     * @param shardIndex
     * @param shardTotal
     */
    private void processAllDeletePrintMqLogData(int shardIndex, int shardTotal) {
        // 每次查10条公司数据处理
        Long comIdStart = 0L;
        Integer batchSize = 10;
        List<Company> comList = companyRepository.listComBatchShard(comIdStart, batchSize, shardIndex, shardTotal);
        do {
            if (comList != null && !comList.isEmpty()) {
                // 业务逻辑,循环处理每一个公司的预定单超时问题
                for (Company company : comList) {
                    // 处理某个公司
                    processCompanyDeletePrintMqLogData(company);
                }
                // 最后一个公司ID设置为下一个开始下标
                comIdStart = comList.get(comList.size() - 1).getId();
                comList = companyRepository.listComBatchShard(comIdStart, batchSize, shardIndex, shardTotal);
            }
        } while (comList != null && comList.size() > 0);
    }

    /**
     * 处理公司删除打印Mq日志数据
     * @param company
     */
    public void processCompanyDeletePrintMqLogData(Company company) {
        if (company == null) {
            XxlJobHelper.log("空公司不处理");
            log.warn("空公司不处理");
            return;
        }
        XxlJobHelper.log("开始删除打印Mq日志数据,公司={}", company.getName());
        log.debug("开始删除打印Mq日志数据,公司={}", company.getName());

        try (TenantContext tenantContext = TenantContext.getInstance()) {
            Long comId = company.getId();
            tenantContext.setTenantId(comId);
            printMqLogRepository.remove(Wrappers.lambdaQuery(PrintMqLog.class)
                    .eq(PrintMqLog::getComId, comId)
                    .le(PrintMqLog::getCreateTime, LocalDateTime.now().minusDays(10)));
        }
    }

    /**
     * 处理门店删除打印Mq日志数据
     * @param club
     */
    public void processClubDeletePrintMqLogData(Club club) {
        if (club == null) {
            XxlJobHelper.log("空门店不处理");
            log.warn("空门店不处理");
            return;
        }
        XxlJobHelper.log("开始删除打印Mq日志数据,门店={}", club.getName());
        log.debug("开始删除打印Mq日志数据,门店={}", club.getName());

        try (TenantContext tenantContext = TenantContext.getInstance()) {
            Long clubId = club.getId();
            Long comId = club.getComId();
            tenantContext.setTenantId(comId);
            printMqLogRepository.remove(Wrappers.lambdaQuery(PrintMqLog.class)
                    .eq(PrintMqLog::getClubId, clubId)
                    .le(PrintMqLog::getCreateTime, LocalDateTime.now().minusDays(10)));
        }
    }

}
