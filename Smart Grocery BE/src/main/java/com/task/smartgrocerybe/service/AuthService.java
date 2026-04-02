package com.task.smartgrocerybe.service;

import com.task.smartgrocerybe.dto.AuthResponse;
import com.task.smartgrocerybe.dto.LoginRequest;
import com.task.smartgrocerybe.dto.RegisterRequest;
import com.task.smartgrocerybe.exception.ResourceAlreadyExistsException;
import com.task.smartgrocerybe.exception.ResourceNotFoundException;
import com.task.smartgrocerybe.model.User;
import com.task.smartgrocerybe.model.enums.Role;
import com.task.smartgrocerybe.repository.UserRepository;
import com.task.smartgrocerybe.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.DuplicateFormatFlagsException;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ResourceAlreadyExistsException("Email already exists");
        }

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new ResourceAlreadyExistsException("Username already exists");
        }

        var user = mapToUser(request);

        userRepository.save(user);
        String token = jwtService.generateToken(user);

        return mapToAuthResponse(user, token);
    }

    private User mapToUser(RegisterRequest request){
        return User.builder()
                .name(request.getName())
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole() != null ? request.getRole() : Role.USER)
                .isActive(true)
                .build();
    }

    private AuthResponse mapToAuthResponse(User user, String token){
        return AuthResponse.builder()
                .token(token)
                .role(user.getRole().name())
                .username(user.getUsername())
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        // throws BadCredentialsException if wrong password
        // throws DisabledException if user is inactive
        // throws UsernameNotFoundException if user doesn't exist
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        var user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        String token = jwtService.generateToken(user);

        return mapToAuthResponse(user, token);
    }

}