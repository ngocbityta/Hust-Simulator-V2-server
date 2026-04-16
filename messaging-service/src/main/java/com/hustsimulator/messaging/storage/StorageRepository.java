package com.hustsimulator.messaging.storage;

import com.hustsimulator.messaging.entity.StoredFile;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface StorageRepository extends JpaRepository<StoredFile, UUID> {
}
