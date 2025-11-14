package com.devhunter.bounty.service;

import com.devhunter.bounty.config.RabbitMQConfig;
import com.devhunter.bounty.model.dto.BountyCreateDTO;
import com.devhunter.bounty.model.dto.BountySubmissionDTO;
import com.devhunter.bounty.model.entity.Bounty;
import com.devhunter.bounty.model.entity.User;
import com.devhunter.bounty.model.enums.BountyStatus;
import com.devhunter.bounty.repository.BountyRepository;
import com.devhunter.bounty.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BountyService {

    private final BountyRepository bountyRepository;
    private final UserRepository userRepository;
    private final RabbitTemplate rabbitTemplate;

    @Transactional
    public Bounty createBounty(BountyCreateDTO dto) {
        Bounty bounty = Bounty.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .rewardXp(dto.getRewardXp())
                .status(BountyStatus.ABERTA)
                .build();
        return bountyRepository.save(bounty);
    }

    @Transactional(readOnly = true)
    public List<Bounty> getOpenBounties() {
        return bountyRepository.findAllByStatus(BountyStatus.ABERTA);
    }

    @Transactional
    public Bounty claimBounty(Long bountyId, Long hunterId) {
        Bounty bounty = bountyRepository.findById(bountyId)
                .orElseThrow(() -> new IllegalArgumentException("Bounty not found"));
        User hunter = userRepository.findById(hunterId)
                .orElseThrow(() -> new IllegalArgumentException("Hunter not found"));

        bounty.setStatus(BountyStatus.EM_ANDAMENTO);
        bounty.setHunter(hunter);
        return bountyRepository.save(bounty);
    }

    @Transactional
    public void submitBounty(Long bountyId, Long hunterId) {
        Bounty bounty = bountyRepository.findById(bountyId)
                .orElseThrow(() -> new IllegalArgumentException("Bounty not found"));

        if (bounty.getHunter() == null || !bounty.getHunter().getId().equals(hunterId)) {
            throw new IllegalStateException("Hunter not assigned to this bounty");
        }

        bounty.setStatus(BountyStatus.EM_REVISAO);
        bountyRepository.save(bounty);

        BountySubmissionDTO submissionDTO = BountySubmissionDTO.builder()
                .bountyId(bountyId)
                .hunterId(hunterId)
                .build();
        rabbitTemplate.convertAndSend(RabbitMQConfig.BOUNTY_SUBMISSION_QUEUE, submissionDTO);
    }

    @Transactional
    public void deleteBounty(Long bountyId) {
        bountyRepository.deleteById(bountyId);
    }
}

