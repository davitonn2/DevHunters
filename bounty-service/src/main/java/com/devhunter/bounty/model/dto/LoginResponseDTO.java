package com.devhunter.bounty.model.dto;

import com.devhunter.bounty.model.enums.UserRole;

public record LoginResponseDTO(String token,
                               Long userId,
                               String name,
                               String login,
                               UserRole role,
                               Integer xp) {
}
