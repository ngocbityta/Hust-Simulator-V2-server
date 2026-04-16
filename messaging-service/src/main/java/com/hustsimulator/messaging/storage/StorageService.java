package com.hustsimulator.messaging.storage;

import com.hustsimulator.messaging.entity.StoredFile;
import java.util.UUID;

public interface StorageService {
    StoredFile store(String originalName, String fileType, byte[] data, UUID uploadedBy);
    StoredFile findById(UUID id);
}
