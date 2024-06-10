package com.ipower.cloud.channelunify.jobs.timer;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.club.clubmanager.domain.entity.BarTableDateLock;
import com.club.clubmanager.domain.entity.Club;
import com.club.clubmanager.domain.entity.Company;
import com.club.clubmanager.domain.enums.BarTableDateLockTypeEnum;
import com.club.clubmanager.domain.repository.BarTableDateLockRepository;
import com.club.clubmanager.domain.repository.ClubRepository;
import com.club.clubmanager.domain.repository.CompanyRepository;
import com.club.clubmanager.domain.service.BarTableDomainService;
import com.club.clubmanager.infra.config.TenantContext;
import com.club.clubmanager.infra.jobs.timer.cmd.TargetComAndClubCmd;
import com.club.clubmanager.infra.jobs.timer.util.JobParamUtil;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author: xionglin
 * @description: 台位锁定（预约，预留）超时定时器，用于最终释放时间统一超时以及兜底
 */
@Component
@Slf4j
public class BarTableLockExpireHandler {

    @Resource
    CompanyRepository companyRepository;

    @Resource
    ClubRepository clubRepository;

    @Resource
    BarTableDateLockRepository tableDateLockRepository;

    @Resource
    BarTableDomainService barTableDomainService;

//    /**
//     * job参数cmd 必须采用静态内部类，否则非静态内部类无法再json转换的时候实例化
//     */

    @XxlJob("bookTableExpire")
    public void jobHandler() {

        // 分片参数 按分片数来对com进行取模后进行处理
        int shardIndex = XxlJobHelper.getShardIndex();
        int shardTotal = XxlJobHelper.getShardTotal();
        //指定参数,例子{"comId":"123","clubId":"123"}
        String jobParam = XxlJobHelper.getJobParam();

        XxlJobHelper.log("预定预留超时xxl定时处理开始执行, 当前分片序号 = {}, 总分片数 = {},jobParam={}", shardIndex, shardTotal, jobParam);
        log.info("预定预留超时xxl定时处理开始执行, 当前分片序号 = {}, 总分片数 = {},jobParam={}", shardIndex, shardTotal, jobParam);

        //超时释放
        bookTableExpire(shardIndex, shardTotal, jobParam);

        XxlJobHelper.log("预定预留超时xxl定时处理执行完毕, 当前分片序号 = {}, 总分片数 = {},jobParam={}", shardIndex, shardTotal, jobParam);
        log.info("预定预留超时xxl定时处理执行完毕, 当前分片序号 = {}, 总分片数 = {},jobParam={}", shardIndex, shardTotal, jobParam);
    }

    /**
     * 锁定桌位释放入口
     *
     * @param shardIndex
     * @param shardTotal
     * @param jobParam
     */
    private void bookTableExpire(int shardIndex, int shardTotal, String jobParam) {
        //无指定命令，执行全部公司
        if (StringUtils.isEmpty(jobParam)) {
            allCompanyDoBookExpire(shardIndex, shardTotal);
            //有指定命令，执行单个公司或门店
        } else {
            TargetComAndClubCmd cmd = JobParamUtil.getParm(jobParam, TargetComAndClubCmd.class);
            if (cmd == null) {
                XxlJobHelper.log("预定预留xxl定时处理失败,参数错误, 当前分片序号 = {}, 总分片数 = {},jobParam={}", shardIndex, shardTotal, jobParam);
                log.error("预定预留xxl定时处理失败,参数错误, 当前分片序号 = {}, 总分片数 = {},jobParam={}", shardIndex, shardTotal, jobParam);
                return;
            }
            //验证命令参数正确性
            cmd.validate();
            //单处理公司
            if (cmd.doCom()) {
                Company company = companyRepository.getById(cmd.getComId());
                companyDoBookExpire(company);
                //单处理门店
            } else if (cmd.doClub()) {
                Club club = clubRepository.getById(cmd.getClubId());
                clubDoBookExpire(club);
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
                    companyDoBookExpire(company);
                }
                //最后一个公司ID设置为下一个开始下标
                comIdStart = comList.get(comList.size() - 1).getId();
                comList = companyRepository.listComBatchShard(comIdStart, batchSize, shardIndex, shardTotal);
            }
        } while (comList != null && comList.size() > 0);
    }

    /**
     * 单company处理
     *
     * @param company
     */
    private void companyDoBookExpire(Company company) {
        if (company == null) {
            XxlJobHelper.log("空公司不处理");
            log.warn("空公司不处理");
            return;
        }
        XxlJobHelper.log("开始处理公司预留预定超时,公司={}", company.getName());
        log.debug("开始处理公司预留预定超时,公司={}", company.getName());
        //一般来说club数量不会超过10个
        List<Club> clubList = clubRepository.listByComId(company.getId());
        for (Club club : clubList) {
            clubDoBookExpire(club);
        }
    }

    /**
     * 单club处理
     *
     * @param club
     */
    private void clubDoBookExpire(Club club) {

        if (club == null) {
            XxlJobHelper.log("空门店不处理");
            log.warn("空门店不处理");
            return;
        }

        XxlJobHelper.log("开始处理门店预留预定超时,门店={}", club.getName());
        log.debug("开始处理门店预留预定超时,门店={}", club.getName());

        try (TenantContext tenantContext = TenantContext.getInstance()) {
            tenantContext.setTenantId(club.getComId());

            //查出所有还没删除的锁定进行兜底
            List<BarTableDateLock> tableLockExpireList = tableDateLockRepository.list(new QueryWrapper<BarTableDateLock>().lambda()
                    .eq(BarTableDateLock::getComId, club.getComId())
                    .eq(BarTableDateLock::getClubId, club.getId())
                    .le(BarTableDateLock::getExpireTime, LocalDateTime.now()));

            //依次更新为过期状态
            for (BarTableDateLock expireLock : tableLockExpireList) {
                try {
                    if (expireLock.getLockType() == BarTableDateLockTypeEnum.book) {
                        //单个事务控制在这儿，一个club失败不要影响其它club
                        barTableDomainService.expireBookBySystem(expireLock.getRecordId(), club.getComId(), club.getId());
                        log.error("处理台位锁定超时释放成功：club={},expireLock={}", club.getName(), expireLock);
                        XxlJobHelper.log("处理台位锁定超时释放成功：club={},expireLock={}", club.getName(), expireLock);
                    }else if(expireLock.getLockType() == BarTableDateLockTypeEnum.hold){
                        //单个事务控制在这儿，一个club失败不要影响其它club
                        barTableDomainService.expireHoldBySystem(expireLock.getRecordId(), club.getComId(), club.getId());
                        log.error("处理台位锁定超时释放成功：club={},expireLock={}", club.getName(), expireLock);
                        XxlJobHelper.log("处理台位锁定超时释放成功：club={},expireLock={}", club.getName(), expireLock);
                    }
                } catch (Exception e) {
                    log.error("处理台位锁定超时释放异常：club={},expireLock={}", club.getName(), expireLock);
                    XxlJobHelper.log("处理预定超时异常：club={},expireLock={}", club.getName(), expireLock);
                }
            }

        }

    }


}
