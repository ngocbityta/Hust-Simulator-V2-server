package com.hustsimulator.context.storage;

import com.hustsimulator.context.common.ResourceNotFoundException;
import com.hustsimulator.context.entity.StoredFile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class StorageServiceImpl implements StorageService {

    private final StorageRepository storageRepository;
    private final StorageProvider storageProvider;

    @Override
    public StoredFile store(String originalName, String fileType, byte[] data, UUID uploadedBy) {
        String fileUrl = storageProvider.upload(originalName, fileType, data);

        StoredFile file = StoredFile.builder()
                .originalName(originalName)
                .fileType(fileType)
                .fileUrl(fileUrl)
                .fileSize(data != null ? (long) data.length : 0L)
                .uploadedBy(uploadedBy)
                .build();

        file = storageRepository.save(file);
        log.info("StorageService: Stored file '{}' -> {}", originalName, fileUrl);
        return file;
    }

    @Override
    public StoredFile findById(UUID id) {
        return storageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("StoredFile", id));
    }
}
