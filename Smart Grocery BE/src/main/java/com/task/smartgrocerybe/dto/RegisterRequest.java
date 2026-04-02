package com.task.smartgrocerybe.dto;

import com.task.smartgrocerybe.model.enums.Role;

import lombok.Data;

// RegisterRequest.java
@Data
public class RegisterRequest {
    private String name;
    private String username;
    private String email;
    private String password;
    private Role role; // ADMIN or USER
}

