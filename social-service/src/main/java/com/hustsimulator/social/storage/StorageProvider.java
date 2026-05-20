package com.hustsimulator.social.storage;

public interface StorageProvider {
    String upload(String originalName, String fileType, byte[] data);
}
