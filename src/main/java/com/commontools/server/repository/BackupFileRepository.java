package com.commontools.server.repository;

import com.commontools.server.model.BackupFile;
import com.commontools.server.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BackupFileRepository extends JpaRepository<BackupFile, Long> {
    List<BackupFile> findByUser(User user);
    List<BackupFile> findByUserAndCategory(User user, String category);
    void deleteByUser(User user);
}

