package com.ipower.cloud.channelunify;


import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

import java.net.InetAddress;

@Slf4j
@SpringBootApplication
@EnableDiscoveryClient
public class BootstrapStarter {
    public static void main(String[] args) throws Exception {
        //SpringApplication.run(enterpriseBootstrap.class, args);

        ConfigurableApplicationContext applicationContext = SpringApplication.run(BootstrapStarter.class, args);
        ConfigurableEnvironment env = applicationContext.getEnvironment();
        String ip = InetAddress.getLocalHost().getHostAddress();
        String port = env.getProperty("server.port");
        //noinspection HttpUrlsUsage
        String url = String.format("Swagger UI: http://%s:%s/doc.html", ip, port);
        log.info("********************** channelUnify服务" + url + " channelUnify service startup complete **********************");
    }
}
