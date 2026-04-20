package com.anshik.flashsaleservice.dto;

import lombok.*;

@Data
public class RegisterRequest {
    private String username;
    private String email;
    private String password;
    private String role; // VENDOR, CUSTOMER, or ADMIN
}
