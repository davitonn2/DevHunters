package com.devhunter.email.consumer;

import com.devhunter.email.model.BountyClaimNotificationDTO;
import com.devhunter.email.model.BountySubmissionDTO;
import com.devhunter.email.config.RabbitMQConfig;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
public class EmailConsumer {

    @Autowired
    private JavaMailSender mailSender;

    @RabbitListener(queues = RabbitMQConfig.CLAIM_QUEUE)
    public void handleClaimNotification(BountyClaimNotificationDTO dto) {
        String assunto = "Há um novo candidato para a sua bounty: " + dto.getBountyTitle();
        String corpo = "Olá!\nO Hunter :" + dto.getHunterName() + " está interessado em trabalhar na sua bounty: " + dto.getBountyTitle() +
                "\n\nVerifique a plataforma para aprovar ou recusar a solicitação.";

        sendEmail(dto.getMasterLogin(), assunto, corpo);
    }

    @RabbitListener(queues = RabbitMQConfig.SUBMISSION_QUEUE)
    public void handleSubmission(BountySubmissionDTO dto) {
        String assunto = "DevHunter - Bounty Submetida para Revisão!";
        String corpo = "O Hunter " + dto.getHunterName() + " entregou a Bounty  " + dto.getBountyTitle() + ".\nCorre lá pra revisar!";

        sendEmail(dto.getMasterLogin(), assunto, corpo);
    }

        @RabbitListener(queues = RabbitMQConfig.COMPLETION_QUEUE)
        public void handleCompletionNotification(BountyClaimNotificationDTO dto) {
            String assunto = "DevHunter - Bounty Finalizada/Recusada";
            String corpo = "Atualização final sobre a tarefa: " + dto.getBountyTitle() +
                    "\n\nVerifique sua recompensa e status na plataforma.";

            if(dto.getHunterEmail() != null && dto.getHunterEmail().contains("@")) {
                sendEmail(dto.getHunterEmail(), assunto, corpo);
            } else {
                System.out.println("⚠️ O login do hunter não parece um email: " + dto.getHunterName());
            }
        }

        @RabbitListener(queues = RabbitMQConfig.APPROVED_QUEUE)
        public void handelBountyApproved(BountyClaimNotificationDTO dto){
        String assunto = "Parabéns! você foi aprovado! 🎯";
        String corpo = "Olá " + dto.getHunterName() + "\n" +
                "                       O Master aceitou sua solicitação para a bounty:"  + dto.getBountyTitle() + " \n" +
                "                       Você já pode começar a trabalhar nela! \n";

        sendEmail(dto.getHunterEmail(), assunto, corpo);
        }

        @RabbitListener(queues = RabbitMQConfig.CREATED_QUEUE)
        public void handelBountyCreated(BountyClaimNotificationDTO dto){

            if(dto.getTargetEmails() == null || dto.getTargetEmails().isEmpty()) {
                System.out.println("⚠️ Nenhum email alvo fornecido para notificação de bounty criada.");
                return;
            }

            String assunto = "Nova Bounty Disponível: " + dto.getBountyTitle();
            String corpo = "Olá Hunter!\n\n" +
                    "Uma nova oportunidade foi postada pelo Master " + dto.getMasterLogin() + ".\n" +
                    "Título: " + dto.getBountyTitle() + "\n" +
                    "Corra para a plataforma e garanta suas recompenas!";

            for (String emailHunter : dto.getTargetEmails()) {
                if (emailHunter != null && emailHunter.contains("@")) {
                    sendEmail(emailHunter, assunto, corpo);
                }
            }
        }

        @RabbitListener(queues = RabbitMQConfig.REJECTED_QUEUE)
        public void handelBountyRejected(BountyClaimNotificationDTO dto){
        String assunto = "Atualização sobre sua candidatura à bounty";
        String corpo = "Olá " + dto.getHunterName() + "\n" +
                "                       Infelizmente, sua solicitação para a bounty:"  + dto.getBountyTitle() + " \n" +
                "                       foi recusada pelo Master. Não desanime, há muitas outras oportunidades esperando por você! \n";

        sendEmail(dto.getHunterEmail(), assunto, corpo);
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