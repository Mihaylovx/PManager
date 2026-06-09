package org.example.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.util.Map;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@Import(SecurityConfig.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    private final User user = User.builder()
            .email("alice@example.com")
            .firstName("Alice")
            .lastName("Smith")
            .hourlyRate(25.0)
            .build();

    @Test
    void getUser_existingEmail_returns200WithUserDTO() throws Exception {
        when(userService.findByEmail("alice@example.com")).thenReturn(Optional.of(user));

        mockMvc.perform(get("/api/users/alice@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("alice@example.com"))
                .andExpect(jsonPath("$.firstName").value("Alice"));
    }

    @Test
    void getUser_unknownEmail_returns404() throws Exception {
        when(userService.findByEmail("ghost@example.com")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/users/ghost@example.com"))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateHourlyRate_validRate_returns200WithUpdatedUser() throws Exception {
        User updated = User.builder()
                .email("alice@example.com")
                .firstName("Alice")
                .lastName("Smith")
                .hourlyRate(50.0)
                .build();
        when(userService.updateHourlyRate("alice@example.com", 50.0)).thenReturn(Optional.of(updated));

        mockMvc.perform(patch("/api/users/alice@example.com/hourly-rate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("hourlyRate", 50.0))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hourlyRate").value(50.0));
    }

    @Test
    void updateHourlyRate_negativeRate_returns400() throws Exception {
        mockMvc.perform(patch("/api/users/alice@example.com/hourly-rate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("hourlyRate", -10.0))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateHourlyRate_missingRate_returns400() throws Exception {
        mockMvc.perform(patch("/api/users/alice@example.com/hourly-rate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateHourlyRate_unknownEmail_returns404() throws Exception {
        when(userService.updateHourlyRate("ghost@example.com", 50.0)).thenReturn(Optional.empty());

        mockMvc.perform(patch("/api/users/ghost@example.com/hourly-rate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("hourlyRate", 50.0))))
                .andExpect(status().isNotFound());
    }
}
