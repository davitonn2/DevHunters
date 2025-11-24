package com.devhunter.bounty.controller;

import com.devhunter.bounty.infra.TokenService;
import com.devhunter.bounty.model.dto.AuthenticationDTO;
import com.devhunter.bounty.model.dto.LoginResponseDTO;
import com.devhunter.bounty.model.dto.RegisterDTO;
import com.devhunter.bounty.model.entity.User;
import com.devhunter.bounty.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final TokenService tokenService;
    private final PasswordEncoder passwordEncoder;

    public AuthController(AuthenticationManager authenticationManager, UserRepository userRepository, TokenService tokenService, PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.tokenService = tokenService;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody AuthenticationDTO dto){
        var userPassword = new UsernamePasswordAuthenticationToken(dto.login(),dto.password());
        var auth = authenticationManager.authenticate(userPassword);
        User user = (User) auth.getPrincipal();
        String token = tokenService.generateToken(user);

        return ResponseEntity.ok(new LoginResponseDTO(
                token,
                user.getId(),
                user.getName(),
                user.getLogin(),
                user.getRole(),
                user.getXp()
        ));
    }

    @PostMapping("/register")
    public ResponseEntity<Void> register(@RequestBody RegisterDTO dto) {
        if(this.userRepository.findByLogin(dto.login()) != null) return ResponseEntity.badRequest().build();

        String encryptedPassword = this.passwordEncoder.encode(dto.password());
        User user = new User(dto.login(), encryptedPassword, dto.role());
        user.setName(dto.name());
        userRepository.save(user);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}