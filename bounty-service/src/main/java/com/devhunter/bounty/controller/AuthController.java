package com.devhunter.bounty.controller;

import com.devhunter.bounty.infra.TokenService;
import com.devhunter.bounty.model.dto.AuthenticationDTO;
import com.devhunter.bounty.model.dto.LoginResponseDTO;
import com.devhunter.bounty.model.dto.RegisterDTO;
import com.devhunter.bounty.model.entity.User;
import com.devhunter.bounty.repository.UserRepository;
import com.devhunter.bounty.service.AuthorizationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final TokenService tokenService;

    public AuthController( AuthenticationManager authenticationManager, UserRepository userRepository, TokenService tokenService) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.tokenService = tokenService;
    }

    @PostMapping("/login")
    public ResponseEntity login(@RequestBody AuthenticationDTO dto){
        var userPassword = new UsernamePasswordAuthenticationToken(dto.login(),dto.password());
        var auth = authenticationManager.authenticate(userPassword);
        String token = tokenService.generateToken((User) auth.getPrincipal());
        return ResponseEntity.ok(new LoginResponseDTO(token));
    }

    @PostMapping("/register")
    public ResponseEntity register(@RequestBody RegisterDTO dto) {
    if(this.userRepository.findByLogin(dto.login()) != null) return ResponseEntity.badRequest().build();

    String encryptedPassword = new BCryptPasswordEncoder().encode(dto.password());
    User user = new User(dto.login(), encryptedPassword, dto.role());
    userRepository.save(user);
    return ResponseEntity.ok().build();
    }
}
