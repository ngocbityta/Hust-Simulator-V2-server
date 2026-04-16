package com.hustsimulator.messaging.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.UUID;

@Component
@ConditionalOnProperty(name = "hustsimulator.storage.provider", havingValue = "local", matchIfMissing = true)
@Slf4j
public class LocalStorageProvider implements StorageProvider {

    @Value("${hustsimulator.storage.local.upload-dir:./uploads}")
    private String uploadDir;

    @Value("${hustsimulator.storage.local.base-url:http://localhost:8082/uploads/}")
    private String baseUrl;

    @PostConstruct
    public void init() {
        try {
            Path path = Paths.get(uploadDir);
            if (!Files.exists(path)) {
                Files.createDirectories(path);
                log.info("Created local uploads directory at {}", path.toAbsolutePath());
            }
        } catch (IOException e) {
            log.error("Could not initialize local storage directory", e);
        }
    }

    @Override
    public String upload(String originalName, String fileType, byte[] data) {
        try {
            String extension = "";
            int dotIndex = originalName.lastIndexOf('.');
            if (dotIndex >= 0) {
                extension = originalName.substring(dotIndex);
            }
            String fileName = UUID.randomUUID() + extension;
            Path filePath = Paths.get(uploadDir, fileName);

            Files.write(filePath, data, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            log.info("Saved local file: {} to {}", originalName, filePath.toAbsolutePath());

            String returnUrl = baseUrl;
            if (!returnUrl.endsWith("/")) {
                returnUrl += "/";
            }
            return returnUrl + fileName;
        } catch (IOException e) {
            log.error("Failed to store file locally", e);
            throw new RuntimeException("Could not store file", e);
        }
    }
}
