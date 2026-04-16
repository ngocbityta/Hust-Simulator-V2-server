package com.hustsimulator.messaging.storage;

public interface StorageProvider {
    String upload(String originalName, String fileType, byte[] data);
}
