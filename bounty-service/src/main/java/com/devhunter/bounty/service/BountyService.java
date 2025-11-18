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
        return bountyRepository.save(bounty);
    }

    @Transactional(readOnly = true)
    public List<Bounty> getBounties() {
        return bountyRepository.findAll();
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

        bounty.setHunter(bounty.getPendingHunter());
        bounty.setPendingHunter(null);
        bounty.setStatus(BountyStatus.EM_ANDAMENTO);
        return bountyRepository.save(bounty);
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

        BountySubmissionDTO submissionDTO = BountySubmissionDTO.builder()
                .bountyId(bountyId)
                .hunterId(hunter.getId())
                .build();
        rabbitTemplate.convertAndSend(RabbitMQConfig.BOUNTY_SUBMISSION_QUEUE, submissionDTO);
    }

    @Transactional
    public void deleteBounty(Long bountyId) {
        User master = requireAuthenticatedUser();
        requireRole(master, UserRole.MASTER);

        Bounty bounty = findBounty(bountyId);
        ensureOwner(master, bounty);
        bountyRepository.delete(bounty);
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

        rabbitTemplate.convertAndSend(RabbitMQConfig.BOUNTY_CLAIM_QUEUE, notificationDTO);
    }

    private void validatePayloadMatchesAuthenticatedUser(User user, Long hunterIdFromPayload) {
        if (hunterIdFromPayload != null && !hunterIdFromPayload.equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Identificação do hunter inválida.");
        }
    }
}

