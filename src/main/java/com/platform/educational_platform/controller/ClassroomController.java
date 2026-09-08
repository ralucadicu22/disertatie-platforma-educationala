package com.platform.educational_platform.controller;

import com.platform.educational_platform.dto.ClassroomDTO;
import com.platform.educational_platform.model.User;
import com.platform.educational_platform.service.ClassroomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/classrooms")
@RequiredArgsConstructor
public class ClassroomController {

    private final ClassroomService classroomService;

    @PostMapping
    public ResponseEntity<ClassroomDTO.Detail> create(
            @RequestBody ClassroomDTO.CreateRequest req,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(classroomService.createClassroom(req, currentUser));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClassroomDTO.Detail> update(
            @PathVariable Long id,
            @RequestBody ClassroomDTO.UpdateRequest req,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(classroomService.updateClassroom(id, req, currentUser));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {
        classroomService.deleteClassroom(id, currentUser);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClassroomDTO.Detail> getOne(@PathVariable Long id) {
        return ResponseEntity.ok(classroomService.getClassroom(id));
    }

    @GetMapping
    public ResponseEntity<List<ClassroomDTO.Summary>> getAll() {
        return ResponseEntity.ok(classroomService.getAllClassrooms());
    }

    @GetMapping("/my")
    public ResponseEntity<List<ClassroomDTO.Summary>> getMy(
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(classroomService.getMyClassrooms(currentUser));
    }

    @PostMapping("/{id}/teachers")
    public ResponseEntity<ClassroomDTO.Detail> addTeacher(
            @PathVariable Long id,
            @RequestBody ClassroomDTO.MemberRequest req,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(classroomService.addTeacher(id, req.getUserId(), currentUser));
    }

    @DeleteMapping("/{id}/teachers/{userId}")
    public ResponseEntity<ClassroomDTO.Detail> removeTeacher(
            @PathVariable Long id,
            @PathVariable Long userId,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(classroomService.removeTeacher(id, userId, currentUser));
    }

    @PostMapping("/{id}/students")
    public ResponseEntity<ClassroomDTO.Detail> addStudent(
            @PathVariable Long id,
            @RequestBody ClassroomDTO.MemberRequest req,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(classroomService.addStudent(id, req.getUserId(), currentUser));
    }

    @DeleteMapping("/{id}/students/{userId}")
    public ResponseEntity<ClassroomDTO.Detail> removeStudent(
            @PathVariable Long id,
            @PathVariable Long userId,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(classroomService.removeStudent(id, userId, currentUser));
    }
}