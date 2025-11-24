package com.devhunter.bounty.model.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BountyCreateDTO {
    private String title;
    private String description;
    private Integer rewardXp;
}

