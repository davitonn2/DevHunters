package com.devhunter.bounty.model.dto;

import com.devhunter.bounty.model.enums.UserRole;

import java.math.BigDecimal;

public record LoginResponseDTO(String token,
                               Long userId,
                               String name,
                               String login,
                               UserRole role,
                               BigDecimal balance) {
}
