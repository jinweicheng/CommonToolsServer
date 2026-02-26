package com.commontools.server.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.beans.factory.annotation.Value;

import io.lettuce.core.ClientOptions;
import io.lettuce.core.SocketOptions;

import java.time.Duration;

/**
 * Redis连接配置
 * 优化Lettuce连接池参数，避免连接泄漏和超时问题
 */
@Configuration
@ConditionalOnProperty(name = "app.security.redis-enabled", havingValue = "true")
@Slf4j
public class RedisConfig {
    @Value("${spring.redis.host:localhost}")
    private String redisHost;

    @Value("${spring.redis.port:6379}")
    private int redisPort;

    @Value("${spring.redis.timeout:2000}")
    private long timeout;

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        // Redis服务器配置
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName(redisHost);
        config.setPort(redisPort);

        // Lettuce客户端配置+连接池优化
        LettuceClientConfiguration.LettuceClientConfigurationBuilder builder = 
            LettuceClientConfiguration.builder();

        // 设置连接超时
        builder.commandTimeout(Duration.ofMillis(timeout));

        // 设置Socket参数
        SocketOptions socketOptions = SocketOptions.builder()
                // 启用TCP Keep-Alive，避免连接被关闭
                .keepAlive(true)
                // 设置TCP No-Delay，减少网络延迟
                .tcpNoDelay(true)
                .build();

        // 设置客户端选项
        ClientOptions clientOptions = ClientOptions.builder()
                .socketOptions(socketOptions)
                // 自动重连接
                .autoReconnect(true)
                .build();

        builder.clientOptions(clientOptions);

        LettuceConnectionFactory factory = new LettuceConnectionFactory(config, builder.build());
        log.info("Redis连接工厂已初始化 - host: {}, port: {}", redisHost, redisPort);
        return factory;
    }

    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory factory) {
        StringRedisTemplate template = new StringRedisTemplate(factory);
        template.afterPropertiesSet();
        log.info("StringRedisTemplate已初始化");
        return template;
    }
}
