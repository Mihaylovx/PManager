package org.example.domain;

import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Builder
public class Project {
    private Long id;
    @Setter
    private String name;
    @Setter
    private String description;
    @Setter
    private LocalDateTime lastUpdated;
    @Setter
    private String managerEmail;
    @Setter
    @Builder.Default
    private Set<String> memberEmails = new HashSet<>();
    @Setter
    private List<Task> tasks;
}
