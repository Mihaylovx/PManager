package org.example.service;

import org.example.domain.User;

import java.util.Optional;

public interface UserService {
    User register(User user);
    Optional<User> login(String email, String password);
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    Optional<User> updateHourlyRate(String email, Double hourlyRate);
}
