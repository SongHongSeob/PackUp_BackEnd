package com.swygbro.packup.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.cache.CacheManager;
import org.springframework.cache.support.NoOpCacheManager;

import jakarta.mail.internet.MimeMessage;
import java.io.InputStream;

@TestConfiguration
@Profile("test")
public class TestConfig {

    @Bean
    @Primary
    public JavaMailSender mockMailSender() {
        return new JavaMailSender() {
            @Override
            public MimeMessage createMimeMessage() {
                return null;
            }

            @Override
            public MimeMessage createMimeMessage(InputStream contentStream) {
                return null;
            }

            @Override
            public void send(MimeMessage mimeMessage) {
                // Mock implementation - do nothing
            }

            @Override
            public void send(MimeMessage... mimeMessages) {
                // Mock implementation - do nothing
            }

            @Override
            public void send(MimeMessagePreparator mimeMessagePreparator) {
                // Mock implementation - do nothing
            }

            @Override
            public void send(MimeMessagePreparator... mimeMessagePreparators) {
                // Mock implementation - do nothing
            }

            @Override
            public void send(SimpleMailMessage simpleMessage) {
                // Mock implementation - do nothing
            }

            @Override
            public void send(SimpleMailMessage... simpleMessages) {
                // Mock implementation - do nothing
            }
        };
    }

    @Bean
    @Primary
    public CacheManager cacheManager() {
        return new NoOpCacheManager();
    }
}
