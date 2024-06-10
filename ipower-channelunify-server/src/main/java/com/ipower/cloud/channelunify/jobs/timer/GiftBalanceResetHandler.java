package com.ipower.cloud.channelunify.jobs.timer;

import com.club.clubmanager.domain.entity.Company;
import com.club.clubmanager.domain.entity.GiftBalance;
import com.club.clubmanager.domain.repository.ClubRepository;
import com.club.clubmanager.domain.repository.CompanyRepository;
import com.club.clubmanager.domain.repository.GiftBalanceRepository;
import com.club.clubmanager.domain.service.GiftBalanceDomainService;
import com.club.clubmanager.infra.config.TenantContext;
import com.club.clubmanager.infra.jobs.timer.cmd.TargetComAndClubCmd;
import com.club.clubmanager.infra.jobs.timer.util.JobParamUtil;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Ryan Wang
 * @version 1.0
 * @description 赠送金额重置定时器
 * @date 2022-10-18 11:40:02
 */
@Slf4j
@Component
public class GiftBalanceResetHandler {

    @Autowired
    CompanyRepository companyRepository;

    @Autowired
    ClubRepository clubRepository;

    @Autowired
    private GiftBalanceRepository giftBalanceRepository;

    @Autowired
    private GiftBalanceDomainService giftBalanceDomainService;

    @XxlJob("giftBalanceReset")
    public void jobHandler() {

        // 分片参数 按分片数来对com进行取模后进行处理
        int shardIndex = XxlJobHelper.getShardIndex();
        int shardTotal = XxlJobHelper.getShardTotal();
        //指定参数,例子{"comId":"123","clubId":"123"}
        String jobParam = XxlJobHelper.getJobParam();

        XxlJobHelper.log("定时重置赠送金额 开始执行, 当前分片序号 = {}, 总分片数 = {},jobParam={}", shardIndex, shardTotal, jobParam);
        log.info("定时重置赠送金额 开始执行, 当前分片序号 = {}, 总分片数 = {},jobParam={}", shardIndex, shardTotal, jobParam);

        //超时释放
        giftBalanceReset(shardIndex, shardTotal, jobParam);

        XxlJobHelper.log("定时重置赠送金额 执行完毕, 当前分片序号 = {}, 总分片数 = {},jobParam={}", shardIndex, shardTotal, jobParam);
        log.info("定时重置赠送金额 执行完毕, 当前分片序号 = {}, 总分片数 = {},jobParam={}", shardIndex, shardTotal, jobParam);

    }

    private void giftBalanceReset(int shardIndex, int shardTotal, String jobParam) {
        //无指定命令，执行全部公司
        if (StringUtils.isEmpty(jobParam)) {
            allResetGiftBalance(shardIndex, shardTotal);
            //有指定命令，执行单个公司或门店
        } else {
            TargetComAndClubCmd cmd = JobParamUtil.getParm(jobParam, TargetComAndClubCmd.class);
            if (cmd == null) {
                XxlJobHelper.log("定时重置赠送金额 失败,参数错误, 当前分片序号 = {}, 总分片数 = {},jobParam={}", shardIndex, shardTotal, jobParam);
                log.error("定时重置赠送金额 失败,参数错误, 当前分片序号 = {}, 总分片数 = {},jobParam={}", shardIndex, shardTotal, jobParam);
                return;
            }
            //验证命令参数正确性
            cmd.validate();
            //单处理公司
            if (cmd.doCom()) {
                Company company = companyRepository.getById(cmd.getComId());
                comResetGiftBalance(company);
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
    private void allResetGiftBalance(int shardIndex, int shardTotal) {
        //每次查10条公司数据处理
        Long comIdStart = 0L;
        Integer batchSize = 10;
        List<Company> comList = companyRepository.listComBatchShard(comIdStart, batchSize, shardIndex, shardTotal);
        do {
            if (comList != null && !comList.isEmpty()) {
                // 业务逻辑,循环处理每一个公司的预定单超时问题
                for (Company company : comList) {
                    //处理某个公司
                    comResetGiftBalance(company);
                }
                //最后一个公司ID设置为下一个开始下标
                comIdStart = comList.get(comList.size() - 1).getId();
                comList = companyRepository.listComBatchShard(comIdStart, batchSize, shardIndex, shardTotal);
            }
        } while (comList != null && comList.size() > 0);
    }

    //因为目前赠送积分是挂在人身上，没有club区分，所以直接公司级别查询即可
    public void comResetGiftBalance(Company company) {
        if (company == null) {
            XxlJobHelper.log("空公司不处理");
            log.warn("空公司不处理");
            return;
        }
        XxlJobHelper.log("开始重置赠送金额,公司={}", company.getName());
        log.debug("开始重置赠送金额,公司={}", company.getName());

        try (TenantContext tenantContext = TenantContext.getInstance()) {
            Long comId = company.getId();
            tenantContext.setTenantId(comId);
            //每次查10条公司数据处理
            Long startId = 0L;
            Integer batchSize = 20;
            List<GiftBalance> needDoList = giftBalanceRepository.listBatch(startId, batchSize, comId);
            do {
                if (needDoList != null && !needDoList.isEmpty()) {
                    // 业务逻辑,循环处理每一个公司的预定单超时问题
                    List<GiftBalance> updateList = new ArrayList<>();
                    for (GiftBalance giftBalance : needDoList) {
                        GiftBalance upItem = new GiftBalance();
                        upItem.setId(giftBalance.getId());
                        upItem.setBalance(giftBalance.getBalanceReset());
                        updateList.add(upItem);
                        log.warn("用户ID={}，赠送重置金额={}，重置前赠送金额={}，重置后赠送金额={}", giftBalance.getUserId(), giftBalance.getBalanceReset(), giftBalance.getBalance(), upItem.getBalance());
                    }
                    //每次更新一批
                    giftBalanceDomainService.clubResetGiftBalanceBatch(updateList);
                    startId = needDoList.get(needDoList.size() - 1).getId();
                    needDoList = giftBalanceRepository.listBatch(startId, batchSize, comId);
                }
            } while (needDoList != null && needDoList.size() > 0);
        }
    }

}
