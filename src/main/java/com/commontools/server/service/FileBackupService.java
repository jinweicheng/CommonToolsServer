package com.commontools.server.service;

import com.commontools.server.model.BackupFile;
import com.commontools.server.model.User;
import com.commontools.server.repository.BackupFileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileBackupService {
    private final BackupFileRepository backupFileRepository;

    @Value("${file.storage.path}")
    private String storagePath;

    @Value("${file.storage.allowed-extensions}")
    private String allowedExtensions;

    @Transactional
    public BackupFile uploadFile(MultipartFile file, User user, String description, String category) throws IOException {
        // 验证文件扩展名
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            throw new RuntimeException("文件名不能为空");
        }

        String extension = getFileExtension(originalFilename).toLowerCase();
        if (!isAllowedExtension(extension)) {
            throw new RuntimeException("不支持的文件格式: " + extension);
        }

        // 创建存储目录
        String userDir = Paths.get(storagePath, String.valueOf(user.getId())).toString();
        File directory = new File(userDir);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        // 生成唯一文件名
        String storedFileName = generateStoredFileName(originalFilename);
        String filePath = Paths.get(userDir, storedFileName).toString();

        // 保存文件
        Path targetPath = Paths.get(filePath);
        Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

        // 自动分类
        if (category == null || category.isEmpty()) {
            category = determineCategory(extension);
        }

        // 保存文件记录
        BackupFile backupFile = new BackupFile();
        backupFile.setOriginalFileName(originalFilename);
        backupFile.setStoredFileName(storedFileName);
        backupFile.setFilePath(filePath);
        backupFile.setFileSize(file.getSize());
        backupFile.setFileType(file.getContentType());
        backupFile.setFileExtension(extension);
        backupFile.setUser(user);
        backupFile.setDescription(description);
        backupFile.setCategory(category);

        return backupFileRepository.save(backupFile);
    }

    public List<BackupFile> getUserFiles(User user) {
        return backupFileRepository.findByUser(user);
    }

    public List<BackupFile> getUserFilesByCategory(User user, String category) {
        return backupFileRepository.findByUserAndCategory(user, category);
    }

    public BackupFile getFileById(Long fileId, User user) {
        BackupFile file = backupFileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("文件不存在"));
        if (!file.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("无权访问该文件");
        }
        return file;
    }

    @Transactional
    public void deleteFile(Long fileId, User user) throws IOException {
        BackupFile file = getFileById(fileId, user);
        
        // 删除物理文件
        Path filePath = Paths.get(file.getFilePath());
        if (Files.exists(filePath)) {
            Files.delete(filePath);
        }
        
        // 删除数据库记录
        backupFileRepository.delete(file);
    }

    public File getFileAsFile(BackupFile backupFile) {
        return new File(backupFile.getFilePath());
    }

    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        return lastDotIndex > 0 ? filename.substring(lastDotIndex + 1) : "";
    }

    private boolean isAllowedExtension(String extension) {
        List<String> allowed = Arrays.asList(allowedExtensions.split(","));
        return allowed.contains(extension.toLowerCase());
    }

    private String generateStoredFileName(String originalFilename) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        String extension = getFileExtension(originalFilename);
        return timestamp + "_" + uuid + "." + extension;
    }

    private String determineCategory(String extension) {
        String ext = extension.toLowerCase();
        if (Arrays.asList("jpg", "jpeg", "png", "gif", "bmp", "webp").contains(ext)) {
            return "image";
        } else if (Arrays.asList("pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt").contains(ext)) {
            return "document";
        } else if (Arrays.asList("zip", "rar", "7z", "tar", "gz").contains(ext)) {
            return "archive";
        }
        return "other";
    }
}

