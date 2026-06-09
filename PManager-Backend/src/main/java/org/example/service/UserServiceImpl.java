package org.example.service;

import org.example.dal.UserDao;
import org.example.domain.User;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    private final UserDao userDao;

    public UserServiceImpl(UserDao userDao) {
        this.userDao = userDao;
    }

    @Override
    public User register(User user) {
        if (userDao.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("Email already registered.");
        }
        return userDao.save(user);
    }

    @Override
    public Optional<User> login(String email, String password) {
        return userDao.findByEmail(email)
                .filter(u -> u.getPassword().equals(password));
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userDao.findByEmail(email);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userDao.existsByEmail(email);
    }

    @Override
    public Optional<User> updateHourlyRate(String email, Double hourlyRate) {
        return userDao.updateHourlyRate(email, hourlyRate);
    }
}
