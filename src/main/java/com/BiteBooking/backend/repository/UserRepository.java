package com.BiteBooking.backend.repository;

import com.BiteBooking.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    @NonNull
    Optional<User> findById(@NonNull Long id);

    boolean existsById(@NonNull Long id);

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    Optional<User> findByGoogleProviderId(String googleProviderId);
}