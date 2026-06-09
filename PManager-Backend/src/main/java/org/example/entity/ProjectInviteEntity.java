package org.example.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.domain.InviteStatus;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "project_invites")
public class ProjectInviteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @Column(name = "project_id", nullable = false)
    private Long projectId;

    @Setter
    @Column(name = "project_name")
    private String projectName;

    @Setter
    @Column(name = "invited_by_email")
    private String invitedByEmail;

    @Setter
    @Column(name = "invitee_email", nullable = false)
    private String inviteeEmail;

    @Setter
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InviteStatus status = InviteStatus.PENDING;

    @Setter
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        if (this.status == null) this.status = InviteStatus.PENDING;
    }
}
