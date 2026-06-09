package org.example.service;
import org.example.dal.UserDao;
import org.example.domain.User;
import org.example.service.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserDao userDao;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .email("test@example.com")
                .firstName("John")
                .lastName("Doe")
                .password("secret123")
                .build();
    }

    @Test
    void register_newEmail_returnsRegisteredUser() {
        when(userDao.existsByEmail(user.getEmail())).thenReturn(false);
        when(userDao.save(any(User.class))).thenReturn(user);

        User result = userService.register(user);

        assertNotNull(result);
        assertEquals("test@example.com", result.getEmail());
        verify(userDao, times(1)).save(any(User.class));
    }

    @Test
    void register_duplicateEmail_throwsIllegalArgumentException() {
        when(userDao.existsByEmail(user.getEmail())).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> userService.register(user));
        verify(userDao, never()).save(any(User.class));
    }

    @Test
    void login_correctCredentials_returnsUser() {
        when(userDao.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        Optional<User> result = userService.login("test@example.com", "secret123");

        assertTrue(result.isPresent());
        assertEquals("test@example.com", result.get().getEmail());
    }

    @Test
    void login_wrongPassword_returnsEmpty() {
        when(userDao.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        Optional<User> result = userService.login("test@example.com", "wrongpassword");

        assertTrue(result.isEmpty());
    }

    @Test
    void login_unknownEmail_returnsEmpty() {
        when(userDao.findByEmail("nobody@example.com")).thenReturn(Optional.empty());

        Optional<User> result = userService.login("nobody@example.com", "any");

        assertTrue(result.isEmpty());
    }

    @Test
    void findByEmail_existingEmail_returnsUser() {
        when(userDao.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        Optional<User> result = userService.findByEmail("test@example.com");

        assertTrue(result.isPresent());
        assertEquals("John", result.get().getFirstName());
    }

    @Test
    void findByEmail_unknownEmail_returnsEmpty() {
        when(userDao.findByEmail("ghost@example.com")).thenReturn(Optional.empty());

        Optional<User> result = userService.findByEmail("ghost@example.com");

        assertTrue(result.isEmpty());
    }

    @Test
    void existsByEmail_existingEmail_returnsTrue() {
        when(userDao.existsByEmail(user.getEmail())).thenReturn(true);

        assertTrue(userService.existsByEmail("test@example.com"));
    }

    @Test
    void existsByEmail_unknownEmail_returnsFalse() {
        when(userDao.existsByEmail("ghost@example.com")).thenReturn(false);

        assertFalse(userService.existsByEmail("ghost@example.com"));
    }

    @Test
    void updateHourlyRate_existingUser_returnsUpdatedUser() {
        User updated = User.builder()
                .email("test@example.com")
                .firstName("John")
                .lastName("Doe")
                .hourlyRate(50.0)
                .build();
        when(userDao.updateHourlyRate("test@example.com", 50.0)).thenReturn(Optional.of(updated));

        Optional<User> result = userService.updateHourlyRate("test@example.com", 50.0);

        assertTrue(result.isPresent());
        assertEquals(50.0, result.get().getHourlyRate());
    }

    @Test
    void updateHourlyRate_nonExistentUser_returnsEmpty() {
        when(userDao.updateHourlyRate("ghost@example.com", 50.0)).thenReturn(Optional.empty());

        Optional<User> result = userService.updateHourlyRate("ghost@example.com", 50.0);

        assertTrue(result.isEmpty());
    }
}
