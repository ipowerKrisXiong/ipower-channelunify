package com.ipower.cloud.channelunify.jobs.timer;

import com.club.clubmanager.domain.entity.Club;
import com.club.clubmanager.domain.entity.Company;
import com.club.clubmanager.domain.enums.SmsBussType;
import com.club.clubmanager.domain.repository.ClubRepository;
import com.club.clubmanager.domain.repository.CompanyRepository;
import com.club.clubmanager.domain.repository.WineDepositRecordRepository;
import com.club.clubmanager.domain.repository.dto.AdventRemainDTO;
import com.club.clubmanager.domain.service.SmsMerchantSendDomainService;
import com.club.clubmanager.domain.service.dto.SmsWrapperDTO;
import com.club.clubmanager.domain.types.ComAndClub;
import com.club.clubmanager.infra.config.TenantContext;
import com.club.clubmanager.infra.jobs.timer.cmd.TargetComAndClubCmd;
import com.club.clubmanager.infra.jobs.timer.util.JobParamUtil;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author: hechenggang
 * @date: 2023/9/27 16:51
 * @description:
 */
@Slf4j
@Component
public class WineAdventRemainHandler {

    @Resource
    WineDepositRecordRepository wineDepositRecordRepository;

    @Resource
    CompanyRepository companyRepository;

    @Resource
    ClubRepository clubRepository;

    @Resource
    SmsMerchantSendDomainService smsMerchantSendDomainService;

    /**
     * 每天中午两点十八分处理.
     * 存酒过期时间2023 10月25日
     * 临期提醒天数是5天
     * 计算逻辑 25-5 = 20日  20 日 下午两点十八分执行定时任务发送短信提醒.相当于推迟提醒了
     */
    @XxlJob("wineAdventRemain")
    public void jobHandler() {
        // 分片参数 按分片数来对com进行取模后进行处理
        int shardIndex = XxlJobHelper.getShardIndex();
        int shardTotal = XxlJobHelper.getShardTotal();
        //指定参数,例子{"comId":"123","clubId":"123"}
        String jobParam = XxlJobHelper.getJobParam();

        XxlJobHelper.log("定时存酒临期处理 开始执行, 当前分片序号 = {}, 总分片数 = {},jobParam={}", shardIndex, shardTotal, jobParam);
        log.info("定时存酒临期处理 开始执行, 当前分片序号 = {}, 总分片数 = {},jobParam={}", shardIndex, shardTotal, jobParam);
        //超时释放
        doAdventRemain(shardIndex, shardTotal, jobParam);
    }

    /**
     * @param shardIndex
     * @param shardTotal
     * @param jobParam
     */
    private void doAdventRemain(int shardIndex, int shardTotal, String jobParam) {

        //无指定命令，执行全部公司
        if (StringUtils.isEmpty(jobParam)) {
            allCompanyDoAdventRemain(shardIndex, shardTotal);
            //有指定命令，执行单个公司或门店
        } else {
            TargetComAndClubCmd cmd = JobParamUtil.getParm(jobParam, TargetComAndClubCmd.class);
            if (cmd == null) {
                return;
            }
            //验证命令参数正确性
            cmd.validate();
            //单处理公司
            if (cmd.doCom()) {
                Company company = companyRepository.getById(cmd.getComId());
                comAdventRemain(company);
                //单处理门店
            } else if (cmd.doClub()) {
                Club club = clubRepository.getById(cmd.getClubId());
                clubAdventRemain(club);
            }
        }

    }

    private void clubAdventRemain(Club club) {

        try (TenantContext tenantContext = TenantContext.getInstance()) {
            tenantContext.setTenantId(club.getComId());

            List<AdventRemainDTO> adventRemainList = wineDepositRecordRepository.getAdventRemainList(club.getComId(), club.getId(), LocalDate.now());

            // 去重,重复手机号只发送一次.
            //List<AdventRemainDTO> collect = adventRemainList.stream().distinct().collect(Collectors.toList());
            // TODO: 2023/11/8 腾讯短信限流配置为每30s发送5条短信，
            // 这儿批处理极限情况会出现the number of SMS messages sent from a single mobile number within 30 seconds exceeds the upper limit
            // 可能出现短信发送失败的情况。
            for (AdventRemainDTO adventRemainDTO : adventRemainList) {
                ComAndClub comAndClub = new ComAndClub();
                comAndClub.setClubId(club.getId());
                comAndClub.setComId(club.getComId());
                List<String> params = new ArrayList<>();
                params.add(adventRemainDTO.getExpireTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                SmsWrapperDTO smsWrapperDTO =
                        SmsWrapperDTO.buildSmsWrapper(SmsBussType.WINE_EXPIRE_REMAIN,params,
                                comAndClub,adventRemainDTO.getMemberPhone());
                smsMerchantSendDomainService.sendSms(smsWrapperDTO);
            }

        }


    }

    private void comAdventRemain(Company company) {
        if (company == null) {
            XxlJobHelper.log("空公司不处理");
            log.warn("空公司不处理");
            return;
        }
        //一般来说club数量不会超过10个
        List<Club> clubList = clubRepository.listByComId(company.getId());
        for (Club club : clubList) {
            try {
                clubAdventRemain(club);
            } catch (Exception e) {
                XxlJobHelper.log("处理存酒临期提失败! club={}", club, e);
                log.error("处理存酒临期提失败! club={}", club, e);
            }
        }
    }

    private void allCompanyDoAdventRemain(int shardIndex, int shardTotal) {
        Long comIdStart = 0L;
        Integer batchSize = 10;
        List<Company> comList = companyRepository.listComBatchShard(comIdStart, batchSize, shardIndex, shardTotal);
        do {
            if (comList != null && !comList.isEmpty()) {
                // 业务逻辑,循环处理每一个公司的预定单超时问题
                for (Company company : comList) {
                    //处理某个公司
                    comAdventRemain(company);
                }
                //最后一个公司ID设置为下一个开始下标
                comIdStart = comList.get(comList.size() - 1).getId();
                comList = companyRepository.listComBatchShard(comIdStart, batchSize, shardIndex, shardTotal);
            }
        } while (comList != null && comList.size() > 0);


    }


}
