package org.example.controller;

import org.example.mapper.UserMapper;
import org.example.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{email}")
    public ResponseEntity<?> getUser(@PathVariable String email) {
        return userService.findByEmail(email)
                .map(UserMapper::toDTO)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PatchMapping("/{email}/hourly-rate")
    public ResponseEntity<?> updateHourlyRate(@PathVariable String email, @RequestBody Map<String, Double> body) {
        Double rate = body.get("hourlyRate");
        if (rate == null || rate < 0) {
            return ResponseEntity.badRequest().body("Invalid hourly rate.");
        }
        return userService.updateHourlyRate(email, rate)
                .map(UserMapper::toDTO)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
