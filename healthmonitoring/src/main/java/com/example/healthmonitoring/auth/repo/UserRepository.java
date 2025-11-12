package com.example.healthmonitoring.auth.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.healthmonitoring.auth.entity.User;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);
}
