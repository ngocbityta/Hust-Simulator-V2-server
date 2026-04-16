package com.hustsimulator.messaging.storage;

import com.hustsimulator.messaging.entity.StoredFile;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/api/storage")
@RequiredArgsConstructor
@Tag(name = "Storage API", description = "File storage operations")
public class StorageController {

    private final StorageService storageService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Upload a file to the storage provider")
    public StoredFile uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestHeader("X-User-Id") String userIdStr) throws IOException {

        UUID userId = UUID.fromString(userIdStr);

        return storageService.store(
                file.getOriginalFilename(),
                file.getContentType(),
                file.getBytes(),
                userId);
    }
}
