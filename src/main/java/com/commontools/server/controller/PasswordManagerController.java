package com.commontools.server.controller;

import com.commontools.server.dto.PasswordEntryRequest;
import com.commontools.server.dto.PasswordEntryResponse;
import com.commontools.server.model.PasswordEntry;
import com.commontools.server.model.User;
import com.commontools.server.service.PasswordManagerService;
import com.commontools.server.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/passwords")
@RequiredArgsConstructor
public class PasswordManagerController {
    private final PasswordManagerService passwordManagerService;
    private final UserService userService;

    @PostMapping
    public ResponseEntity<PasswordEntry> createPasswordEntry(
            @Valid @RequestBody PasswordEntryRequest request,
            @RequestHeader("X-Master-Password") String masterPassword,
            Authentication authentication) throws Exception {
        User user = getCurrentUser(authentication);
        PasswordEntry entry = passwordManagerService.createPasswordEntry(request, user, masterPassword);
        return ResponseEntity.ok(entry);
    }

    @GetMapping
    public ResponseEntity<List<PasswordEntryResponse>> getPasswordEntries(
            @RequestParam(value = "category", required = false) String category,
            @RequestHeader("X-Master-Password") String masterPassword,
            Authentication authentication) throws Exception {
        User user = getCurrentUser(authentication);
        List<PasswordEntryResponse> entries = category != null ? 
                passwordManagerService.getDecryptedPasswordEntriesByCategory(user, category, masterPassword) : 
                passwordManagerService.getDecryptedPasswordEntries(user, masterPassword);
        return ResponseEntity.ok(entries);
    }

    @GetMapping("/{entryId}")
    public ResponseEntity<PasswordEntryResponse> getPasswordEntry(
            @PathVariable Long entryId,
            @RequestHeader("X-Master-Password") String masterPassword,
            Authentication authentication) throws Exception {
        User user = getCurrentUser(authentication);
        PasswordEntryResponse entry = passwordManagerService.getDecryptedPasswordEntry(entryId, user, masterPassword);
        return ResponseEntity.ok(entry);
    }

    @PutMapping("/{entryId}")
    public ResponseEntity<PasswordEntry> updatePasswordEntry(
            @PathVariable Long entryId,
            @Valid @RequestBody PasswordEntryRequest request,
            @RequestHeader("X-Master-Password") String masterPassword,
            Authentication authentication) throws Exception {
        User user = getCurrentUser(authentication);
        PasswordEntry entry = passwordManagerService.updatePasswordEntry(entryId, request, user, masterPassword);
        return ResponseEntity.ok(entry);
    }

    @DeleteMapping("/{entryId}")
    public ResponseEntity<Void> deletePasswordEntry(@PathVariable Long entryId, Authentication authentication) {
        User user = getCurrentUser(authentication);
        passwordManagerService.deletePasswordEntry(entryId, user);
        return ResponseEntity.ok().build();
    }

    private User getCurrentUser(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return userService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("用户不存在"));
    }
}

