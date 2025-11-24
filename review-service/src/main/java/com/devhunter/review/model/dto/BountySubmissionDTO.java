package com.devhunter.review.model.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BountySubmissionDTO {
    private Long bountyId;
    private Long hunterId;
}

