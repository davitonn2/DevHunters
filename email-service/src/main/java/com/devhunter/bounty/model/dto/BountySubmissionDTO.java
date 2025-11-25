package com.devhunter.bounty.model.dto;

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

    public Long getBountyId() {
        return bountyId;
    }

    public void setBountyId(Long bountyId) {
        this.bountyId = bountyId;
    }

    public Long getHunterId() {
        return hunterId;
    }

    public void setHunterId(Long hunterId) {
        this.hunterId = hunterId;
    }

    public String getMasterLogin() {
        return masterLogin;
    }

    public void setMasterLogin(String masterLogin) {
        this.masterLogin = masterLogin;
    }
}
