package com.hustsimulator.context.storage;

import com.hustsimulator.context.entity.StoredFile;
import java.util.UUID;

public interface StorageService {
    StoredFile store(String originalName, String fileType, byte[] data, UUID uploadedBy);
    StoredFile findById(UUID id);
}
