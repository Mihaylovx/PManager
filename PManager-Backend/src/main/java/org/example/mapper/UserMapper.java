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
                .build();
    }

    public static User toDomain(UserDTO dto) {
        return User.builder()
                .email(dto.getEmail())
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .password(dto.getPassword())
                .build();
    }

    public static User toDomain(UserEntity entity) {
        return User.builder()
                .email(entity.getEmail())
                .firstName(entity.getFirstName())
                .lastName(entity.getLastName())
                .password(entity.getPassword())
                .build();
    }

    public static UserEntity toEntity(User user) {
        return new UserEntity(
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getPassword()
        );
    }
}
