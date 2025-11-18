package com.devhunter.bounty.model.dto;

import com.devhunter.bounty.model.enums.UserRole;

public record RegisterDTO(String login, String password, UserRole role) {
}
