package org.example.DTO;

import lombok.Builder;
import lombok.Data;
import org.example.domain.InviteStatus;

import java.time.LocalDateTime;

@Data
@Builder
public class ProjectInviteDTO {
    private Long id;
    private Long projectId;
    private String projectName;
    private String invitedByEmail;
    private String inviteeEmail;
    private InviteStatus status;
    private LocalDateTime createdAt;
}
