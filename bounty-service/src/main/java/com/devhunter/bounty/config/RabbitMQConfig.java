package com.devhunter.bounty.config;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String BOUNTY_CLAIM_QUEUE = "bounty.claim.queue";
    public static final String BOUNTY_COMPLETION_QUEUE = "bounty.completion.queue";

    @Bean
    public Queue submissionQueue() {
        return new Queue("bounty.submission.queue", true);
    }

    @Bean
    public Queue claimRequestQueue() {
        return new Queue(BOUNTY_CLAIM_QUEUE, true);
    }

    @Bean
    public Queue completionQueue() {
        return new Queue(BOUNTY_COMPLETION_QUEUE, true);
    }
}