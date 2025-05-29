package com.NE.chloe_Java.dto.auth;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterResponse {
    private String id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private List<String> roles;
}
