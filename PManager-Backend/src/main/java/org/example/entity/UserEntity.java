package org.example.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "users")
public class UserEntity {

    @Id
    @Column(unique = true, nullable = false)
    private String email;

    @Setter
    private String firstName;

    @Setter
    private String lastName;

    @Setter
    private String password;

    @Setter
    @Column(name = "hourly_rate", columnDefinition = "double precision default 0")
    private Double hourlyRate = 0.0;

    public UserEntity(String email, String firstName, String lastName, String password) {
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.password = password;
    }
}
