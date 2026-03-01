package com.zhaoguhong.shortlink.admin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * 短链管理端启动类。
 *
 * @author zhaoguhong
 * @date 2026/2/27
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class ShortlinkAdminApplication {

    public static void main(String[] args) {
        SpringApplication.run(ShortlinkAdminApplication.class, args);
    }
}
