package org.example.dal;

import org.example.domain.User;
import org.example.mapper.UserMapper;
import org.example.repository.UserRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class UserDao {

    private final UserRepository userRepository;

    public UserDao(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findById(email).map(UserMapper::toDomain);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsById(email);
    }

    public User save(User user) {
        return UserMapper.toDomain(userRepository.save(UserMapper.toEntity(user)));
    }
}
