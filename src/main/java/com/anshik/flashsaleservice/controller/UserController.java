package com.anshik.flashsaleservice.controller;

import com.anshik.flashsaleservice.dto.LoginRequest;
import com.anshik.flashsaleservice.dto.RegisterRequest;
import com.anshik.flashsaleservice.dto.UserResponse;
import com.anshik.flashsaleservice.entity.User;
import com.anshik.flashsaleservice.repository.UserRepository;
import com.anshik.flashsaleservice.security.JwtUtil;
import com.anshik.flashsaleservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor // Constructor injection for performance
public class UserController {

    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterRequest request) {
        String message = userService.registerUser(request);
        return new ResponseEntity<>(message, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<UserResponse> login(@RequestBody LoginRequest request) {
        // Authenticate
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        // Optimization: Find user only once
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String token = jwtUtil.generateToken(user.getUsername(), user.getRole().name());

        return ResponseEntity.ok(UserResponse.builder()
                .token(token)
                .type("Bearer")
                .role(user.getRole().name())
                .username(user.getUsername())
                .build());
    }
}