package com.hustsimulator.context.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Local implementation of StorageProvider.
 */
@Component
@Slf4j
public class LocalStorageProvider implements StorageProvider {

    @Override
    public String upload(String originalName, String fileType, byte[] data) {
        log.info("Mock upload using LocalStorageProvider: file={}, type={}", originalName, fileType);
        return "https://storage.hust.edu.vn/mock/" + originalName;
    }
}
