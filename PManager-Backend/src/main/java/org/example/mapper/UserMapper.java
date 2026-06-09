package org.example.mapper;

import org.example.DTO.UserDTO;
import org.example.domain.User;
import org.example.entity.UserEntity;

public class UserMapper {

    private UserMapper() {}

    public static UserDTO toDTO(User user) {
        return UserDTO.builder()
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .hourlyRate(user.getHourlyRate())
                .build();
    }

    public static User toDomain(UserDTO dto) {
        return User.builder()
                .email(dto.getEmail())
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .password(dto.getPassword())
                .hourlyRate(dto.getHourlyRate())
                .build();
    }

    public static User toDomain(UserEntity entity) {
        return User.builder()
                .email(entity.getEmail())
                .firstName(entity.getFirstName())
                .lastName(entity.getLastName())
                .password(entity.getPassword())
                .hourlyRate(entity.getHourlyRate())
                .build();
    }

    public static UserEntity toEntity(User user) {
        UserEntity entity = new UserEntity(
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getPassword()
        );
        if (user.getHourlyRate() != null) {
            entity.setHourlyRate(user.getHourlyRate());
        }
        return entity;
    }
}
