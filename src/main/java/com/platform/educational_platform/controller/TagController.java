package com.platform.educational_platform.controller;


import com.platform.educational_platform.dto.metadata.CreateTagRequest;
import com.platform.educational_platform.dto.metadata.TagDTO;
import com.platform.educational_platform.service.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tags")
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;

    @GetMapping
    public ResponseEntity<List<TagDTO>> list() {
        return ResponseEntity.ok(tagService.listAll());
    }

    @PostMapping
    public ResponseEntity<TagDTO> create(@RequestBody CreateTagRequest req) {
        return ResponseEntity.ok(tagService.create(req));
    }

    @DeleteMapping("/{tagId}")
    public ResponseEntity<Void> delete(@PathVariable Long tagId) {
        tagService.delete(tagId);
        return ResponseEntity.noContent().build();
    }
}