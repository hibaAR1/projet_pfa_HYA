package com.exemple.pfa.repository;

import com.exemple.pfa.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Find user by email (JPA will automatically implement this)
    Optional<User> findByEmail(String email);

    // Check if a user with the given email already exists (JPA will implement this too)
    boolean existsByEmail(String email);

}