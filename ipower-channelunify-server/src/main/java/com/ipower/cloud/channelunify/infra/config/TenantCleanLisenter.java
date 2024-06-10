package com.ipower.cloud.channelunify.infra.config;//package com.club.clubmanager.infra.config;
//
//import org.springframework.stereotype.Component;
//
//import javax.servlet.ServletRequestEvent;
//import javax.servlet.ServletRequestListener;
//
//@Component
//public class TenantCleanLisenter implements ServletRequestListener {
//
//    @Override
//    public void requestDestroyed(ServletRequestEvent arg0) {
//        //清除登录用户装载
//        TenantContext.clean();
//    }
//
//    @Override
//    public void requestInitialized(ServletRequestEvent arg0) {
////        System.out.println("requestInitialized" + "," + new Date());
////        Object count = arg0.getServletContext().getAttribute("count");
////        Integer cInteger = 0;
////        if (count != null) {
////            cInteger = Integer.valueOf(count.toString());
////        }
////        System.out.println("历史访问次数：：" + count);
////        cInteger++;
////        arg0.getServletContext().setAttribute("count", cInteger);
//    }
//}
