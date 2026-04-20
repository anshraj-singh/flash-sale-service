package com.anshik.flashsaleservice.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    // We use Enum for roles to keep it strict
    @Enumerated(EnumType.STRING)
    private Role role;

    public enum Role {
        ADMIN, VENDOR, CUSTOMER
    }
}