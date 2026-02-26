package com.commontools.server.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * 异步处理配置
 * 用于异步执行统计记录、邮件发送等不需要立即返回结果的操作
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    /**
     * 统计相关的线程池
     * 用于异步处理使用统计数据记录，避免阻塞主请求
     */
    @Bean(name = "statisticsTaskExecutor")
    public Executor statisticsTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // 核心线程数：根据CPU核数调整，一般为 CPU核数/2
        executor.setCorePoolSize(2);
        // 最大线程数：一般为 核心线程数的2倍
        executor.setMaxPoolSize(4);
        // 队列容量：缓冲未处理的任务
        executor.setQueueCapacity(100);
        // 线程名前缀
        executor.setThreadNamePrefix("statistics-async-");
        // 拒绝策略：使用调用者所在线程来运行该任务
        executor.setRejectedExecutionHandler(new java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy());
        // 是否等待任务完成才关闭线程池
        executor.setWaitForTasksToCompleteOnShutdown(true);
        // 等待时间（秒）
        executor.setAwaitTerminationSeconds(30);
        executor.initialize();
        return executor;
    }

    /**
     * 邮件相关的线程池
     * 用于异步发送邮件，避免阻塞主请求
     */
    @Bean(name = "emailTaskExecutor")
    public Executor emailTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("email-async-");
        executor.setRejectedExecutionHandler(new java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.initialize();
        return executor;
    }
}
