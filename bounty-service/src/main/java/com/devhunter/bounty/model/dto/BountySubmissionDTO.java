package com.devhunter.bounty.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BountySubmissionDTO {
    private Long bountyId;
    private Long hunterId;
}

