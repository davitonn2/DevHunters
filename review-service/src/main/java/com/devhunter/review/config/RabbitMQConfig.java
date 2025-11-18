package com.devhunter.review.config;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String BOUNTY_SUBMISSION_QUEUE = "bounty.submission.queue";

    @Bean
    public Queue submissionQueue() {
        return new Queue(BOUNTY_SUBMISSION_QUEUE, true);
    }
}

