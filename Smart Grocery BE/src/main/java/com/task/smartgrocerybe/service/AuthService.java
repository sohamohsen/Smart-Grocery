package com.task.smartgrocerybe.service;

import com.task.smartgrocerybe.dto.AdminSummaryResponse;
import com.task.smartgrocerybe.dto.AuthResponse;
import com.task.smartgrocerybe.dto.LoginRequest;
import com.task.smartgrocerybe.dto.RegisterRequest;
import com.task.smartgrocerybe.exception.BadRequestException;
import com.task.smartgrocerybe.exception.ResourceAlreadyExistsException;
import com.task.smartgrocerybe.exception.ResourceNotFoundException;
import com.task.smartgrocerybe.model.User;
import com.task.smartgrocerybe.model.enums.Role;
import com.task.smartgrocerybe.repository.UserRepository;
import com.task.smartgrocerybe.security.JwtService;
import com.task.smartgrocerybe.util.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthResponse register(RegisterRequest request, Role role) {
        var user = createUser(request, role);

        userRepository.save(user);
        String token = jwtService.generateToken(user);

        return mapToAuthResponse(user, token, role);
    }

    public AdminSummaryResponse createAdmin(RegisterRequest request) {
        var admin = createUser(request, Role.ADMIN);
        userRepository.save(admin);
        return mapToAdminSummary(admin);
    }

    public PageResponse<AdminSummaryResponse> getAdmins(
            int page, int size, String sortBy, String sortDir, String search) {
        return getAccountsByRole(Role.ADMIN, page, size, sortBy, sortDir, search);
    }

    public PageResponse<AdminSummaryResponse> getUsers(
            int page, int size, String sortBy, String sortDir, String search) {
        return getAccountsByRole(Role.USER, page, size, sortBy, sortDir, search);
    }

    public AdminSummaryResponse updateAccountStatus(Integer id, boolean active) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with id: " + id));

        if (user.getRole() == Role.SUPER_ADMIN) {
            throw new BadRequestException("Super admin accounts cannot be modified");
        }

        user.setIsActive(active);
        User savedUser = userRepository.save(user);
        return mapToAdminSummary(savedUser);
    }

    public AdminSummaryResponse getAccountSummary(Integer id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with id: " + id));
        return mapToAdminSummary(user);
    }

    private PageResponse<AdminSummaryResponse> getAccountsByRole(
            Role role, int page, int size, String sortBy, String sortDir, String search) {

        Pageable pageable = buildPageable(page, size, sortBy, sortDir);
        Page<User> users = userRepository.findByRoleWithSearch(
                role,
                isValid(search) ? search : null,
                pageable
        );

        return PageResponse.<AdminSummaryResponse>builder()
                .content(users.getContent().stream().map(this::mapToAdminSummary).toList())
                .pageNumber(users.getNumber())
                .pageSize(users.getSize())
                .totalElements(users.getTotalElements())
                .totalPages(users.getTotalPages())
                .last(users.isLast())
                .build();
    }

    private User createUser(RegisterRequest request, Role role) {
        validateUniqueUser(request);
        return mapToUser(request, role);
    }

    private void validateUniqueUser(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ResourceAlreadyExistsException("Email already exists");
        }

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new ResourceAlreadyExistsException("Username already exists");
        }
    }

    private User mapToUser(RegisterRequest request, Role role){
        return User.builder()
                .name(request.getName())
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(role)
                .isActive(true)
                .build();
    }

    private AuthResponse mapToAuthResponse(User user, String token, Role role) {
        Role finalRole = (role != null) ? role : user.getRole();

        if (finalRole == null) {
            throw new IllegalStateException("User role cannot be null");
        }

        return AuthResponse.builder()
                .token(token)
                .role(finalRole.name())
                .username(user.getUsername())
                .build();
    }

    private AdminSummaryResponse mapToAdminSummary(User user) {
        return AdminSummaryResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .username(user.getUsername())
                .email(user.getEmail())
                .active(user.getIsActive())
                .role(user.getRole().name())
                .createdAt(user.getCreatedAt())
                .build();
    }

    public AuthResponse login(LoginRequest request) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        var user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        String token = jwtService.generateToken(user);

        return mapToAuthResponse(user, token, null);
    }

    private Pageable buildPageable(
            int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        return PageRequest.of(page, size, sort);
    }

    private boolean isValid(String value) {
        return value != null && !value.isBlank();
    }

}
