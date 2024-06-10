package com.ipower.cloud.channelunify.infra.config;

import com.mars.auth.core.util.LoginUserUtils;
import com.mars.auth.type.LoginUserBaseInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 */
@Slf4j
public class TenantInputInterceptor implements HandlerInterceptor {

    /**
     * 预处理回调方法，实现处理器的预处理（如检查登陆），第三个参数为响应的处理器，自定义Controller
     * 返回值：true表示继续流程（如调用下一个拦截器或处理器）；
     * 　　　*       false表示流程中断（如登录检查失败），不会继续调用其他的拦截器或处理器，此时我们需要通过response来产生响应；
     */

    private String TENANT_HEADER = "comId";
    //拦截请求前置执行
    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response, Object handler) throws Exception {

        //设置之前先清除，因为tomcat执行线程用的线程池，怕还遗留有上次的信息
        //TenantContext.clean(); 这里不做清除，不然前面无法进行手动设置，清除了之后后面无效

        //按优先级顺序获取comId
        //先从登录用户身上获取
        Long tenantId = null;
        LoginUserBaseInfo loginUserBaseInfo = LoginUserUtils.getUser().getLoginUserBaseInfo();
        if (loginUserBaseInfo != null) {
            tenantId = loginUserBaseInfo.getTenantId();
        }

        //如果登录人身上取不到，则从header中取
        if (tenantId == null) {
            String tenantIdStr = request.getHeader(TENANT_HEADER);
            if (tenantIdStr != null)
                tenantId = Long.valueOf(tenantIdStr);
        }

        //设置租户
        TenantContext.setTenantIdCurThread(tenantId);

        //如果没有则保持前面自己的设置
        return true;

    }

    /**
     * 后处理回调方法，实现处理器的后处理（但在渲染视图之前），此时我们可以通过modelAndView（模型和视图对象）对模型数据进行处理或对视图进行处理，modelAndView也可能为null。
     */
//    //拦截请求controller执行完后执行
//    @Override
//    public void postHandle(HttpServletRequest request,
//                           HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
//    }
//

    /**
     * 整个请求处理完毕回调方法，即在视图渲染完毕时回调，如性能监控中我们可以在此记录结束时间并输出消耗时间，还可以进行一些资源清理，类似于try-catch-finally中的finall
     * 　　 * 但仅调用处理器执行链中preHandle返回true的拦截器的afterCompletion。
     */
//    //post之后执行
    @Override
    public void afterCompletion(HttpServletRequest request,
                                HttpServletResponse response, Object handler, Exception ex) throws Exception {
        //清除当前线程租户信息
        TenantContext.clean();
    }
}
