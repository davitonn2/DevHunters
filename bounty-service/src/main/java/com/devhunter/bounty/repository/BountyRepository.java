package com.devhunter.bounty.repository;

import com.devhunter.bounty.model.entity.Bounty;
import com.devhunter.bounty.model.enums.BountyStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BountyRepository extends JpaRepository<Bounty, Long> {
    List<Bounty> findAllByStatus(BountyStatus status);
}

