package com.ipower.cloud.channelunify.infra.sms.impl;

import com.alphaclub.msgcenter.client.dto.SmsDTO;
import com.alphaclub.msgcenter.client.dto.SmsSendRsultStatis;
import com.alphaclub.msgcenter.client.dto.VerifyCodeSendReq;
import com.alphaclub.msgcenter.client.ws.MsgCenterClient;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.club.clubmanager.domain.entity.*;
import com.club.clubmanager.domain.enums.SmsGroupType;
import com.club.clubmanager.domain.enums.SmsMerchResourceType;
import com.club.clubmanager.domain.repository.SmsMerchantConfigRepository;
import com.club.clubmanager.domain.repository.SmsMerchantResourceRepository;
import com.club.clubmanager.domain.repository.SmsSendStatRepository;
import com.club.clubmanager.domain.repository.dto.SmsResidueNumAndMerchantNameDTO;
import com.club.clubmanager.domain.service.cache.SmsMerchantConfigCacheService;
import com.club.clubmanager.domain.sms.SmsProcess;
import com.club.clubmanager.domain.types.ComAndClub;
import com.mars.service.core.exception.BusinessException;
import com.mars.service.core.exception.ExceptionCommonDefineEnum;
import com.mars.service.core.model.HttpResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * @author: hechenggang
 * @date: 2023/9/22 14:42
 * @description:
 */
@Slf4j
@Component
public class SmsProcessImpl implements SmsProcess {
    @Resource
    MsgCenterClient msgCenterClient;

    @Resource
    SmsMerchantResourceRepository smsMerchantResourceRepository;

    @Resource
    SmsMerchantConfigCacheService smsMerchantConfigCacheService;

    @Resource
    SmsMerchantConfigRepository smsMerchantConfigRepository;

    @Resource
    SmsSendStatRepository smsSendStatRepository;


    /**
     * @param comId
     * @param clubId
     * @param deductNum 扣除商户短信条数
     */
    @Override
    public Integer deductMerchantSmsNum(Long comId, Long clubId, Integer deductNum) {
        return smsMerchantResourceRepository.deductMerchantSmsNum(comId, clubId, deductNum);
    }

    /**
     * @param clubId
     * @param comId
     * @param restoreNum 恢复商户短信条数
     */
    @Override
    public Integer restoreMerchantSmsNum(Long comId, Long clubId, Integer restoreNum) {
        return smsMerchantResourceRepository.restoreMerchantSmsNum(comId, clubId, restoreNum);
    }

    /**
     * 发送短信
     *
     * @return 返回发送成功的短信
     */
    @Override
    public SmsSendRsultStatis sendSms(SmsDTO smsDTO) {
        try {
            HttpResult<SmsSendRsultStatis> httpResult = msgCenterClient.sendSmsSync(smsDTO);
            if (httpResult.isSuccess()) {
                return httpResult.getData();
            } else {
                throw new BusinessException(ExceptionCommonDefineEnum.COMMON_FAIL, httpResult.getMsg());
            }
        } catch (Exception e) {
            log.error("发送短信失败", e);
            throw new BusinessException(ExceptionCommonDefineEnum.COMMON_FAIL, "发送短信失败");
        }
    }

    /**
     * 发送验证码
     *
     * @return 返回验证码
     */
    @Override
    public String sendVerifyCode(VerifyCodeSendReq req) {
        try {
            HttpResult<String> httpResult = msgCenterClient.sendVerifiCode(req);
            if (httpResult.isSuccess()) {
                return httpResult.getData();
            } else {
                log.error("验证码发送失败,param={},res={}", req, httpResult);
                return "";
            }
        } catch (Exception e) {
            log.error("验证码发送失败,param={}", req, e);
            return "";
        }
    }

    /**
     * @param clubId       检查商户短信发送状态
     * @param comId
     * @param smsGroupType
     */
    @Override
    public Boolean checkMerchSmsConfOpenStatus(Long comId, Long clubId, SmsGroupType smsGroupType) {
        if (smsGroupType.getDefaultSms()) {
            return true;
        }
        Boolean openStatus = smsMerchantConfigCacheService.getOpenStatus(comId, clubId, smsGroupType.getCode());
        if (openStatus == null) {
            LambdaQueryWrapper<SmsMerchantConfig> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(SmsMerchantConfig::getSmsGroupType, smsGroupType.getCode());
            if (clubId == null) {
                queryWrapper.isNull(SmsMerchantConfig::getClubId);
            } else {
                queryWrapper.eq(SmsMerchantConfig::getClubId, clubId);
            }
            SmsMerchantConfig smsMerchantConfig = smsMerchantConfigRepository.getOne(queryWrapper);
            if (smsMerchantConfig != null) {
                smsMerchantConfigCacheService.setOpenStatus(comId, clubId, smsGroupType.getCode(), smsMerchantConfig.getOpenStatus());
                return smsMerchantConfig.getOpenStatus();

            } else {
                return false;
            }

        }
        return openStatus;
    }

    @Override
    public SmsResidueNumAndMerchantNameDTO getResidueNum(SmsMerchResourceType merchSmsResourceType, ComAndClub comAndClub) {

        SmsResidueNumAndMerchantNameDTO dto = new SmsResidueNumAndMerchantNameDTO();
        LambdaQueryWrapper<SmsMerchantResource> queryWrapper = new LambdaQueryWrapper<>();

        //公司短信资源
        if (SmsMerchResourceType.COM == merchSmsResourceType) {
            queryWrapper.isNull(SmsMerchantResource::getClubId);
            queryWrapper.eq(SmsMerchantResource::getComId, comAndClub.getComId());
            SmsMerchantResource one = smsMerchantResourceRepository.getOne(queryWrapper);
            if (one == null) {
                return null;
            } else {
                dto.setResidueNum(one.getResidueNum());
                dto.setMerchantName(one.getCompanyName());
                dto.setComName(one.getCompanyName());
            }
            //门店短信资源
        } else if (SmsMerchResourceType.CLUB == merchSmsResourceType) {
            queryWrapper.eq(SmsMerchantResource::getComId, comAndClub.getComId());
            queryWrapper.eq(SmsMerchantResource::getClubId, comAndClub.getClubId());
            SmsMerchantResource one = smsMerchantResourceRepository.getOne(queryWrapper);
            if (one == null) {
                return null;
            } else {
                dto.setResidueNum(one.getResidueNum());
                dto.setMerchantName(one.getClubName());
                dto.setComName(one.getCompanyName());
            }
        }

        return dto;
    }

    /**
     * @param comId
     * @param clubId
     * @param sendNum
     * @param tempId
     */
    @Override
    public void smsStat(Long comId, Long clubId, Integer sendNum, String tempId) {

        if (sendNum <= 0) {
            return;
        }

        String month = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
        LambdaUpdateWrapper<SmsSendStat> updateWrapper = new UpdateWrapper<SmsSendStat>()
                .lambda()
                .eq(SmsSendStat::getStatMonth,month)
                .eq(SmsSendStat::getTemplateId,tempId)//库存必须大于请求数
                .eq(SmsSendStat::getComId,comId)
                .isNull(Objects.isNull(clubId),SmsSendStat::getClubId)
                .eq(Objects.nonNull(clubId),SmsSendStat::getClubId,clubId)
                .setSql("send_num = send_num + " + sendNum);
        smsSendStatRepository.update(updateWrapper);
    }
}
