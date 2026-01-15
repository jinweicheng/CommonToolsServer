package com.commontools.server.controller;

import com.commontools.server.model.BackupFile;
import com.commontools.server.model.User;
import com.commontools.server.service.FileBackupService;
import com.commontools.server.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/files")
@RequiredArgsConstructor
public class FileBackupController {
    private final FileBackupService fileBackupService;
    private final UserService userService;

    @PostMapping("/upload")
    public ResponseEntity<BackupFile> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "category", required = false) String category,
            Authentication authentication) throws IOException {
        User user = getCurrentUser(authentication);
        BackupFile backupFile = fileBackupService.uploadFile(file, user, description, category);
        return ResponseEntity.ok(backupFile);
    }

    @GetMapping
    public ResponseEntity<List<BackupFile>> getUserFiles(
            @RequestParam(value = "category", required = false) String category,
            Authentication authentication) {
        User user = getCurrentUser(authentication);
        List<BackupFile> files = category != null ? 
                fileBackupService.getUserFilesByCategory(user, category) : 
                fileBackupService.getUserFiles(user);
        return ResponseEntity.ok(files);
    }

    @GetMapping("/{fileId}")
    public ResponseEntity<BackupFile> getFileInfo(@PathVariable Long fileId, Authentication authentication) {
        User user = getCurrentUser(authentication);
        BackupFile file = fileBackupService.getFileById(fileId, user);
        return ResponseEntity.ok(file);
    }

    @GetMapping("/{fileId}/download")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long fileId, Authentication authentication) {
        User user = getCurrentUser(authentication);
        BackupFile backupFile = fileBackupService.getFileById(fileId, user);
        File file = fileBackupService.getFileAsFile(backupFile);

        Resource resource = new FileSystemResource(file);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, 
                        "attachment; filename=\"" + backupFile.getOriginalFileName() + "\"")
                .contentType(MediaType.parseMediaType(backupFile.getFileType()))
                .body(resource);
    }

    @DeleteMapping("/{fileId}")
    public ResponseEntity<Void> deleteFile(@PathVariable Long fileId, Authentication authentication) throws IOException {
        User user = getCurrentUser(authentication);
        fileBackupService.deleteFile(fileId, user);
        return ResponseEntity.ok().build();
    }

    private User getCurrentUser(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return userService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("用户不存在"));
    }
}

