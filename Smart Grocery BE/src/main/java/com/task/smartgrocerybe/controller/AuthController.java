package com.task.smartgrocerybe.controller;

import com.task.smartgrocerybe.dto.AuthResponse;
import com.task.smartgrocerybe.dto.LoginRequest;
import com.task.smartgrocerybe.dto.RegisterRequest;
import com.task.smartgrocerybe.model.enums.Role;
import com.task.smartgrocerybe.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
            @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request, Role.USER));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}