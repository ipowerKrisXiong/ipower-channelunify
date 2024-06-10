package com.ipower.cloud.channelunify.jobs.timer;

import com.club.clubmanager.application.service.WineDepositManageService;
import com.club.clubmanager.domain.entity.Club;
import com.club.clubmanager.domain.entity.Company;
import com.club.clubmanager.domain.entity.WineDepositRecord;
import com.club.clubmanager.domain.repository.ClubRepository;
import com.club.clubmanager.domain.repository.CompanyRepository;
import com.club.clubmanager.domain.repository.WineDepositRecordRepository;
import com.club.clubmanager.infra.config.TenantContext;
import com.club.clubmanager.infra.jobs.timer.cmd.TargetComAndClubCmd;
import com.club.clubmanager.infra.jobs.timer.util.JobParamUtil;
import com.mars.service.core.dic.SystemDefaultUserType;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * @author: hechenggang
 * @date: 2022/8/25 14:21
 * @description:
 */
@Component
@Slf4j
public class DepositWineExpireHandler {

    @Resource
    WineDepositRecordRepository wineDepositRecordRepository;

    @Resource
    CompanyRepository companyRepository;

    @Resource
    ClubRepository clubRepository;

    @Resource
    WineDepositManageService wineDepositManageService;

    @XxlJob("checkDepositWine")
    public void jobHandler() {
        // 分片参数 按分片数来对com进行取模后进行处理
        int shardIndex = XxlJobHelper.getShardIndex();
        int shardTotal = XxlJobHelper.getShardTotal();
        //指定参数,例子{"comId":"123","clubId":"123"}
        String jobParam = XxlJobHelper.getJobParam();

        XxlJobHelper.log("定时存酒超时处理 开始执行, 当前分片序号 = {}, 总分片数 = {},jobParam={}", shardIndex, shardTotal, jobParam);
        log.info("定时存酒超时处理 开始执行, 当前分片序号 = {}, 总分片数 = {},jobParam={}", shardIndex, shardTotal, jobParam);

        //超时释放
        checkDepositWine(shardIndex, shardTotal, jobParam);

        XxlJobHelper.log("定时存酒超时处理 执行完毕, 当前分片序号 = {}, 总分片数 = {},jobParam={}", shardIndex, shardTotal, jobParam);
        log.info("定时存酒超时处理 执行完毕, 当前分片序号 = {}, 总分片数 = {},jobParam={}", shardIndex, shardTotal, jobParam);
    }


    /**
     * @param shardIndex
     * @param shardTotal
     * @param jobParam
     */
    private void checkDepositWine(int shardIndex, int shardTotal, String jobParam) {
        //无指定命令，执行全部公司
        if (StringUtils.isEmpty(jobParam)) {
            allCompanyDoBookExpire(shardIndex, shardTotal);
            //有指定命令，执行单个公司或门店
        } else {
            TargetComAndClubCmd cmd = JobParamUtil.getParm(jobParam, TargetComAndClubCmd.class);
            if (cmd == null) {
                XxlJobHelper.log("定时存酒超时处理 失败,参数错误, 当前分片序号 = {}, 总分片数 = {},jobParam={}", shardIndex, shardTotal, jobParam);
                log.error("定时存酒超时处理 失败,参数错误, 当前分片序号 = {}, 总分片数 = {},jobParam={}", shardIndex, shardTotal, jobParam);
                return;
            }
            //验证命令参数正确性
            cmd.validate();
            //单处理公司
            if (cmd.doCom()) {
                Company company = companyRepository.getById(cmd.getComId());
                comWineExpire(company);
                //单处理门店
            } else if (cmd.doClub()) {
                Club club = clubRepository.getById(cmd.getClubId());
                clubWineExpire(club);
            }
        }
    }

    /**
     * 全company处理
     *
     * @param shardIndex
     * @param shardTotal
     */
    private void allCompanyDoBookExpire(int shardIndex, int shardTotal) {
        //每次查10条公司数据处理
        Long comIdStart = 0L;
        Integer batchSize = 10;
        List<Company> comList = companyRepository.listComBatchShard(comIdStart, batchSize, shardIndex, shardTotal);
        do {
            if (comList != null && !comList.isEmpty()) {
                // 业务逻辑,循环处理每一个公司的预定单超时问题
                for (Company company : comList) {
                    //处理某个公司
                    comWineExpire(company);
                }
                //最后一个公司ID设置为下一个开始下标
                comIdStart = comList.get(comList.size() - 1).getId();
                comList = companyRepository.listComBatchShard(comIdStart, batchSize, shardIndex, shardTotal);
            }
        } while (comList != null && comList.size() > 0);
    }


    public void comWineExpire(Company company) {
        if (company == null) {
            XxlJobHelper.log("空公司不处理");
            log.warn("空公司不处理");
            return;
        }
        XxlJobHelper.log("开始处理公司存酒超时,公司={}", company.getName());
        log.debug("开始处理公司存酒超时,公司={}", company.getName());
        //一般来说club数量不会超过10个
        List<Club> clubList = clubRepository.listByComId(company.getId());
        for (Club club : clubList) {
            try {
                clubWineExpire(club);
            } catch (Exception e) {
                XxlJobHelper.log("处理公司存酒超时,处理失败! club={}", club, e);
                log.error("处理公司存酒超时,处理失败! club={}", club, e);
            }
        }
    }

    public void clubWineExpire(Club club) {
        try (TenantContext tenantContext = TenantContext.getInstance()) {
            tenantContext.setTenantId(club.getComId());
            //每次查n条数据处理
            Long startId = 0L;
            Integer batchSize = 20;
            List<WineDepositRecord> needDoList = wineDepositRecordRepository.listDepositExpireBatch(club.getId(), startId, batchSize);
            do {
                if (needDoList != null && !needDoList.isEmpty()) {
                    // 业务逻辑,循环处理每一个公司的预定单超时问题
                    List<WineDepositRecord> updateList = new ArrayList<>();
                    for (WineDepositRecord wineDepositRecord : needDoList) {
                        WineDepositRecord upItem = new WineDepositRecord();
                        upItem.setId(wineDepositRecord.getId());
                        upItem.setExpireStatus(true);
                        upItem.setExpireNum(wineDepositRecord.getDepositNum() - wineDepositRecord.getExtractNum());
                        upItem.setUpdateName(SystemDefaultUserType.SYSTEM.getName());
                        upItem.setUpdateId(SystemDefaultUserType.SYSTEM.getCode());
                        upItem.setUpdateTime(LocalDateTime.now());
                        updateList.add(upItem);
                    }
                    //每次更新一批
                    wineDepositManageService.clubWineExpireBatch(updateList);
                    startId = needDoList.get(needDoList.size() - 1).getId();
                    needDoList = wineDepositRecordRepository.listDepositExpireBatch(club.getId(), startId, batchSize);
                }
            } while (needDoList != null && needDoList.size() > 0);
        }
    }


}
