package com.devhunter.bounty.model.dto;

import com.devhunter.bounty.model.enums.UserRole;

import java.math.BigDecimal;

public record UserResponseDTO(Long id,
                              String name,
                              String login,
                              UserRole role,
                              BigDecimal balance) {
}
