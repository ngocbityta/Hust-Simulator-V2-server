package com.hustsimulator.context.storage;

/**
 * Universal interface for storage providers.
 */
public interface StorageProvider {
    /**
     * Uploads file data and returns the public URL.
     */
    String upload(String originalName, String fileType, byte[] data);
}
