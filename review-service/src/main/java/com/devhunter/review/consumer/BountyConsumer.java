package com.devhunter.review.consumer;

import com.devhunter.review.model.dto.BountySubmissionDTO;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class BountyConsumer {

    @RabbitListener(queues = "bounty.submission.queue")
    public void handleSubmission(BountySubmissionDTO submission) {
        System.out.printf("[REVIEW-SERVICE] Recebida submissão para Bounty ID: %d do Hunter ID: %d%n",
                submission.getBountyId(), submission.getHunterId());
    }
}

