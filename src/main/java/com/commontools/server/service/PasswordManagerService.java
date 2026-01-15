package com.commontools.server.service;

import com.commontools.server.dto.PasswordEntryRequest;
import com.commontools.server.dto.PasswordEntryResponse;
import com.commontools.server.model.PasswordEntry;
import com.commontools.server.model.User;
import com.commontools.server.repository.PasswordEntryRepository;
import com.commontools.server.util.EncryptionUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.SecretKey;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PasswordManagerService {
    private final PasswordEntryRepository passwordEntryRepository;

    @Transactional
    public PasswordEntry createPasswordEntry(PasswordEntryRequest request, User user, String masterPassword) throws Exception {
        // 使用主密码派生加密密钥
        String salt = user.getId() + user.getUsername();
        SecretKey key = EncryptionUtil.deriveKey(masterPassword, salt);

        PasswordEntry entry = new PasswordEntry();
        entry.setTitle(request.getTitle());
        entry.setWebsite(request.getWebsite());
        entry.setEncryptedUsername(EncryptionUtil.encrypt(request.getUsername(), key));
        entry.setEncryptedPassword(EncryptionUtil.encrypt(request.getPassword(), key));
        entry.setEncryptedNotes(request.getNotes() != null ? 
                EncryptionUtil.encrypt(request.getNotes(), key) : null);
        entry.setCategory(request.getCategory());
        entry.setUser(user);

        return passwordEntryRepository.save(entry);
    }

    public List<PasswordEntry> getUserPasswordEntries(User user) {
        return passwordEntryRepository.findByUser(user);
    }

    public List<PasswordEntry> getUserPasswordEntriesByCategory(User user, String category) {
        return passwordEntryRepository.findByUserAndCategory(user, category);
    }

    public PasswordEntry getPasswordEntryById(Long entryId, User user) {
        PasswordEntry entry = passwordEntryRepository.findById(entryId)
                .orElseThrow(() -> new RuntimeException("密码条目不存在"));
        if (!entry.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("无权访问该密码条目");
        }
        return entry;
    }

    public PasswordEntryResponse getDecryptedPasswordEntry(Long entryId, User user, String masterPassword) throws Exception {
        PasswordEntry entry = getPasswordEntryById(entryId, user);
        return convertToResponse(entry, masterPassword);
    }

    public List<PasswordEntryResponse> getDecryptedPasswordEntries(User user, String masterPassword) throws Exception {
        List<PasswordEntry> entries = getUserPasswordEntries(user);
        return entries.stream()
                .map(entry -> {
                    try {
                        return convertToResponse(entry, masterPassword);
                    } catch (Exception e) {
                        throw new RuntimeException("解密失败: " + e.getMessage());
                    }
                })
                .collect(Collectors.toList());
    }

    public List<PasswordEntryResponse> getDecryptedPasswordEntriesByCategory(User user, String category, String masterPassword) throws Exception {
        List<PasswordEntry> entries = getUserPasswordEntriesByCategory(user, category);
        return entries.stream()
                .map(entry -> {
                    try {
                        return convertToResponse(entry, masterPassword);
                    } catch (Exception e) {
                        throw new RuntimeException("解密失败: " + e.getMessage());
                    }
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public PasswordEntry updatePasswordEntry(Long entryId, PasswordEntryRequest request, User user, String masterPassword) throws Exception {
        PasswordEntry entry = getPasswordEntryById(entryId, user);
        String salt = user.getId() + user.getUsername();
        SecretKey key = EncryptionUtil.deriveKey(masterPassword, salt);

        entry.setTitle(request.getTitle());
        entry.setWebsite(request.getWebsite());
        entry.setEncryptedUsername(EncryptionUtil.encrypt(request.getUsername(), key));
        entry.setEncryptedPassword(EncryptionUtil.encrypt(request.getPassword(), key));
        entry.setEncryptedNotes(request.getNotes() != null ? 
                EncryptionUtil.encrypt(request.getNotes(), key) : null);
        entry.setCategory(request.getCategory());

        return passwordEntryRepository.save(entry);
    }

    @Transactional
    public void deletePasswordEntry(Long entryId, User user) {
        PasswordEntry entry = getPasswordEntryById(entryId, user);
        passwordEntryRepository.delete(entry);
    }

    private PasswordEntryResponse convertToResponse(PasswordEntry entry, String masterPassword) throws Exception {
        String salt = entry.getUser().getId() + entry.getUser().getUsername();
        SecretKey key = EncryptionUtil.deriveKey(masterPassword, salt);

        PasswordEntryResponse response = new PasswordEntryResponse();
        response.setId(entry.getId());
        response.setTitle(entry.getTitle());
        response.setWebsite(entry.getWebsite());
        response.setCategory(entry.getCategory());
        response.setUserId(entry.getUser().getId());
        response.setCreatedAt(entry.getCreatedAt());
        response.setUpdatedAt(entry.getUpdatedAt());

        // 解密敏感信息
        response.setUsername(EncryptionUtil.decrypt(entry.getEncryptedUsername(), key));
        response.setPassword(EncryptionUtil.decrypt(entry.getEncryptedPassword(), key));
        if (entry.getEncryptedNotes() != null) {
            response.setNotes(EncryptionUtil.decrypt(entry.getEncryptedNotes(), key));
        }

        return response;
    }
}

