package com.ipower.cloud.channelunify.jobs.timer;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.club.clubmanager.domain.entity.Company;
import com.club.clubmanager.domain.entity.Member;
import com.club.clubmanager.domain.enums.SmsBussType;
import com.club.clubmanager.domain.repository.CompanyRepository;
import com.club.clubmanager.domain.repository.MemberRepository;
import com.club.clubmanager.domain.repository.VipMemberRepository;
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

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 *
 * @Author : ranze
 * @create 2023/10/18
 * description : 用户会员卡失效提示
 */
@Component
@Slf4j
public class MemberBirthdayHandler {

    private final CompanyRepository companyRepository;
    private final VipMemberRepository vipMemberRepository;
    private final MemberRepository memberRepository;
    private final SmsMerchantSendDomainService smsMerchantSendDomainService;

    public MemberBirthdayHandler(CompanyRepository companyRepository, VipMemberRepository vipMemberRepository, MemberRepository memberRepository, SmsMerchantSendDomainService smsMerchantSendDomainService) {
        this.companyRepository = companyRepository;
        this.vipMemberRepository = vipMemberRepository;
        this.memberRepository = memberRepository;
        this.smsMerchantSendDomainService = smsMerchantSendDomainService;
    }

    @XxlJob("memberBirthday")
    public void jobHandler() {

        // 分片参数
        int shardIndex = XxlJobHelper.getShardIndex();
        int shardTotal = XxlJobHelper.getShardTotal();
        //指定参数,例子{"comId":"123","clubId":"123"}
        String jobParam = XxlJobHelper.getJobParam();

        XxlJobHelper.log("会员生日提醒xxl定时处理开始执行, 当前分片序号 = {}, 总分片数 = {},jobParam={}", shardIndex, shardTotal, jobParam);
        log.info("会员生日提醒xxl定时处理开始执行, 当前分片序号 = {}, 总分片数 = {},jobParam={}", shardIndex, shardTotal, jobParam);
//        if (shardIndex == 0){
//            // 现阶段这里只分片为0的服务实例去处理
            memberBirthday(shardIndex, shardTotal, jobParam);
//        }

        XxlJobHelper.log("会员生日提醒xxl定时处理执行完毕, 当前分片序号 = {}, 总分片数 = {},jobParam={}", shardIndex, shardTotal, jobParam);
        log.info("会员生日提醒xxl定时处理执行完毕, 当前分片序号 = {}, 总分片数 = {},jobParam={}", shardIndex, shardTotal, jobParam);
    }

    /**
     * 锁定桌位释放入口
     *
     * @param shardIndex
     * @param shardTotal
     * @param jobParam
     */
    private void memberBirthday(int shardIndex, int shardTotal, String jobParam) {

        if (StringUtils.isEmpty(jobParam)) {
            //无指定命令，执行全部公司
            allCompanyDoMemberBirthday(shardIndex, shardTotal);
        } else {
            //有指定命令，执行单个公司或门店
            TargetComAndClubCmd cmd = JobParamUtil.getParm(jobParam, TargetComAndClubCmd.class);
            if (cmd == null) {
                XxlJobHelper.log("会员生日提醒xxl定时处理失败,参数错误, 当前分片序号 = {}, 总分片数 = {},jobParam={}", shardIndex, shardTotal, jobParam);
                log.error("会员生日提醒xxl定时处理失败,参数错误, 当前分片序号 = {}, 总分片数 = {},jobParam={}", shardIndex, shardTotal, jobParam);
                return;
            }
            //验证命令参数正确性
            cmd.validate();
            //单处理公司
            if (cmd.doCom()) {
                Company company = companyRepository.getById(cmd.getComId());
                companyDoMemberBirthday(company);
            }else {
                //单处理门店
                XxlJobHelper.log("会员生日提醒xxl定时处理失败,参数错误：会员卡属于公司及产品，不可但门店处理, 当前分片序号 = {}, 总分片数 = {},jobParam={}", shardIndex, shardTotal, jobParam);
                log.error("会员生日提醒xxl定时处理失败,参数错误：会员卡属于公司及产品，不可但门店处理, 当前分片序号 = {}, 总分片数 = {},jobParam={}", shardIndex, shardTotal, jobParam);
            }
        }
    }


    /**
     * 全company处理
     *
     * @param shardIndex
     * @param shardTotal
     */
    private void allCompanyDoMemberBirthday(int shardIndex, int shardTotal) {
        //每次查10条公司数据处理
        Long comIdStart = 0L;
        Integer batchSize = 10;
        List<Company> comList = companyRepository.listComBatchShard(comIdStart, batchSize, shardIndex, shardTotal);
        do {
            if (comList != null && !comList.isEmpty()) {
                // 业务逻辑,循环处理每一个公司的预定单超时问题
                for (Company company : comList) {
                    //处理某个公司
                    companyDoMemberBirthday(company);
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
    private void companyDoMemberBirthday(Company company) {
        if (company == null) {
            XxlJobHelper.log("空公司不处理");
            log.warn("空公司不处理");
            return;
        }
        XxlJobHelper.log("开始处理公司会员生日提醒,公司={}", company.getName());
        log.debug("开始处理公司会员生日提醒,公司={}", company.getName());

        try (TenantContext tenantContext = TenantContext.getInstance()){
            tenantContext.setTenantId(company.getId());
            boolean startMark = true;
            long pageCurrent = 1L;
            long pageSize = 200L;
            while (startMark){
                Page<Member> page = new Page<>(pageCurrent, pageSize);
                LambdaQueryWrapper<Member> wrapper = Wrappers.lambdaQuery(Member.class)
                        .select(Member::getMobile, Member::getId, Member::getComId)
                        .last(" WHERE MONTH(birthday) = MONTH(CURDATE()) AND DAY(birthday) = DAY(CURDATE())");

                Page<Member> memberPage = memberRepository.page(page, wrapper);
                for (Member member : memberPage.getRecords()) {
                    SmsWrapperDTO smsWrapperDTO = SmsWrapperDTO.buildSmsWrapper(SmsBussType.MEMBER_BIRTHDAY,
                            Collections.singletonList(company.getName()),
                            ComAndClub.valueOf(member.getComId(),null),
                            member.getMobile());
                    smsMerchantSendDomainService.sendSms(smsWrapperDTO);
                }
                pageCurrent = pageCurrent + 1L;
                if (memberPage.getRecords().size() != pageSize){
                    startMark =false;
                }
            }
        }
    }
}
