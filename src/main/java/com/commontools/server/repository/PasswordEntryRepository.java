package com.commontools.server.repository;

import com.commontools.server.model.PasswordEntry;
import com.commontools.server.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PasswordEntryRepository extends JpaRepository<PasswordEntry, Long> {
    List<PasswordEntry> findByUser(User user);
    List<PasswordEntry> findByUserAndCategory(User user, String category);
    void deleteByUser(User user);
}

