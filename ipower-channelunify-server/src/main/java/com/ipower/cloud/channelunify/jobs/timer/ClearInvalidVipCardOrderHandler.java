package com.ipower.cloud.channelunify.jobs.timer;

import com.club.clubmanager.application.service.VipMemberOrderService;
import com.club.clubmanager.domain.entity.Company;
import com.club.clubmanager.domain.repository.CompanyRepository;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @Author ranze
 * @create 2022/11/28 11:55
 */
@Component
@Slf4j
public class ClearInvalidVipCardOrderHandler {

    @Autowired
    private VipMemberOrderService vipMemberOrderService;
    @Autowired
    private CompanyRepository companyRepository;


    @XxlJob("clearInvalidVipCardOrder")
    public void clearInvalidOrderJobHandler() {
        try {
            // 分片参数 按分片数来对com进行取模后进行处理
            int shardIndex = XxlJobHelper.getShardIndex();
            int shardTotal = XxlJobHelper.getShardTotal();

            clearInvalidAllOrder(shardIndex, shardTotal);
            log.info("clearInvalidVipCardOrder --success，jobId={}.params={}", XxlJobHelper.getJobId(), XxlJobHelper.getJobParam());
            XxlJobHelper.log("clearInvalidVipCardOrder --success，jobId={}.params={}", XxlJobHelper.getJobId(), XxlJobHelper.getJobParam());
        } catch (Exception e) {
            log.error("clearInvalidVipCardOrder --fail，jobId={}.params={}.errorMsg={}", XxlJobHelper.getJobId(), XxlJobHelper.getJobParam(), e);
            XxlJobHelper.log("clearInvalidVipCardOrder --fail，jobId={}.params={}", XxlJobHelper.getJobId(), XxlJobHelper.getJobParam());
            XxlJobHelper.handleFail("失败");
            throw e;
        }
    }


    /**
     * 清理所有公司
     *
     * @param shardIndex
     * @param shardTotal
     */
    private void clearInvalidAllOrder(int shardIndex, int shardTotal) {
        //每次查10条公司数据处理
        Long comIdStart = 0L;
        Integer batchSize = 10;
        List<Company> comList = companyRepository.listComBatchShard(comIdStart, batchSize, shardIndex, shardTotal);
        do {
            if (comList != null && !comList.isEmpty()) {
                // 业务逻辑,循环处理每一个公司的预定单超时问题
                for (Company company : comList) {
                    vipMemberOrderService.clearInvalidOrder(company);
                }
                //最后一个公司ID设置为下一个开始下标
                comIdStart = comList.get(comList.size() - 1).getId();
                comList = companyRepository.listComBatchShard(comIdStart, batchSize, shardIndex, shardTotal);
            }
        } while (comList != null && comList.size() > 0);
    }
}
