package com.devhunter.email.model;

import lombok.*;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
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
    private List<String> targetEmails;
}
