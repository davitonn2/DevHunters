package com.devhunter.bounty.service;

import com.devhunter.bounty.config.RabbitMQConfig;
import com.devhunter.bounty.model.dto.BountyClaimNotificationDTO;
import com.devhunter.bounty.model.dto.BountyCreateDTO;
import com.devhunter.bounty.model.dto.BountySubmissionDTO;
import com.devhunter.bounty.model.entity.Bounty;
import com.devhunter.bounty.model.entity.User;
import com.devhunter.bounty.model.enums.BountyStatus;
import com.devhunter.bounty.model.enums.UserRole;
import com.devhunter.bounty.repository.BountyRepository;
import com.devhunter.bounty.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BountyService {

    private final BountyRepository bountyRepository;
    private final UserRepository userRepository;
    private final RabbitTemplate rabbitTemplate;

    @Transactional
    public Bounty createBounty(BountyCreateDTO dto) {
        User creator = requireAuthenticatedUser();
        requireRole(creator, UserRole.MASTER);

        Bounty bounty = Bounty.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .rewardXp(dto.getRewardXp())
                .status(BountyStatus.ABERTA)
                .createdBy(creator)
                .build();

        Bounty saved = bountyRepository.save(bounty);
        notifyAllHunters(saved, creator);
        return saved;
    }

    private void notifyAllHunters(Bounty bounty, User creator){
        try{
            List<User> hunters = userRepository.findAllByRole(UserRole.HUNTER);

            if(hunters.isEmpty()) return;

            List<String> huntersEmails = hunters.stream()
                    .map(User::getLogin)
                    .toList();

            BountyClaimNotificationDTO notificationDTO = BountyClaimNotificationDTO.builder()
                    .bountyId(bounty.getId())
                    .bountyTitle(bounty.getTitle())
                    .masterLogin(creator.getLogin())
                    .targetEmails(huntersEmails)
                    .build();

            rabbitTemplate.convertAndSend(RabbitMQConfig.BOUNTY_CREATED_QUEUE, notificationDTO);
        } catch (Exception e){
            System.out.println("❌ [NOTIFICAÇÃO HUNTERS] Falha ao notificar hunters sobre nova bounty: " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public List<Bounty> getBounties() {
        // Correto: Filtrar apenas por status ABERTA
        return bountyRepository.findAllByStatus(BountyStatus.ABERTA);
    }

    @Transactional(readOnly = true)
    public List<Bounty> getPendingBounties() {
        // Retorna todas que não estão ABERTA (pendentes, em andamento, em revisão, etc.)
        return bountyRepository.findAllByStatusNot(BountyStatus.ABERTA);
    }

    @Transactional
    public Bounty requestClaim(Long bountyId, Long hunterIdFromPayload) {
        User hunter = requireAuthenticatedUser();
        requireRole(hunter, UserRole.HUNTER);
        validatePayloadMatchesAuthenticatedUser(hunter, hunterIdFromPayload);

        Bounty bounty = findBounty(bountyId);
        if (bounty.getStatus() != BountyStatus.ABERTA) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bounty não está aberta para reivindicação.");
        }
        bounty.setPendingHunter(hunter);
        bounty.setStatus(BountyStatus.AGUARDANDO_APROVACAO);
        Bounty saved = bountyRepository.save(bounty);
        notifyMasterAboutClaim(saved, hunter);
        return saved;
    }

    @Transactional
    public Bounty approveClaim(Long bountyId) {
        User master = requireAuthenticatedUser();
        requireRole(master, UserRole.MASTER);

        Bounty bounty = findBounty(bountyId);
        ensureOwner(master, bounty);

        if (bounty.getStatus() != BountyStatus.AGUARDANDO_APROVACAO || bounty.getPendingHunter() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Não existe solicitação pendente para este bounty.");
        }

        User hunter = bounty.getPendingHunter(); // Pega o hunter antes de limpar o campo pending

        bounty.setHunter(hunter);
        bounty.setPendingHunter(null);
        bounty.setStatus(BountyStatus.EM_ANDAMENTO);
        Bounty saved = bountyRepository.save(bounty);


        // --- ADICIONADO: Avisa o Hunter que ele foi aprovado para começar ---
        BountyClaimNotificationDTO notificationDTO = BountyClaimNotificationDTO.builder()
                .bountyId(saved.getId())
                .bountyTitle(saved.getTitle())
                .hunterId(hunter.getId())
                .hunterName(hunter.getName())
                .hunterEmail(hunter.getLogin())
                .masterId(master.getId())
                .masterLogin(master.getLogin())
                .build();

        // Usamos a mesma fila de Claim para notificar a aprovação
        rabbitTemplate.convertAndSend(RabbitMQConfig.BOUNTY_APPROVED_QUEUE, notificationDTO);
        // ------------------------------------------------------------------

        return saved;
    }

    @Transactional
    public Bounty rejectClaim(Long bountyId) {
        User master = requireAuthenticatedUser();
        requireRole(master, UserRole.MASTER);

        Bounty bounty = findBounty(bountyId);
        ensureOwner(master, bounty);

        if (bounty.getStatus() != BountyStatus.AGUARDANDO_APROVACAO || bounty.getPendingHunter() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Não existe solicitação pendente para este bounty.");
        }

        User rejectedHunter = bounty.getPendingHunter();
        String hunterEmail = rejectedHunter.getLogin();

        // Rejeita e volta a abertura
        bounty.setPendingHunter(null);
        bounty.setStatus(BountyStatus.ABERTA);
        Bounty saved = bountyRepository.save(bounty);

        // Notifica o Hunter que foi rejeitado (reutiliza fila de claims)
        BountyClaimNotificationDTO notificationDTO = BountyClaimNotificationDTO.builder()
                .bountyId(saved.getId())
                .bountyTitle(saved.getTitle())
                .hunterId(rejectedHunter.getId())
                .hunterName(rejectedHunter.getName())
                .hunterEmail(hunterEmail)
                .masterId(master.getId())
                .masterLogin(master.getLogin())
                .build();

        rabbitTemplate.convertAndSend(RabbitMQConfig.BOUNTY_REJECTED_QUEUE, notificationDTO);

        return saved;
    }

    @Transactional
    public void submitBounty(Long bountyId, Long hunterIdFromPayload) {
        User hunter = requireAuthenticatedUser();
        requireRole(hunter, UserRole.HUNTER);
        validatePayloadMatchesAuthenticatedUser(hunter, hunterIdFromPayload);

        Bounty bounty = findBounty(bountyId);

        if (bounty.getHunter() == null || !bounty.getHunter().getId().equals(hunter.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Este bounty não está atribuído a você.");
        }

        bounty.setStatus(BountyStatus.EM_REVISAO);
        bountyRepository.save(bounty);

        User master = bounty.getCreatedBy();
        String emailMaster = master.getLogin();

        BountySubmissionDTO submissionDTO = BountySubmissionDTO.builder()
                .bountyId(bounty.getId())
                .hunterId(bounty.getHunter().getId())
                .masterLogin(bounty.getCreatedBy().getLogin())
                .bountyTitle(bounty.getTitle())
                .hunterName(bounty.getHunter().getName())
                .build();

        rabbitTemplate.convertAndSend(RabbitMQConfig.BOUNTY_SUBMISSION_QUEUE, submissionDTO);
    }

    @Transactional
    public void rejectReview(Long bountyId, String reason) {
        User master = requireAuthenticatedUser();
        requireRole(master, UserRole.MASTER);

        Bounty bounty = findBounty(bountyId);
        ensureOwner(master, bounty);

        if (bounty.getStatus() != BountyStatus.EM_REVISAO) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bounty não está em revisão.");
        }

        // Volta para em andamento para o hunter continuar
        bounty.setStatus(BountyStatus.EM_ANDAMENTO);
        bountyRepository.save(bounty);

        // Notifica Hunter sobre reprovação com motivo
        BountyClaimNotificationDTO notificationDTO = BountyClaimNotificationDTO.builder()
                .bountyId(bounty.getId())
                .bountyTitle(bounty.getTitle())
                .hunterId(bounty.getHunter() != null ? bounty.getHunter().getId() : null)
                .hunterName(bounty.getHunter() != null ? bounty.getHunter().getName() : null)
                .masterId(master.getId())
                .masterLogin(master.getLogin())
                .build();

        rabbitTemplate.convertAndSend(RabbitMQConfig.BOUNTY_COMPLETION_QUEUE, notificationDTO);
    }

    @Transactional
    public void deleteBounty(Long bountyId) {
        User master = requireAuthenticatedUser();
        requireRole(master, UserRole.MASTER);

        Bounty bounty = findBounty(bountyId);
        ensureOwner(master, bounty);
        bountyRepository.delete(bounty);
    }

    @Transactional
    public void completeBounty(Long bountyId) {
        User master = requireAuthenticatedUser();
        requireRole(master, UserRole.MASTER);

        Bounty bounty = findBounty(bountyId);
        ensureOwner(master, bounty);

        // Validação: Só pode finalizar se estiver EM_REVISAO
        if (bounty.getStatus() != BountyStatus.EM_REVISAO) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Status inválido. A bounty deve estar EM_REVISAO para ser finalizada.");
        }

        User hunter = bounty.getHunter();
        if (hunter == null) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Erro: Bounty sem hunter associado.");
        }

        bounty.setStatus(BountyStatus.FECHADA);
        bountyRepository.save(bounty);

        // --- CORREÇÃO DE XP (BLINDAGEM) ---
        // Garante que não quebra se vier nulo do banco
        int xpReward = bounty.getRewardXp() != null ? bounty.getRewardXp() : 0;
        int currentXp = hunter.getXp() != null ? hunter.getXp() : 0;
        int newXp = currentXp + xpReward;

        hunter.setXp(newXp);
        userRepository.save(hunter);

        // Log para você confirmar no terminal que funcionou
        System.out.println("✅ [XP UPGRADE] O Hunter " + hunter.getLogin() + " subiu de " + currentXp + " para " + newXp + " XP!");
        // ----------------------------------

        notifyHunterAboutCompletion(bounty, hunter);
    }

    private User requireAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuário não autenticado.");
        }

        String login = authentication.getName();
        User user = userRepository.findByLogin(login);

        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuário não encontrado.");
        }

        return user;
    }

    private void requireRole(User user, UserRole role) {
        if (user.getRole() != role) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Ação permitida apenas para " + role.name());
        }
    }

    private void ensureOwner(User master, Bounty bounty) {
        if (bounty.getCreatedBy() == null || !bounty.getCreatedBy().getId().equals(master.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Somente o criador pode executar esta ação.");
        }
    }

    private Bounty findBounty(Long bountyId) {
        return bountyRepository.findById(bountyId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Bounty não encontrada."));
    }

    private void notifyMasterAboutSubmission(Bounty bounty, User hunter) {
        if (bounty.getCreatedBy() == null) {
            return;
        }

        BountyClaimNotificationDTO notificationDTO = BountyClaimNotificationDTO.builder()
                .bountyId(bounty.getId())
                .bountyTitle(bounty.getTitle())
                .hunterId(hunter.getId())
                .hunterName(hunter.getName())
                .masterId(bounty.getCreatedBy().getId())
                .masterLogin(bounty.getCreatedBy().getLogin())
                .build();

        // 2. Hunter terminou: Notifica o Master para revisar.
        // Reutiliza a fila por simplicidade
        rabbitTemplate.convertAndSend(RabbitMQConfig.BOUNTY_CLAIM_QUEUE, notificationDTO);
    }

    private void notifyMasterAboutClaim(Bounty bounty, User hunter) {
        if (bounty.getCreatedBy() == null) {
            return;
        }

        BountyClaimNotificationDTO notificationDTO = BountyClaimNotificationDTO.builder()
                .bountyId(bounty.getId())
                .bountyTitle(bounty.getTitle())
                .hunterId(hunter.getId())
                .hunterName(hunter.getName())
                .masterId(bounty.getCreatedBy().getId())
                .masterLogin(bounty.getCreatedBy().getLogin())
                .build();

        // 1. Hunter quer fazer: Notifica o Master para aceitar/enviar detalhes.
        rabbitTemplate.convertAndSend(RabbitMQConfig.BOUNTY_CLAIM_QUEUE, notificationDTO);
    }

    private void notifyHunterAboutCompletion(Bounty bounty, User hunter) {
       String hunterEmail = hunter.getLogin();
        BountyClaimNotificationDTO notificationDTO = BountyClaimNotificationDTO.builder()
                .bountyId(bounty.getId())
                .bountyTitle(bounty.getTitle())
                .hunterId(hunter.getId())
                .hunterName(hunter.getName())
                .hunterEmail(hunterEmail)
                .masterId(bounty.getCreatedBy().getId())
                .masterLogin(bounty.getCreatedBy().getLogin())
                .build();

        rabbitTemplate.convertAndSend(RabbitMQConfig.BOUNTY_COMPLETION_QUEUE, notificationDTO);
    }

    private void validatePayloadMatchesAuthenticatedUser(User user, Long hunterIdFromPayload) {
        if (hunterIdFromPayload != null && !hunterIdFromPayload.equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Identificação do hunter inválida.");
        }
    }
}

