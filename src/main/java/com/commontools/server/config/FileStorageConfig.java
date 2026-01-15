package com.commontools.server.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.io.File;

@Configuration
public class FileStorageConfig {
    @Value("${file.storage.path}")
    private String storagePath;

    @PostConstruct
    public void init() {
        File directory = new File(storagePath);
        if (!directory.exists()) {
            directory.mkdirs();
        }
    }
}

