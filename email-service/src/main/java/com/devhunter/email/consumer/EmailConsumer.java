package com.devhunter.email.consumer;

import com.devhunter.email.model.BountyClaimNotificationDTO;
import com.devhunter.email.model.BountySubmissionDTO;
import com.devhunter.email.config.RabbitMQConfig;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class EmailConsumer {

    @RabbitListener(queues = RabbitMQConfig.CLAIM_QUEUE)
    public void handleClaimNotification(BountyClaimNotificationDTO dto) {
        // Simulate sending email to master or hunter depending on context
        System.out.printf("[EMAIL-SERVICE] Claim notification — bounty=%d title=%s hunter=%s masterLogin=%s\n",
                dto.getBountyId(), dto.getBountyTitle(), dto.getHunterName(), dto.getMasterLogin());
    }

    @RabbitListener(queues = RabbitMQConfig.SUBMISSION_QUEUE)
    public void handleSubmission(BountySubmissionDTO dto) {
        System.out.printf("[EMAIL-SERVICE] Submission notification — bountyId=%d hunterId=%d\n",
                dto.getBountyId(), dto.getHunterId());
    }

    @RabbitListener(queues = RabbitMQConfig.COMPLETION_QUEUE)
    public void handleCompletionNotification(BountyClaimNotificationDTO dto) {
        System.out.printf("[EMAIL-SERVICE] Completion notification — bounty=%d title=%s hunter=%s master=%s\n",
                dto.getBountyId(), dto.getBountyTitle(), dto.getHunterName(), dto.getMasterLogin());
    }
}
