package org.example.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.DTO.UserDTO;
import org.example.config.SecurityConfig;
import org.example.domain.User;
import org.example.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    private final User registeredUser = User.builder()
            .email("alice@example.com")
            .firstName("Alice")
            .lastName("Smith")
            .build();

    @Test
    void register_validUser_returns201WithUserDTO() throws Exception {
        when(userService.register(any(User.class))).thenReturn(registeredUser);

        UserDTO dto = UserDTO.builder()
                .email("alice@example.com")
                .firstName("Alice")
                .lastName("Smith")
                .password("password123")
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("alice@example.com"))
                .andExpect(jsonPath("$.firstName").value("Alice"));
    }

    @Test
    void register_duplicateEmail_returns409() throws Exception {
        when(userService.register(any(User.class))).thenThrow(new IllegalArgumentException("Email already registered."));

        UserDTO dto = UserDTO.builder()
                .email("alice@example.com")
                .firstName("Alice")
                .lastName("Smith")
                .password("password123")
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isConflict());
    }

    @Test
    void register_invalidBody_returns400() throws Exception {
        String invalidBody = "{\"email\":\"not-an-email\",\"firstName\":\"\",\"lastName\":\"Smith\",\"password\":\"pw\"}";

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_validCredentials_returns200WithUserDTO() throws Exception {
        when(userService.login("alice@example.com", "password123")).thenReturn(Optional.of(registeredUser));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"alice@example.com\",\"password\":\"password123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("alice@example.com"));
    }

    @Test
    void login_invalidCredentials_returns401() throws Exception {
        when(userService.login("alice@example.com", "wrongpass")).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"alice@example.com\",\"password\":\"wrongpass\"}"))
                .andExpect(status().isUnauthorized());
    }
}
