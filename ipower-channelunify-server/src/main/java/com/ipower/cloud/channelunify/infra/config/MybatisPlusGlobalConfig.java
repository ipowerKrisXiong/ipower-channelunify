package com.ipower.cloud.channelunify.infra.config;

import com.baomidou.mybatisplus.core.config.GlobalConfig;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.handler.TenantLineHandler;
import com.baomidou.mybatisplus.extension.plugins.inner.BlockAttackInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.TenantLineInnerInterceptor;
import com.mars.service.core.exception.BusinessException;
import com.mars.service.core.exception.ExceptionCommonDefineEnum;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
//不用MapperScan，@Mapper自己知道从工程根目录里面扫出来。代码入口为：AutoConfiguredMapperScannerRegistrar是自动扫描的入口类
//@MapperScan({"com.club.clubmanager.infra.persistence.mapper"})
public class MybatisPlusGlobalConfig {

    @Bean
    public GlobalConfig globalConfig() {
        GlobalConfig conf = new GlobalConfig();
        //自定义assigend id生成器
        conf.setIdentifierGenerator(new CustomIdGenerator());
        return conf;
    }

    /**
     * 注意:
     * <p>
     * 使用多个功能需要注意顺序关系,建议使用如下顺序
     * 多租户,动态表名
     * 分页,乐观锁
     * sql 性能规范,防止全表更新与删除
     * 总结: 对 sql 进行单次改造的优先放入,不对 sql 进行改造的最后放入
     */

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();


        //多租户插件
        interceptor.addInnerInterceptor(new TenantLineInnerInterceptor(new TenantLineHandler() {
            //对应表需要自动添加租户ID查询条件得时候, 自动添加的租户ID
            @Override
            public Expression getTenantId() {

                //先从租户上下文
                Long tenantId = TenantContext.getTenantId();
                if (tenantId != null) {
                    return new LongValue(tenantId);
                } else {
                    throw new BusinessException(ExceptionCommonDefineEnum.COMMON_FAIL, "找不到租户ID");
                }

            }

            // true表示不添加租户查询条件 false表示要增加条件
            @Override
            public boolean ignoreTable(String tableName) {
                if ("company".equalsIgnoreCase(tableName)
                        || "company_reg_info".equalsIgnoreCase(tableName)
                        || "sys_menu".equalsIgnoreCase(tableName)
                        || "sys_role".equalsIgnoreCase(tableName)
                        || "sys_role_menu".equalsIgnoreCase(tableName)
                        || "sys_user".equalsIgnoreCase(tableName)
                        || "sys_user_role".equalsIgnoreCase(tableName)
                        || "department".equalsIgnoreCase(tableName)
                        || "sys_user_department".equalsIgnoreCase(tableName)
                        || "club".equalsIgnoreCase(tableName)
                        || "sys_user_manage_club".equalsIgnoreCase(tableName)
                        // 排除本地消息表
                        || "mars_local_msg_record".equalsIgnoreCase(tableName)
                        // 排除进件(收单)商户账户表
                        || "income_mch_account".equalsIgnoreCase(tableName)
                        // 排除收单账户绑定关系表
                        || "income_mch_account_bind".equalsIgnoreCase(tableName)
                        // 排除进件(收单)商户账户表
                        || "income_mch_account_pos".equalsIgnoreCase(tableName)
                        // 排除盛付通交易订单和支付单关联表
                        || "transaction_trade_bind".equalsIgnoreCase(tableName)
                        // 排除pos机退款日志记录表
                        || "pos_refund_log".equalsIgnoreCase(tableName)
                        // 门店资源限制表
                        || "club_res_config".equalsIgnoreCase(tableName)
                        // 公司资源限制表
                        || "company_res_config".equalsIgnoreCase(tableName)
                        // app版本管理
                        || "app_version_config".equalsIgnoreCase(tableName)
                        // 微信三方平台token
                        || "wx_third_plat_token".equalsIgnoreCase(tableName)
                        //小程序注册
                        || "wx_third_mini_app".equalsIgnoreCase(tableName)
                        || "wx_third_code_manage_task".equalsIgnoreCase(tableName)
                        || "wx_third_code_manage_task_detail_audit".equalsIgnoreCase(tableName)
                        || "wx_third_code_manage_task_detail_release".equalsIgnoreCase(tableName)
                        || "wx_third_code_manage_task_detail_submit".equalsIgnoreCase(tableName)
                        || "wx_third_mini_app_reg_notify_record".equalsIgnoreCase(tableName)
                        || "wx_third_mini_app_def_config".equalsIgnoreCase(tableName)
                        || "wx_third_open_plat_app".equalsIgnoreCase(tableName)
                        || "sms_business_template".equals(tableName)
                        || "sms_recharge_sku".equals(tableName)
                        // 发票申请
                        || "mch_invoice_apply".equalsIgnoreCase(tableName)
                        || "mch_consume_bill".equalsIgnoreCase(tableName)
                        // 系统全局配置表
                        || "sys_def_config".equalsIgnoreCase(tableName)
                        || "sms_merchant_resource".equalsIgnoreCase(tableName)
                ) {
                    return true;
                } else {
                    return false;
                }
            }

            //租户字段名称
            @Override
            public String getTenantIdColumn() {
                return "com_id";
            }
        }));

        // 自动分页
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor());
        //配置乐观锁插件，必须开启这个@Version注解才生效
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());

        //防止而已全表更新
        interceptor.addInnerInterceptor(new BlockAttackInnerInterceptor());

        return interceptor;
    }

}
