package com.example.auth_service.config;

import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

<<<<<<< HEAD

=======
>>>>>>> 70653b2 (feat(auth-service): Integrate RabbitMQ for data sharing.)
@Configuration
public class RabbitMQConfig {
    
    // Tên của bưu điện trung tâm
    public static final String EXCHANGE_NAME = "ev_maintenance_exchange";
    // Mã bưu phẩm cho sự kiện tạo user
    public static final String ROUTING_KEY_USER_CREATED = "user.created";

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(EXCHANGE_NAME);
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}