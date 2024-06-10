package com.ipower.cloud.channelunify.infra.repository.impl;

import com.club.clubmanager.domain.entity.FundAccount;
import com.club.clubmanager.infra.persistence.mapper.AccountMapper;
import com.club.clubmanager.domain.repository.AccountRepository;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 账户表 服务实现类
 * </p>
 *
 * @author ranze
 * @since 2023-01-12
 */
@Service
public class AccountRepositoryImpl extends ServiceImpl<AccountMapper, FundAccount> implements AccountRepository {

}
