package com.ipower.cloud.channelunify.jobs.timer;

import com.club.clubmanager.application.service.coupon.CouponMallOrderService;
import com.club.clubmanager.domain.entity.Club;
import com.club.clubmanager.domain.entity.Company;
import com.club.clubmanager.domain.entity.CouponMemberRecord;
import com.club.clubmanager.domain.enums.CouponRecordDisableReason;
import com.club.clubmanager.domain.repository.ClubRepository;
import com.club.clubmanager.domain.repository.CompanyRepository;
import com.club.clubmanager.domain.repository.CouponMemberRecordRepository;
import com.club.clubmanager.domain.service.CouponDomainService;
import com.club.clubmanager.infra.config.TenantContext;
import com.club.clubmanager.infra.jobs.timer.cmd.TargetComAndClubCmd;
import com.club.clubmanager.infra.jobs.timer.util.JobParamUtil;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author: xionglin
 * @description: 优惠券领取记录超时兜底
 */
@Component
@Slf4j
public class CouponMemberRecordExpireHandler {

    @Resource
    CompanyRepository companyRepository;

    @Resource
    ClubRepository clubRepository;

    @Resource
    CouponMemberRecordRepository couponMemberRecordRepository;

    @Resource
    CouponMallOrderService couponMallOrderService;

    @Resource
    CouponDomainService couponDomainService;

    @XxlJob("couponMemberRecordLiveExpire")
    public void jobHandler() {

        // 分片参数 按分片数来对com进行取模后进行处理
        int shardIndex = XxlJobHelper.getShardIndex();
        int shardTotal = XxlJobHelper.getShardTotal();
        //指定参数,例子{"comId":"123","clubId":"123"}
        String jobParam = XxlJobHelper.getJobParam();

        XxlJobHelper.log("优惠券领取记录超时xxl定时处理开始执行, 当前分片序号 = {}, 总分片数 = {},jobParam={}", shardIndex, shardTotal, jobParam);
        log.info("优惠券领取记录超时xxl定时处理开始执行, 当前分片序号 = {}, 总分片数 = {},jobParam={}", shardIndex, shardTotal, jobParam);

        //超时释放
        couponRecordExpire(shardIndex, shardTotal, jobParam);

        XxlJobHelper.log("优惠券领取记录超时xxl定时处理执行完毕, 当前分片序号 = {}, 总分片数 = {},jobParam={}", shardIndex, shardTotal, jobParam);
        log.info("优惠券领取记录超时xxl定时处理执行完毕, 当前分片序号 = {}, 总分片数 = {},jobParam={}", shardIndex, shardTotal, jobParam);
    }

    /**
     * 锁定桌位释放入口
     *
     * @param shardIndex
     * @param shardTotal
     * @param jobParam
     */
    private void couponRecordExpire(int shardIndex, int shardTotal, String jobParam) {
        //无指定命令，执行全部公司
        if (StringUtils.isEmpty(jobParam)) {
            allCompanyDoCouponRecordExpire(shardIndex, shardTotal);
        //有指定命令，执行单个公司或门店
        } else {
            TargetComAndClubCmd cmd = JobParamUtil.getParm(jobParam, TargetComAndClubCmd.class);
            if (cmd == null) {
                XxlJobHelper.log("优惠券领取记录超时xxl定时处理失败,参数错误, 当前分片序号 = {}, 总分片数 = {},jobParam={}", shardIndex, shardTotal, jobParam);
                log.error("优惠券领取记录超时xxl定时处理失败,参数错误, 当前分片序号 = {}, 总分片数 = {},jobParam={}", shardIndex, shardTotal, jobParam);
                return;
            }
            //验证命令参数正确性
            cmd.validate();
            //单处理公司
            if (cmd.doCom()) {
                Company company = companyRepository.getById(cmd.getComId());
                companyDoCouponRecordExpire(company);
                //单处理门店
            } else if (cmd.doClub()) {
                Club club = clubRepository.getById(cmd.getClubId());
                clubDoCouponRecordExpire(club);
            }
        }
    }

    /**
     * 全company处理
     *
     * @param shardIndex
     * @param shardTotal
     */
    private void allCompanyDoCouponRecordExpire(int shardIndex, int shardTotal) {
        //每次查10条公司数据处理
        Long comIdStart = 0L;
        Integer batchSize = 10;
        List<Company> comList = companyRepository.listComBatchShard(comIdStart, batchSize, shardIndex, shardTotal);
        do {
            if (comList != null && !comList.isEmpty()) {
                // 业务逻辑,循环处理每一个公司的预定单超时问题
                for (Company company : comList) {
                    //处理某个公司
                    companyDoCouponRecordExpire(company);
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
    private void companyDoCouponRecordExpire(Company company) {
        if (company == null) {
            XxlJobHelper.log("空公司不处理");
            log.warn("空公司不处理");
            return;
        }
        XxlJobHelper.log("开始处理优惠券领取记录超时,公司={}", company.getName());
        log.debug("开始处理优惠券领取记录超时,公司={}", company.getName());
        //一般来说club数量不会超过10个
        List<Club> clubList = clubRepository.listByComId(company.getId());
        for (Club club : clubList) {
            clubDoCouponRecordExpire(club);
        }
    }

    /**
     * 单club处理
     *
     * @param club
     */
    private void clubDoCouponRecordExpire(Club club) {

        if (club == null) {
            XxlJobHelper.log("空门店不处理");
            log.warn("空门店不处理");
            return;
        }

        XxlJobHelper.log("开始处理优惠券领取记录超时,门店={}", club.getName());
        log.debug("开始处理优惠券领取记录超时,门店={}", club.getName());

        try (TenantContext tenantContext = TenantContext.getInstance()) {
            tenantContext.setTenantId(club.getComId());
            //每次查200条数据处理
            Long idStart = 0L;
            Integer batchSize = 200;
            List<CouponMemberRecord> recordList = couponMemberRecordRepository.listExpiredNoWriteOffRecord(club.getComId(),club.getId(), idStart,batchSize);
            do {
                if (recordList != null && !recordList.isEmpty()) {
                    for (CouponMemberRecord record : recordList) {
                        try{
                            couponDomainService.disableCouponMemberRecord(record.getId(), CouponRecordDisableReason.EXPIRED, couponMallOrderService);
                        }catch (Exception e){
                            log.error("处理优惠券领取记录超时失败：clubId={},recordId={},expireTime={}", club.getName(), record.getId(),record.getActiveTimeEnd());
                            XxlJobHelper.log("处理优惠券领取记录超时失败：clubId={},recordId={},expireTime={}", club.getName(), record.getId(),record.getActiveTimeEnd());
                        }
                    }
                    //最后一个公司ID设置为下一个开始下标
                    idStart = recordList.get(recordList.size() - 1).getId();
                    recordList = couponMemberRecordRepository.listExpiredNoWriteOffRecord(club.getComId(),club.getId(), idStart,batchSize);
                }
            } while (recordList != null && recordList.size() > 0);
        }

        log.error("处理优惠券领取记录超时成功：clubId={}", club.getName());
        XxlJobHelper.log("处理优惠券领取记录超时成功：clubId={}", club.getName());
    }


}
