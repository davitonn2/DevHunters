package com.devhunter.email.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BountySubmissionDTO implements Serializable {
    private Long bountyId;
    private Long hunterId;
    private String masterLogin;

}
