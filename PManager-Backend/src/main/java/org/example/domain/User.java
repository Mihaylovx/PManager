package org.example.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Builder
public class User {
    private String email;
    @Setter
    private String firstName;
    @Setter
    private String lastName;
    @Setter
    private String password;
    @Setter
    private Double hourlyRate;
}
