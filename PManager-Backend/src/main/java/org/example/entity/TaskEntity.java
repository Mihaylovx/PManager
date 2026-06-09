package org.example.entity;

import jakarta.persistence.*;
import lombok.Setter;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.domain.TaskStatus;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "tasks")
public class TaskEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    private String title;

    @Setter
    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "varchar(20) default 'TODO'")
    private TaskStatus status = TaskStatus.TODO;

    @Setter
    @Column(name = "hours_worked", columnDefinition = "double precision default 0")
    private Double hoursWorked = 0.0;

    @Setter
    @ManyToOne
    @JoinColumn(name = "assigned_to")
    private UserEntity assignedTo;

    @ManyToOne
    @JoinColumn(name = "project_id")
    @Setter
    private ProjectEntity project;

    public boolean isCompleted() {
        return status == TaskStatus.DONE;
    }
}
