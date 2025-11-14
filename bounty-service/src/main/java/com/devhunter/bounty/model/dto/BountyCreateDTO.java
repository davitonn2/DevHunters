package com.devhunter.bounty.model.dto;

import lombok.Data;

@Data
public class BountyCreateDTO {
    private String title;
    private String description;
    private Integer rewardXp;
}

