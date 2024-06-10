package com.ipower.cloud.channelunify.jobs.timer;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.club.clubmanager.domain.entity.*;
import com.club.clubmanager.domain.enums.SmsBussType;
import com.club.clubmanager.domain.enums.SmsGroupType;
import com.club.clubmanager.domain.enums.SmsMerchResourceType;
import com.club.clubmanager.domain.repository.*;
import com.club.clubmanager.infra.config.TenantContext;
import com.google.common.base.Strings;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * @author: hechenggang
 * @date: 2023/11/21 15:06
 * @description:
 */
@Component
@Slf4j
public class SmsSendStatInitHandler {

    @Resource
    CompanyRepository companyRepository ;

    @Resource
    ClubRepository clubRepository;


    @Resource
    SmsSendStatRepository smsSendStatRepository;

    /**
     SmsSendStat smsSendStat = new SmsSendStat();
     smsSendStat.setStatMonth(month);
     smsSendStat.setClubId(clubId);
     smsSendStat.setComId(comId);
     smsSendStat.setTemplateId(tempId);
     smsSendStat.setSendNum(sendNum);
     smsSendStatRepository.save(smsSendStat);
     */
    @XxlJob("initNextMonth")
    public void jobHandler() {
        String jobParam = XxlJobHelper.getJobParam();
        initNextMonth(jobParam);
    }

    private void initNextMonth(String jobParam) {
        /**
         * 默认是初始下个月数据，
         * 项目首次上线初始化一次当月数据
         */
        String month = "";

        if (Strings.isNullOrEmpty(jobParam)){
            month  = LocalDate.now().plusMonths(1).format(DateTimeFormatter.ofPattern("yyyyMM"));
        }else {
            month  =  jobParam;
        }

        List<String>  companyTmpId = new ArrayList<>();
        List<String>  clubTmpId = new ArrayList<>();

        for (SmsBussType value : SmsBussType.values()) {
            if (value.getSmsGroupType().getSmsMerchResourceType()==SmsMerchResourceType.COM){
                companyTmpId.add(value.getTmplId());
            }else {
                clubTmpId.add(value.getTmplId());
            }
        }


        List<Company> companies = companyRepository.list();

        for (Company company : companies) {
            try (TenantContext tenantContext = TenantContext.getInstance()) {
                tenantContext.setTenantId(company.getId());
                for (String tempId: companyTmpId) {
                    LambdaQueryWrapper<SmsSendStat> queryWrapper = new LambdaQueryWrapper<>();
                    queryWrapper.eq(SmsSendStat::getStatMonth,month);
                    queryWrapper.eq(SmsSendStat::getTemplateId,tempId);
                    queryWrapper.eq(SmsSendStat::getComId,company.getId());
                    queryWrapper.isNull(SmsSendStat::getClubId);
                    SmsSendStat one = smsSendStatRepository.getOne(queryWrapper);
                    if (Objects.isNull(one)){
                        SmsSendStat smsSendStat = new SmsSendStat();
                        smsSendStat.setStatMonth(month);
                        smsSendStat.setClubId(null);
                        smsSendStat.setComId(company.getId());
                        smsSendStat.setTemplateId(tempId);
                        smsSendStat.setSendNum(0);
                        smsSendStatRepository.save(smsSendStat);
                    }
                }

                LambdaQueryWrapper<Club> clubLambdaQueryWrapper = new LambdaQueryWrapper<>();

                clubLambdaQueryWrapper.eq(Club::getComId,company.getId());
                List<Club> clubs = clubRepository.list(clubLambdaQueryWrapper);
                for (Club club : clubs) {
                    for (String tempId : clubTmpId) {
                        LambdaQueryWrapper<SmsSendStat> queryWrapper = new LambdaQueryWrapper<>();
                        queryWrapper.eq(SmsSendStat::getStatMonth,month);
                        queryWrapper.eq(SmsSendStat::getTemplateId,tempId);
                        queryWrapper.eq(SmsSendStat::getClubId,club.getId());
                        SmsSendStat one = smsSendStatRepository.getOne(queryWrapper);
                        if (Objects.isNull(one)){
                            SmsSendStat smsSendStat = new SmsSendStat();
                            smsSendStat.setStatMonth(month);
                            smsSendStat.setClubId(club.getId());
                            smsSendStat.setComId(club.getComId());
                            smsSendStat.setTemplateId(tempId);
                            smsSendStat.setSendNum(0);
                            smsSendStatRepository.save(smsSendStat);
                        }
                    }
                }
            }
        }
    }
}
