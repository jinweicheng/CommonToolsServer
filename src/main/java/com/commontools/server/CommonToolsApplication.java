package com.commontools.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * CommonTools 应用主类
 * 
 * @EnableAsync 启用异步方法支持，用于处理统计、邮件等不需要立即返回的操作
 */
@SpringBootApplication
@EnableJpaAuditing
@EnableAsync
public class CommonToolsApplication {
    public static void main(String[] args) {
        SpringApplication.run(CommonToolsApplication.class, args);
    }
}

