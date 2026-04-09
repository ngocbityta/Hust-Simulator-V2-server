package com.hustsimulator.context.storage;

import com.hustsimulator.context.entity.StoredFile;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StorageServiceTest {

    @Mock private StorageRepository storageRepository;
    @Mock private StorageProvider storageProvider;

    @InjectMocks private StorageServiceImpl storageService;

    @Test
    void store_shouldUploadAndSaveMetadata() {
        UUID userId = UUID.randomUUID();
        byte[] data = "test-content".getBytes();
        String mockUrl = "http://mock.url/test.txt";
        
        when(storageProvider.upload(anyString(), anyString(), any())).thenReturn(mockUrl);
        when(storageRepository.save(any(StoredFile.class))).thenAnswer(i -> i.getArgument(0));

        StoredFile result = storageService.store("test.txt", "text/plain", data, userId);

        assertThat(result.getFileUrl()).isEqualTo(mockUrl);
        assertThat(result.getFileSize()).isEqualTo((long) data.length);
        assertThat(result.getUploadedBy()).isEqualTo(userId);
        
        verify(storageProvider).upload("test.txt", "text/plain", data);
        verify(storageRepository).save(any(StoredFile.class));
    }
}
