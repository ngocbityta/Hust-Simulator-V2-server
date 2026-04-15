package com.hustsimulator.context.storage;

import com.hustsimulator.context.entity.StoredFile;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.hustsimulator.context.entity.User;

import java.io.IOException;

@RestController
@RequestMapping("/api/storage")
@RequiredArgsConstructor
@Tag(name = "Storage API", description = "File storage operations")
public class StorageController {

    private final StorageService storageService;
    private final com.hustsimulator.context.user.UserRepository userRepository;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Upload a file to the storage provider")
    public StoredFile uploadFile(
            @RequestParam("file") MultipartFile file,
            Authentication authentication) throws IOException {

        String phonenumber = authentication.getName();
        User user = userRepository.findByPhonenumber(phonenumber)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return storageService.store(
                file.getOriginalFilename(),
                file.getContentType(),
                file.getBytes(),
                user.getId());
    }
}
