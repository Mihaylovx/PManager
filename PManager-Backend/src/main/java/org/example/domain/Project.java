package org.example.domain;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

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
    private List<Task> tasks;
}