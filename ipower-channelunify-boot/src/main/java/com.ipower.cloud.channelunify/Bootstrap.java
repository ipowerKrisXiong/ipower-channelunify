package com.ipower.cloud.channelunify;


import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

import java.net.InetAddress;

@Slf4j
@SpringBootApplication(scanBasePackages = {"com.ipower"})
@EnableDiscoveryClient
public class Bootstrap {
    public static void main(String[] args) throws Exception {
        //SpringApplication.run(enterpriseBootstrap.class, args);

        ConfigurableApplicationContext applicationContext = SpringApplication.run(cn.need.cloud.enterprise.EnterpriseBootstrap.class, args);
        ConfigurableEnvironment env = applicationContext.getEnvironment();
        String ip = InetAddress.getLocalHost().getHostAddress();
        String port = env.getProperty("server.port");
        //noinspection HttpUrlsUsage
        String url = String.format("Swagger UI: http://%s:%s/doc.html", ip, port);
        log.info("********************** 企业管理服务" + url + " enterprise service startup complete **********************");
    }
}
