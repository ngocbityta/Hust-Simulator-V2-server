package com.hustsimulator.social.storage;

import com.hustsimulator.social.entity.StoredFile;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface StorageRepository extends JpaRepository<StoredFile, UUID> {
}
