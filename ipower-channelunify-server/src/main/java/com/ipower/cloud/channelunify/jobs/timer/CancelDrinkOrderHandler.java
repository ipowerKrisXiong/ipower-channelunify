package com.ipower.cloud.channelunify.jobs.timer;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.club.clubmanager.domain.entity.Club;
import com.club.clubmanager.domain.entity.Company;
import com.club.clubmanager.domain.entity.DrinkOrder;
import com.club.clubmanager.domain.enums.DrinkOrderStatusEnum;
import com.club.clubmanager.domain.repository.ClubRepository;
import com.club.clubmanager.domain.repository.CompanyRepository;
import com.club.clubmanager.domain.repository.DrinkOrderRepository;
import com.club.clubmanager.domain.service.DrinkOrderDomainService;
import com.club.clubmanager.infra.config.TenantContext;
import com.club.clubmanager.infra.jobs.timer.cmd.TargetComAndClubCmd;
import com.club.clubmanager.infra.jobs.timer.util.JobParamUtil;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

/**
 * @author hk
 * @date 2022/11/29
 */
@Component
@Slf4j
public class CancelDrinkOrderHandler {
    private final DrinkOrderRepository drinkOrderRepository;
    private final DrinkOrderDomainService drinkOrderDomainService;
    private final CompanyRepository companyRepository;
    private final ClubRepository clubRepository;

    public CancelDrinkOrderHandler(DrinkOrderRepository drinkOrderRepository,
                                   DrinkOrderDomainService drinkOrderDomainService, CompanyRepository companyRepository,
                                   ClubRepository clubRepository) {
        this.drinkOrderRepository = drinkOrderRepository;
        this.drinkOrderDomainService = drinkOrderDomainService;
        this.companyRepository = companyRepository;
        this.clubRepository = clubRepository;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * 取消酒水订单
     */
    @XxlJob("cancelDrinkOrder")
    public void cancelJobHander() {

        // 分片参数 按分片数来对com进行取模后进行处理
        int shardIndex = XxlJobHelper.getShardIndex();
        int shardTotal = XxlJobHelper.getShardTotal();
        //指定参数,例子{"comId":"123","clubId":"123"}
        String jobParam = XxlJobHelper.getJobParam();

        XxlJobHelper.log("取消酒水订单定时处理开始执行, 当前分片序号 = {}, 总分片数 = {},jobParam={}", shardIndex, shardTotal, jobParam);
        log.info("取消酒水订单定时处理开始执行, 当前分片序号 = {}, 总分片数 = {},jobParam={}", shardIndex, shardTotal, jobParam);

        //清理酒水订单
        cancelDrinkOrder(shardIndex, shardTotal, jobParam);

        XxlJobHelper.log("取消酒水订单定时处理执行完毕, 当前分片序号 = {}, 总分片数 = {},jobParam={}", shardIndex, shardTotal, jobParam);
        log.info("取消酒水订单定时处理执行完毕, 当前分片序号 = {}, 总分片数 = {},jobParam={}", shardIndex, shardTotal, jobParam);
    }

    /**
     * 取消酒水订单
     *
     * @param shardIndex
     * @param shardTotal
     * @param jobParam
     */
    private void cancelDrinkOrder(int shardIndex, int shardTotal, String jobParam) {
        //无指定命令，执行全部公司
        if (StringUtils.isEmpty(jobParam)) {
            allCompanyCancelDrinkOrder(shardIndex, shardTotal);
            //有指定命令，执行单个公司或门店
        } else {
            TargetComAndClubCmd cmd = JobParamUtil.getParm(jobParam, TargetComAndClubCmd.class);
            if (cmd == null) {
                XxlJobHelper.log("取消酒水订单定时处理失败,参数错误, 当前分片序号 = {}, 总分片数 = {},jobParam={}", shardIndex, shardTotal, jobParam);
                log.error("取消酒水订单定时处理失败,参数错误, 当前分片序号 = {}, 总分片数 = {},jobParam={}", shardIndex, shardTotal, jobParam);
                return;
            }
            //验证命令参数正确性
            cmd.validate();
            //单处理公司
            if (cmd.doCom()) {
                Company company = companyRepository.getById(cmd.getComId());
                companyCancelDrinkOrder(company);
                //单处理门店
            } else if (cmd.doClub()) {
                Club club = clubRepository.getById(cmd.getClubId());
                clubCancelDrinkOrder(club);
            }
        }
    }

    /**
     * 全company处理
     *
     * @param shardIndex
     * @param shardTotal
     */
    private void allCompanyCancelDrinkOrder(int shardIndex, int shardTotal) {
        //每次查10条公司数据处理
        Long comIdStart = 0L;
        Integer batchSize = 10;
        List<Company> comList = companyRepository.listComBatchShard(comIdStart, batchSize, shardIndex, shardTotal);
        do {
            if (comList != null && !comList.isEmpty()) {
                // 业务逻辑,循环处理每一个公司的预定单超时问题
                for (Company company : comList) {
                    //处理某个公司
                    companyCancelDrinkOrder(company);
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
    private void companyCancelDrinkOrder(Company company) {
        if (Objects.isNull(company)) {
            XxlJobHelper.log("空公司不处理");
            log.warn("空公司不处理");
            return;
        }
        XxlJobHelper.log("开始处理公司取消酒水订单,公司={}", company.getName());
        log.debug("开始处理公司取消酒水订单,公司={}", company.getName());
        //一般来说club数量不会超过10个
        List<Club> clubList = clubRepository.listByComId(company.getId());
        for (Club club : clubList) {
            clubCancelDrinkOrder(club);
        }
    }

    /**
     * 单club处理
     *
     * @param club
     */
    private void clubCancelDrinkOrder(Club club) {

        if (club == null) {
            XxlJobHelper.log("空门店不处理");
            log.warn("空门店不处理");
            return;
        }

        XxlJobHelper.log("开始处理门店取消酒水订单,门店={}", club.getName());
        log.debug("开始处理门店取消酒水订单,门店={}", club.getName());

        try (TenantContext tenantContext = TenantContext.getInstance()) {
            tenantContext.setTenantId(club.getComId());
            // 取消今天之前的超时订单
            List<Long> orderIds = drinkOrderRepository.listObjs(Wrappers.lambdaQuery(DrinkOrder.class)
                            .select(DrinkOrder::getId)
                            .lt(DrinkOrder::getExpirationTime, LocalDate.now().atStartOfDay())
                            .eq(DrinkOrder::getOrderStatus, DrinkOrderStatusEnum.UNPAID)
                            .eq(DrinkOrder::getClubId, club.getId())
                    , o -> (Long) o);
            if (CollectionUtils.isNotEmpty(orderIds)) {
                drinkOrderDomainService.cancel(orderIds, "超时取消");
                log.debug("取消酒水订单成功：门店={},数量={}", club.getName(), orderIds.size());
                XxlJobHelper.log("取消酒水订单成功：门店={},数量={}", club.getName(), orderIds.size());
            } else {
                log.debug("没有酒水订单需要取消");
                XxlJobHelper.log("没有酒水订单需要取消,门店={}", club.getName());
            }

        }

    }
}
