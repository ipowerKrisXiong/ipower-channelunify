package com.ipower.cloud.channelunify.infra.config;

import com.alibaba.ttl.TransmittableThreadLocal;
import lombok.extern.slf4j.Slf4j;

/**
 * 租户上下文
 */
@Slf4j
public class TenantContext implements AutoCloseable {

    //TransmittableThreadLocal可以实现线程池传递
    private static final TransmittableThreadLocal<TenantContext> TENANT_MANAGER_HOLDER = new TransmittableThreadLocal<TenantContext>();

    private Long tenantId = null;

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }

    public static void setTenantIdCurThread(Long tenantId) {
        getInstance().setTenantId(tenantId);
    }

    public static Long getTenantId() {
        return TENANT_MANAGER_HOLDER.get() != null ? TENANT_MANAGER_HOLDER.get().tenantId : null;
    }

    public static void clean() {
        TENANT_MANAGER_HOLDER.remove();
    }

    //自动关闭释放
    @Override
    public void close() {
        clean();
    }

    public static TenantContext getInstance() {
        //不做检查，不然spingmvc设置了后面再设置要报错
//        Preconditions.checkState(null == TENANT_MANAGER_HOLDER.get(), "Hint has previous value, please clear first.");
        TenantContext tenantContext = TENANT_MANAGER_HOLDER.get();
        if (tenantContext == null) {
            tenantContext = new TenantContext();
            TENANT_MANAGER_HOLDER.set(tenantContext);
        }
        return tenantContext;
    }

}
