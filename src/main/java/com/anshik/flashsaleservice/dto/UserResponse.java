package com.anshik.flashsaleservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class UserResponse {
    private String token;
    private String type;
    private String role;
    private String username;
}