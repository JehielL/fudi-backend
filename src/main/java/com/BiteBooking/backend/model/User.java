package com.BiteBooking.backend.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class User {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    private String firstName;
    private String lastName;
    private LocalDate birthdayDate;

    @Column(unique = true, nullable = false)
    private String email;

    private String password;
    private String phone;

    @Enumerated(EnumType.STRING)
    private Role role;

    private String imgUser;
    private String city;
    private String aboutMe;

    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "user_auth_providers", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "auth_provider")
    @Builder.Default
    private Set<AuthProvider> authProviders = new HashSet<>();

    @Column(unique = true)
    private String googleProviderId;

    public boolean hasAuthProvider(AuthProvider provider) {
        return authProviders != null && authProviders.contains(provider);
    }

    public void addAuthProvider(AuthProvider provider) {
        if (authProviders == null) {
            authProviders = new HashSet<>();
        }
        authProviders.add(provider);
    }

    public boolean isLocalUser() {
        return hasAuthProvider(AuthProvider.LOCAL);
    }

    public boolean isGoogleUser() {
        return hasAuthProvider(AuthProvider.GOOGLE);
    }
}