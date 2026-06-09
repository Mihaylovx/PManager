package org.example.controller;

import org.example.DTO.ProjectInviteDTO;
import org.example.service.ProjectInviteService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/invites")
public class ProjectInviteController {

    private final ProjectInviteService inviteService;

    public ProjectInviteController(ProjectInviteService inviteService) {
        this.inviteService = inviteService;
    }

    @GetMapping
    public ResponseEntity<List<ProjectInviteDTO>> getPendingInvites(@RequestParam String user) {
        return ResponseEntity.ok(inviteService.getPendingInvitesForUser(user));
    }

    @PostMapping("/{id}/accept")
    public ResponseEntity<ProjectInviteDTO> acceptInvite(@PathVariable Long id) {
        return inviteService.acceptInvite(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/decline")
    public ResponseEntity<ProjectInviteDTO> declineInvite(@PathVariable Long id) {
        return inviteService.declineInvite(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
