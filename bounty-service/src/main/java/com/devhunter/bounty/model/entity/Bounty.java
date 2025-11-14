package com.devhunter.bounty.model.entity;

import com.devhunter.bounty.model.enums.BountyStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

    private Integer rewardXp;

    @Enumerated(EnumType.STRING)
    private BountyStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hunter_id")
    private User hunter;
}

