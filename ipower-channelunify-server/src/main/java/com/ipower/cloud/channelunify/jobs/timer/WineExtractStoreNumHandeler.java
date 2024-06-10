package com.ipower.cloud.channelunify.jobs.timer;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.club.clubmanager.domain.entity.Company;
import com.club.clubmanager.domain.entity.WineDepositRecord;
import com.club.clubmanager.domain.entity.WineExtractRecord;
import com.club.clubmanager.domain.repository.CompanyRepository;
import com.club.clubmanager.domain.repository.WineDepositRecordRepository;
import com.club.clubmanager.domain.repository.WineExtractRecordRepository;
import com.club.clubmanager.infra.config.TenantContext;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * 升级到1.9.0的时候执行一次升级更新取酒记录StoreNum数据
 * @author: hechenggang
 * @date: 2023/7/19 14:49
 * @description:
 */
@Component
@Slf4j
public class WineExtractStoreNumHandeler {

    private static  int count  =  0 ;

    @Resource
    WineDepositRecordRepository wineDepositRecordRepository ;

    @Resource
    WineExtractRecordRepository wineExtractRecordRepository;

    @Resource
    CompanyRepository companyRepository;

    @XxlJob("handlerStoreNum")
    public void jobHandler() {
        // 分片参数 按分片数来对com进行取模后进行处理
        int shardIndex = XxlJobHelper.getShardIndex();
        int shardTotal = XxlJobHelper.getShardTotal();
        //指定参数,例子{"comId":"123","clubId":"123"}
        String jobParam = XxlJobHelper.getJobParam();

        if(shardIndex!=0){
            XxlJobHelper.log("取酒时剩余库存量处理定时任务 开始执行, 非分片0，只有分片0才需要执行, 当前分片序号= {}, 总分片数 = {},jobParam={}", shardIndex, shardTotal, jobParam);
            log.info("取酒时剩余库存量处理定时任务 开始执行, 非分片0，只有分片0才需要执行, 当前分片序号= {}, 总分片数 = {},jobParam={}", shardIndex, shardTotal, jobParam);
            return;
        }

        XxlJobHelper.log("取酒时剩余库存量处理定时任务 开始执行, 当前分片序号 = {}, 总分片数 = {},jobParam={}", shardIndex, shardTotal, jobParam);
        count++;
        log.info("取酒时剩余库存量处理定时任务 开始执行, 当前分片序号 = {}, 总分片数 = {},jobParam={}", shardIndex, shardTotal, jobParam);
        log.info("handlerStoreNum 定时任务执行次数:"+count);

        List<Company> list = companyRepository.list();
        for (Company company : list) {
            try (TenantContext tenantContext = TenantContext.getInstance()) {
                tenantContext.setTenantId(company.getId());
                LambdaQueryWrapper<WineDepositRecord> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.gt(WineDepositRecord::getExtractNum,0);
                queryWrapper.eq(WineDepositRecord::getComId,company.getId());
                List<WineDepositRecord> wineDepositRecords = wineDepositRecordRepository.list(queryWrapper);
                //遍历所有有取过酒的存酒记录，更新他们的取酒记录中的StoreNum
                for (WineDepositRecord wineDepositRecord : wineDepositRecords) {
                    List<WineExtractRecord> ls = new ArrayList<>();
                    Integer depositNum = wineDepositRecord.getDepositNum();
                    LambdaQueryWrapper<WineExtractRecord> extractRecordLambdaQueryWrapper = new LambdaQueryWrapper<>();
                    extractRecordLambdaQueryWrapper.eq(WineExtractRecord::getDepositRecordId,wineDepositRecord.getId());
                    extractRecordLambdaQueryWrapper.orderByAsc(WineExtractRecord::getCreateTime);
                    List<WineExtractRecord> wineExtractRecords = wineExtractRecordRepository.list(extractRecordLambdaQueryWrapper);
                    for (WineExtractRecord wineExtractRecord : wineExtractRecords) {
                        depositNum = depositNum-wineExtractRecord.getExtractNum();
                        wineExtractRecord.setStoreNum(depositNum);
                        ls.add(wineExtractRecord);
                    }
                    wineExtractRecordRepository.saveOrUpdateBatch(ls);
                    log.info("handlerStoreNum 更新完存酒记录的取酒记录storeNum,wineDepositRecordId="+wineDepositRecord.getId());
                }
            }
        }

        log.info("取酒时剩余库存量处理定时任务 执行完成, 总执行次数:"+count);

    }
}
