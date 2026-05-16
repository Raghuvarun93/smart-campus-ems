package com.campus.ems.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "app_users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    private String role; // ROLE_ADMIN, ROLE_STUDENT

    private String fullName;

    @Column(unique = true)
    private String email;
}
