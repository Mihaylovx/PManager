package org.example.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "projects")
public class ProjectEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    private String name;
    @Setter
    private String description;
    @Setter
    private LocalDateTime lastUpdated;

    @Setter
    @Column(name = "manager_email")
    private String managerEmail;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "project_members",
            joinColumns = @JoinColumn(name = "project_id")
    )
    @Column(name = "member_email")
    @Setter
    private Set<String> memberEmails = new HashSet<>();

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Setter
    private List<TaskEntity> tasks = new ArrayList<>();

    @PrePersist
    @PreUpdate
    public void updateTimestamp() {
        this.lastUpdated = LocalDateTime.now();
    }
}
