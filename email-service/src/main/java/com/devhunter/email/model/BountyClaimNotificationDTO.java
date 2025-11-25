package com.devhunter.email.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BountyClaimNotificationDTO implements Serializable {
    private Long bountyId;
    private String bountyTitle;
    private Long hunterId;
    private String hunterName;
    private Long masterId;
    private String masterLogin;
    private String masterEmail;
    private String hunterEmail;
}
