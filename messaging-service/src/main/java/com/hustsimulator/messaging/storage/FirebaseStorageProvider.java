package com.hustsimulator.messaging.storage;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.StorageClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Component
@ConditionalOnProperty(name = "hustsimulator.storage.provider", havingValue = "firebase")
@Slf4j
public class FirebaseStorageProvider implements StorageProvider {

    @Value("${hustsimulator.storage.firebase.bucket:}")
    private String firebaseBucket;

    @PostConstruct
    public void init() {
        try {
            if (FirebaseApp.getApps().isEmpty()) {
                InputStream serviceAccount = null;
                try {
                    serviceAccount = new FileInputStream("serviceAccountKey.json");
                } catch (Exception e) {
                    log.warn("serviceAccountKey.json not found, falling back to default credentials");
                }

                GoogleCredentials credentials = (serviceAccount != null)
                        ? GoogleCredentials.fromStream(serviceAccount)
                        : GoogleCredentials.getApplicationDefault();

                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(credentials)
                        .setStorageBucket(firebaseBucket)
                        .build();

                FirebaseApp.initializeApp(options);
                log.info("Firebase app initialized for Storage Provider");
            }
        } catch (IOException e) {
            log.error("Failed to initialize Firebase app", e);
        }
    }

    @Override
    public String upload(String originalName, String fileType, byte[] data) {
        try {
            String extension = "";
            int dotIndex = originalName.lastIndexOf('.');
            if (dotIndex >= 0) {
                extension = originalName.substring(dotIndex);
            }
            String fileName = UUID.randomUUID() + extension;

            Storage storage = StorageClient.getInstance().bucket().getStorage();
            BlobId blobId = BlobId.of(firebaseBucket, fileName);
            BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType(fileType).build();

            storage.create(blobInfo, data);

            String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8.toString());
            String downloadUrl = String.format("https://firebasestorage.googleapis.com/v0/b/%s/o/%s?alt=media",
                    firebaseBucket, encodedFileName);

            log.info("Uploaded file to Firebase storage: {}", downloadUrl);
            return downloadUrl;
        } catch (Exception e) {
            log.error("Failed to upload to Firebase", e);
            throw new RuntimeException("Firebase upload failed", e);
        }
    }
}
