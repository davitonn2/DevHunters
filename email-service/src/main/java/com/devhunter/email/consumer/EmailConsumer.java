package com.devhunter.email.consumer;

import com.devhunter.email.model.BountyClaimNotificationDTO;
import com.devhunter.email.model.BountySubmissionDTO;
import com.devhunter.email.config.RabbitMQConfig;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
public class EmailConsumer {

    @Autowired
    private JavaMailSender mailSender;

    @RabbitListener(queues = RabbitMQConfig.CLAIM_QUEUE)
    public void handleClaimNotification(BountyClaimNotificationDTO dto) {
        String assunto = "DevHunter - Nova Atualização na Bounty: " + dto.getBountyTitle();
        String corpo = "Olá!\n\nHouve uma movimentação na Bounty ID: " + dto.getBountyId() +
                "\nCaçador: " + dto.getHunterName() +
                "\nMestre: " + dto.getMasterLogin();

        // ⚠️ IMPORTANTE: Como seu sistema usa 'login' e não tem campo 'email',
        // estou assumindo que o login SEJA o email (ex: joao@gmail.com).
        // Se não for, troque abaixo por um email fixo pra testar (ex: "seu-amigo@gmail.com")
        sendEmail(dto.getMasterLogin(), assunto, corpo);
    }

    @RabbitListener(queues = RabbitMQConfig.SUBMISSION_QUEUE)
    public void handleSubmission(BountySubmissionDTO dto) {
        String assunto = "DevHunter - Bounty Submetida para Revisão!";
        String corpo = "O Caçador (ID: " + dto.getHunterId() + ") entregou a Bounty (ID: " + dto.getBountyId() + ").\nCorre lá pra revisar!";

        // Aqui você teria que buscar o email do Mestre, mas vou mandar fixo pra vc testar
        // Troque pelo seu email real para teste
        sendEmail("seu-email-aqui@gmail.com", assunto, corpo);
    }

    @RabbitListener(queues = RabbitMQConfig.COMPLETION_QUEUE)
    public void handleCompletionNotification(BountyClaimNotificationDTO dto) {
        String assunto = "DevHunter - Bounty Finalizada/Recusada";
        String corpo = "Atualização final sobre a tarefa: " + dto.getBountyTitle() +
                "\n\nVerifique seu XP e status na plataforma.";

        // Tentando mandar pro login do Hunter (assumindo ser email)
        // Se o login for só "joao", isso vai falhar.
        if(dto.getHunterName() != null && dto.getHunterName().contains("@")) {
            sendEmail(dto.getHunterName(), assunto, corpo);
        } else {
            System.out.println("⚠️ O login do hunter não parece um email: " + dto.getHunterName());
        }
    }

    private void sendEmail(String para, String assunto, String texto) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("noreply@devhunter.com");
            message.setTo(para);
            message.setSubject(assunto);
            message.setText(texto);
            mailSender.send(message);
            System.out.println("✅ Email enviado com sucesso para: " + para);
        } catch (Exception e) {
            System.err.println("❌ Erro ao enviar email: " + e.getMessage());
        }
    }
}