package com.ipower.cloud.channelunify.infra.persistence.mapper;

import com.club.clubmanager.domain.entity.FundAccount;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 账户表 Mapper 接口
 * </p>
 *
 * @author ranze
 * @since 2023-01-12
 */
@Mapper
public interface AccountMapper extends BaseMapper<FundAccount> {

}
