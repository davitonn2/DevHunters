package com.devhunter.bounty.controller;

import com.devhunter.bounty.model.dto.UserResponseDTO;
import com.devhunter.bounty.model.entity.User;
import com.devhunter.bounty.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> findById(@PathVariable Long id) {
        System.out.println("--- DEBUG: Frontend pediu dados do usuário ID: " + id + " ---");

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));

        BigDecimal saldo = user.getBalance() != null ? user.getBalance() : BigDecimal.ZERO;

        System.out.println("--- DEBUG: Saldo encontrado no banco para enviar: " + saldo + " ---");


        Map<String, Object> resposta = new HashMap<>();
        resposta.put("id", user.getId());
        resposta.put("name", user.getName());
        resposta.put("login", user.getLogin());
        resposta.put("email", user.getLogin());
        resposta.put("role", user.getRole());
        resposta.put("balance", saldo);

        return ResponseEntity.ok(resposta);
    }
}
