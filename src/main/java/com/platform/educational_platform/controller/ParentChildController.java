package com.platform.educational_platform.controller;

import com.platform.educational_platform.dto.ParentChildDTO;
import com.platform.educational_platform.model.User;
import com.platform.educational_platform.service.ParentChildService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/parent-child")
@RequiredArgsConstructor
public class ParentChildController {

    private final ParentChildService parentChildService;

    @PostMapping("/link")
    public ResponseEntity<Void> link(
            @RequestBody ParentChildDTO.LinkRequest req,
            @AuthenticationPrincipal User currentUser) {
        parentChildService.linkParentChild(req, currentUser);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/parent/{parentId}/child/{childId}")
    public ResponseEntity<Void> unlink(
            @PathVariable Long parentId,
            @PathVariable Long childId,
            @AuthenticationPrincipal User currentUser) {
        parentChildService.unlinkParentChild(parentId, childId, currentUser);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/all")
    public ResponseEntity<List<ParentChildDTO.ParentWithChildren>> getAll(
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(parentChildService.getAllAssociations(currentUser));
    }

    @GetMapping("/parent/{parentId}")
    public ResponseEntity<ParentChildDTO.ParentWithChildren> getParentDetails(
            @PathVariable Long parentId,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(parentChildService.getParentDetails(parentId, currentUser));
    }

    @GetMapping("/my-children")
    public ResponseEntity<List<ParentChildDTO.PersonInfo>> getMyChildren(
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(parentChildService.getMyChildren(currentUser));
    }

    @GetMapping("/my-parents")
    public ResponseEntity<List<ParentChildDTO.PersonInfo>> getMyParents(
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(parentChildService.getMyParents(currentUser));
    }
}