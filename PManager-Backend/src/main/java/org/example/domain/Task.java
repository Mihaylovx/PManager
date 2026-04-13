package org.example.domain;

import lombok.*;

@Getter
@Builder
public class Task {
    private Long id;
    @Setter
    private String title;
    @Setter
    private boolean completed;
    private Long projectId;
}