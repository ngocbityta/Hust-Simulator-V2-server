package com.hustsimulator.social.storage;

import com.hustsimulator.social.entity.StoredFile;
import java.util.UUID;

public interface StorageService {
    StoredFile store(String originalName, String fileType, byte[] data, UUID uploadedBy);
    StoredFile findById(UUID id);
}
