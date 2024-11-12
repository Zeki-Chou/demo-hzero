package com.hand.demo.config;

import org.hzero.core.message.MessageAccessor;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ExceptionMessageConfig {
    @Bean
    public SmartInitializingSingleton iamSmartInitializingSingleton() {
        return () -> {
            MessageAccessor.addBasenames(
                    "classpath:messages/messages"
            );
        };
    }
}
