package com.devhunter.bounty.model.entity;

import com.devhunter.bounty.model.enums.BountyStatus;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "bounties")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Bounty {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    private BigDecimal rewardValue;

    @Enumerated(EnumType.STRING)
    @Column(length = 40)
    private BountyStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnoreProperties({"password", "authorities"})
    @JoinColumn(name = "hunter_id")
    private User hunter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnoreProperties({"password", "authorities"})
    @JoinColumn(name = "created_by_id")
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnoreProperties({"password", "authorities"})
    @JoinColumn(name = "pending_hunter_id")
    private User pendingHunter;
}

