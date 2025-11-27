package com.devhunter.email.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    public static final String SUBMISSION_QUEUE = "bounty.submission.queue";
    public static final String CLAIM_QUEUE = "bounty.claim.queue";
    public static final String COMPLETION_QUEUE = "bounty.completion.queue";
    public static final String APPROVED_QUEUE = "bounty.approved.queue";
    public static final String CREATED_QUEUE = "bounty.created.queue";
    public static final String REJECTED_QUEUE = "bounty.rejected.queue";

    @Bean
    public Queue rejectedQueue() {
        return new Queue(REJECTED_QUEUE, true);
    }

    @Bean
    public Queue approvedQueue() {
        return new Queue(APPROVED_QUEUE, true);
    }

    @Bean
    public Queue createdQueue() {
        return new Queue(CREATED_QUEUE, true);
    }

    @Bean
    public Queue submissionQueue() {
        return new Queue(SUBMISSION_QUEUE, true);
    }

    @Bean
    public Queue claimQueue() {
        return new Queue(CLAIM_QUEUE, true);
    }

    @Bean
    public Queue completionQueue() {
        return new Queue(COMPLETION_QUEUE, true);
    }
    
    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, Jackson2JsonMessageConverter converter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(converter);
        return template;
    }
}
