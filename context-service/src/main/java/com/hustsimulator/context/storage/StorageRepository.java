package com.hustsimulator.context.storage;

import com.hustsimulator.context.entity.StoredFile;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface StorageRepository extends JpaRepository<StoredFile, UUID> {
}
