package com.devhunter.bounty.model.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BountyClaimNotificationDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long bountyId;
    private String bountyTitle;
    private Long hunterId;
    private String hunterName;
    private Long masterId;
    private String masterLogin;
    private String hunterEmail;
    private List<String> targetEmails;
}

