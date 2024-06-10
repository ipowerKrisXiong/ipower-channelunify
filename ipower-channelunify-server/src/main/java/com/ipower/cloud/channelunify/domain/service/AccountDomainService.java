package com.ipower.cloud.channelunify.domain.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.club.clubmanager.application.dto.walletcard.CardPayAmountDTO;
import com.club.clubmanager.domain.entity.FundAccount;
import com.club.clubmanager.domain.entity.FundAccountCard;
import com.club.clubmanager.domain.entity.FundAccountRecord;
import com.club.clubmanager.domain.enums.AccountType;
import com.club.clubmanager.domain.repository.AccountCardRepository;
import com.club.clubmanager.domain.repository.AccountRecordRepository;
import com.club.clubmanager.domain.repository.AccountRepository;
import com.club.clubmanager.domain.service.dto.AccountChargeDTO;
import com.club.clubmanager.domain.service.dto.AccountComputePayDTO;
import com.club.clubmanager.domain.service.dto.AccountPayDTO;
import com.club.clubmanager.infra.util.PriceUtil;
import com.mars.commonutils.core.utils.SnowflakeIdWorker;
import com.mars.service.core.exception.BusinessException;
import com.mars.service.core.exception.ExceptionCommonDefineEnum;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Objects;

@Service
public class AccountDomainService {

    private final AccountRepository accountRepository;
    private final AccountCardRepository accountCardRepository;
    private final AccountRecordRepository accountRecordRepository;

    public AccountDomainService(AccountRepository accountRepository, AccountCardRepository accountCardRepository, AccountRecordRepository accountRecordRepository) {
        this.accountRepository = accountRepository;
        this.accountCardRepository = accountCardRepository;
        this.accountRecordRepository = accountRecordRepository;
    }

    /**
     * 保存并获取
     * @param cardId 卡ID 必填
     * @param memberId
     * @return
     */
    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public FundAccount saveAndGet(Long cardId, Long memberId, AccountType accountType){
        LambdaQueryWrapper<FundAccountCard> queryWrapper = Wrappers.lambdaQuery(FundAccountCard.class)
                .eq(FundAccountCard::getCardId, cardId)
                .eq(FundAccountCard::getType, accountType.getType())
                .eq(Objects.nonNull(memberId), FundAccountCard::getMemberId, memberId);
        FundAccountCard fundAccountCard = accountCardRepository.getOne(queryWrapper);

        FundAccount fundAccount;
        if (Objects.isNull(fundAccountCard)){
            fundAccount = new FundAccount();
            fundAccount.setId(SnowflakeIdWorker.getInstance().nextId());
            fundAccount.setBalance(0L);
            fundAccount.setGiftBalance(0L);
            fundAccount.setType(accountType.getType());
            accountRepository.save(fundAccount);
            fundAccountCard = FundAccountCard.create(cardId, memberId, fundAccount.getId(), accountType.getType());
            accountCardRepository.save(fundAccountCard);
        }
        fundAccount = accountRepository.getById(fundAccountCard.getAccountId());
        return fundAccount;
    }


    /**
     * 增加金额
     * @param chargeDTO
     * @return
     */
    public FundAccount increase(AccountChargeDTO chargeDTO){
        LambdaUpdateWrapper<FundAccount> updateWrapper = Wrappers.lambdaUpdate(FundAccount.class)
                .setSql("balance = balance + " + chargeDTO.getChargeAmount())
                .setSql("gift_balance = gift_balance + " + chargeDTO.getGiftAmount())
                .eq(FundAccount::getId, chargeDTO.getAccountId());
        boolean update = accountRepository.update(updateWrapper);
        if (!update){
            throw new BusinessException(ExceptionCommonDefineEnum.COMMON_FAIL, "充值失败");
        }
        FundAccount account = accountRepository.getById(chargeDTO.getAccountId());
        createRecord(account, chargeDTO.getChargeAmount(), chargeDTO.getGiftAmount());
        return account;
    }

    /**
     * 减少金额
     * @param payDTO
     */
    public FundAccount reduce(AccountPayDTO payDTO){
        LambdaUpdateWrapper<FundAccount> updateWrapper = Wrappers.lambdaUpdate(FundAccount.class)
                .setSql("balance = balance - " + payDTO.getAmount())
                .setSql("gift_balance = gift_balance - " + payDTO.getGiftAmount())
                .ge(FundAccount::getBalance, payDTO.getAmount())
                .ge(FundAccount::getGiftBalance, payDTO.getGiftAmount())
                .eq(FundAccount::getId, payDTO.getAccountId());
        boolean update = accountRepository.update(updateWrapper);
        if (!update){
            throw new BusinessException(ExceptionCommonDefineEnum.COMMON_FAIL, "扣款失败");
        }
        FundAccount account = accountRepository.getById(payDTO.getAccountId());
        createRecord(account, payDTO.getAmount() * -1, payDTO.getGiftAmount() * -1);
        return account;
    }


    /**
     * 流水
     * @param account
     * @param balance
     * @param giftBalance
     */
    private Long createRecord(FundAccount account, Long balance, Long giftBalance){
        FundAccountRecord record = new FundAccountRecord();
        record.setAccountId(account.getId());
        record.setOldBalance(account.getBalance() - balance);
        record.setBalance(balance);
        record.setNewBalance(account.getBalance());
        record.setOldGiftBalance(account.getGiftBalance() - giftBalance);
        record.setGiftBalance(giftBalance);
        record.setNewGiftBalance(account.getGiftBalance());
        accountRecordRepository.save(record);
        return record.getId();
    }


    /**
     * 基于订单总金额计算实际扣款的账户本金和账户赠送金额
     * @param account
     * @param amount
     * @return
     */
    public CardPayAmountDTO computePayAmount(AccountComputePayDTO account, Long amount){
        BigDecimal balance = new BigDecimal(account.getBalance());
        BigDecimal giftBalance = new BigDecimal(account.getGiftBalance());
        BigDecimal totalAmount = new BigDecimal(amount);
        BigDecimal totalBalance = balance.add(giftBalance);
        // 扣款本金
        BigDecimal payAmount;
        // 扣款赠送金额
        BigDecimal payGiftAmount;
        if (totalBalance.compareTo(totalAmount) < 0){
            // 余额小于扣款金额则直接返回
            throw new BusinessException(ExceptionCommonDefineEnum.COMMON_FAIL, "余额不足");
        }else if (totalBalance.compareTo(totalAmount) == 0){
            // 余额等于扣款金额则直接全部扣款
            payAmount = balance;
            payGiftAmount = giftBalance;
        }else {
            // 余额大于扣款金额则按比例扣款
            payAmount = PriceUtil.floatToIntegerRoundHalfUp(totalAmount.multiply(PriceUtil.divideRatioForCompute(balance,totalBalance)));

            if (payAmount.compareTo(balance) > 0){
                // 如果比例的扣款本金大于余额本金，则直接余额本金扣完
                payAmount = balance;
            }
            payGiftAmount = totalAmount.subtract(payAmount);
            if (payGiftAmount.compareTo(giftBalance) > 0){
                // 如果赠送扣款金额大于赠送余额，则直接扣完余额，重新计算扣款本金
                payGiftAmount = giftBalance;
                payAmount = totalAmount.subtract(payGiftAmount);
            }
        }
        return CardPayAmountDTO.builder()
                .amount(payAmount.longValue())
                .giftAmount(payGiftAmount.longValue())
                .build();
    }

}
